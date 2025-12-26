package com.yuehai.media

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.util.Printer
import android.view.Choreographer
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

object VoiceRoomLagMonitor {

    private const val TAG = "LagMonitor"
    private const val PREF_NAME = "VoiceRoomLagMonitorPrefs"

    // ---------------- Config ----------------

    data class Config(
        var uiJankMs: Long = 200L,
        var uiCriticalMs: Long = 500L,
        var audioFrozenRate: Double = 10.0,
        var alertCooldownMs: Long = 3000L,
        var continuousJankCount: Int = 3,
        var continuousWindowMs: Long = 1000L,
        var enableLooperBlock: Boolean = true,
        var looperBlockMs: Long = 300L,
        var looperSevereMs: Long = 1000L
    )

    @Volatile
    private var currentConfig = Config()

    fun updateConfig(config: Config) {
        currentConfig = config
        Log.i(TAG, "Config Updated: $config")
    }

    // ---------------- State ----------------

    private val isMonitoring = AtomicBoolean(false)
    private var contextRef: WeakReference<Context>? = null
    private var prefs: SharedPreferences? = null

    private var lastFrameTimeNanos = 0L
    private var jankCountInWindow = 0
    private var windowStartTime = 0L
    private var looperMsgStartTime = 0L

    var isAppInBackground = false
        set(value) {
            field = value
            if (value) stop()
        }

    var onLagEventDetected: ((LagType, String) -> Unit)? = null

    enum class LagType {
        UI,
        CONTINUOUS_UI,
        AUDIO,
        LOOPER_BLOCK
    }

    // ---------------- Threads ----------------

    private val mainHandler = Handler(Looper.getMainLooper())
    private var analyzerThread: HandlerThread? = null
    private var analyzerHandler: Handler? = null

    private fun ensureAnalyzerThread() {
        if (analyzerThread == null) {
            analyzerThread = HandlerThread("LagAnalyzer").apply { start() }
            analyzerHandler = Handler(analyzerThread!!.looper)
        }
    }

    // ---------------- Init ----------------

    fun init(context: Context) {
        contextRef = WeakReference(context.applicationContext)
        prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun start() {
        if (!isMonitoring.compareAndSet(false, true)) return

        lastFrameTimeNanos = 0L
        jankCountInWindow = 0
        windowStartTime = 0L
        looperMsgStartTime = 0L

        mainHandler.post { Choreographer.getInstance().postFrameCallback(frameCallback) }

        if (currentConfig.enableLooperBlock) {
            Looper.getMainLooper().setMessageLogging(looperPrinter)
        }
    }

    fun stop() {
        if (!isMonitoring.compareAndSet(true, false)) return

        mainHandler.post { Choreographer.getInstance().removeFrameCallback(frameCallback) }

        if (currentConfig.enableLooperBlock) {
            Looper.getMainLooper().setMessageLogging(null)
        }

        analyzerHandler?.removeCallbacksAndMessages(null)
        analyzerThread?.quitSafely()
        analyzerThread = null
        analyzerHandler = null
    }

    // ---------------- Frame Monitor ----------------

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring.get()) return

            if (lastFrameTimeNanos != 0L) {
                val durationMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                val now = SystemClock.uptimeMillis()

                if (durationMs > currentConfig.uiJankMs) {

                    if (durationMs >= currentConfig.uiCriticalMs) {
                        processLagAsync(LagType.UI, durationMs, 0, 0.0)
                    }

                    if (windowStartTime == 0L || now - windowStartTime > currentConfig.continuousWindowMs) {
                        windowStartTime = now
                        jankCountInWindow = 1
                    } else {
                        jankCountInWindow++
                        if (jankCountInWindow >= currentConfig.continuousJankCount) {
                            processLagAsync(LagType.CONTINUOUS_UI, durationMs, 0, 0.0)
                            jankCountInWindow = 0
                            windowStartTime = 0L
                        }
                    }
                }
            }

            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    // ---------------- Looper Block ----------------

    private val looperPrinter = Printer { msg ->
        if (!isMonitoring.get()) return@Printer

        when {
            msg.startsWith(">>>>>") -> looperMsgStartTime = SystemClock.uptimeMillis()
            msg.startsWith("<<<<<") -> {
                val cost = SystemClock.uptimeMillis() - looperMsgStartTime
                if (cost >= currentConfig.looperBlockMs) {
                    processLagAsync(LagType.LOOPER_BLOCK, cost, 0, 0.0)
                }
            }
        }
    }

    // ---------------- Audio ----------------

    fun onAudioStatsUpdate(frozenRate: Double, rtt: Int) {
        if (!isMonitoring.get()) return
        if (frozenRate > currentConfig.audioFrozenRate) {
            processLagAsync(LagType.AUDIO, 0L, rtt, frozenRate)
        }
    }

    // ---------------- Core Report ----------------

    private fun processLagAsync(type: LagType, uiDuration: Long, rtt: Int, frozenRate: Double) {
        if (!canReport(type)) return
        updateLastReportTime(type)

        ensureAnalyzerThread()
        analyzerHandler?.post {
            if (!isMonitoring.get()) return@post

            val report = StringBuilder()
            val mem = getMemorySnapshot()

            when (type) {
                LagType.UI -> {
                    report.append("严重 UI 卡顿: ${uiDuration}ms\n")
                    if (uiDuration >= 1000) report.append(getMainThreadStackTrace())
                }
                LagType.CONTINUOUS_UI -> report.append("持续 UI 掉帧 (${currentConfig.continuousJankCount})\n")
                LagType.LOOPER_BLOCK -> {
                    report.append("主线程 Looper 阻塞: ${uiDuration}ms\n")
                    if (uiDuration >= currentConfig.looperSevereMs) report.append(getMainThreadStackTrace())
                }
                LagType.AUDIO -> report.append("语音卡顿: Frozen=$frozenRate%, RTT=${rtt}ms\n")
            }

            Log.e(TAG, "[$type]\n$report\nMem:$mem")

            onLagEventDetected?.let { callback ->
                mainHandler.post {
                    callback.invoke(
                        type,
                        if (type == LagType.AUDIO) "$frozenRate%" else "${uiDuration}ms"
                    )
                }
            }
        }
    }

    // ---------------- Utils ----------------

    private fun getMemorySnapshot(): String {
        val context = contextRef?.get() ?: return "N/A"
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            "${(mi.totalMem - mi.availMem) * 100 / mi.totalMem}% (Free:${mi.availMem / 1048576}M)"
        } catch (e: Exception) { "Err" }
    }

    private fun getMainThreadStackTrace(): String {
        return Looper.getMainLooper().thread.stackTrace
            .take(15)
            .joinToString("\n") { "  at $it" }
    }

    // ---------------- Persistent Cooldown ----------------

    private fun canReport(type: LagType): Boolean {
        val last = prefs?.getLong(type.name, 0L) ?: 0L
        val cooldown = currentConfig.alertCooldownMs
        return System.currentTimeMillis() - last >= cooldown
    }

    private fun updateLastReportTime(type: LagType) {
        prefs?.edit()?.putLong(type.name, System.currentTimeMillis())?.apply()
    }
}
