package com.yuehai.media

import android.app.ActivityManager
import android.content.Context
import android.os.*
import android.util.Log
import android.view.Choreographer
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import android.util.Printer

object VoiceRoomLagMonitor {

    private const val TAG = "LagMonitor"

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


    private val isMonitoring = AtomicBoolean(false)
    private val lastReportTime = AtomicLong(0L)

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


    private val mainHandler = Handler(Looper.getMainLooper())

    private var analyzerThread: HandlerThread? = null
    private var analyzerHandler: Handler? = null

    private fun ensureAnalyzerThread() {
        if (analyzerThread == null) {
            analyzerThread = HandlerThread("LagAnalyzer").apply { start() }
            analyzerHandler = Handler(analyzerThread!!.looper)
        }
    }


    private var contextRef: WeakReference<Context>? = null

    fun init(context: Context) {
        contextRef = WeakReference(context.applicationContext)
    }


    fun start() {
        if (!isMonitoring.compareAndSet(false, true)) return

        lastFrameTimeNanos = 0L
        jankCountInWindow = 0
        windowStartTime = 0L
        looperMsgStartTime = 0L


        mainHandler.post {
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }


        if (currentConfig.enableLooperBlock) {
            Looper.getMainLooper().setMessageLogging(looperPrinter)
        }
    }

    fun stop() {
        if (!isMonitoring.compareAndSet(true, false)) return

        mainHandler.post {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
        }


        if (currentConfig.enableLooperBlock) {
            Looper.getMainLooper().setMessageLogging(null)
        }

        analyzerHandler?.removeCallbacksAndMessages(null)
        analyzerThread?.quitSafely()
        analyzerThread = null
        analyzerHandler = null
    }



    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring.get()) return

            if (lastFrameTimeNanos != 0L) {
                val durationMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                val now = SystemClock.uptimeMillis()

                if (durationMs > currentConfig.uiJankMs) {


                    if (durationMs >= currentConfig.uiCriticalMs) {
                        processLagAsync(
                            LagType.UI,
                            uiDuration = durationMs,
                            rtt = 0,
                            frozenRate = 0.0
                        )
                    }


                    if (windowStartTime == 0L || now - windowStartTime > currentConfig.continuousWindowMs) {
                        windowStartTime = now
                        jankCountInWindow = 1
                    } else {
                        jankCountInWindow++
                        if (jankCountInWindow >= currentConfig.continuousJankCount) {
                            processLagAsync(
                                LagType.CONTINUOUS_UI,
                                uiDuration = durationMs,
                                rtt = 0,
                                frozenRate = 0.0
                            )
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


    private val looperPrinter = Printer { msg ->
        if (!isMonitoring.get()) return@Printer

        when {
            msg.startsWith(">>>>>") -> {
                looperMsgStartTime = SystemClock.uptimeMillis()
            }

            msg.startsWith("<<<<<") -> {
                val cost = SystemClock.uptimeMillis() - looperMsgStartTime
                if (cost >= currentConfig.looperBlockMs) {
                    processLagAsync(
                        LagType.LOOPER_BLOCK,
                        uiDuration = cost,
                        rtt = 0,
                        frozenRate = 0.0
                    )
                }
            }
        }
    }


    fun onAudioStatsUpdate(frozenRate: Double, rtt: Int) {
        if (!isMonitoring.get()) return

        if (frozenRate > currentConfig.audioFrozenRate) {
            processLagAsync(
                LagType.AUDIO,
                uiDuration = 0L,
                rtt = rtt,
                frozenRate = frozenRate
            )
        }
    }


    private fun processLagAsync(
        type: LagType,
        uiDuration: Long,
        rtt: Int,
        frozenRate: Double
    ) {
        val now = System.currentTimeMillis()
        val last = lastReportTime.get()

        if (now - last < currentConfig.alertCooldownMs) return
        if (!lastReportTime.compareAndSet(last, now)) return

        ensureAnalyzerThread()

        analyzerHandler?.post {
            if (!isMonitoring.get()) return@post

            val report = StringBuilder()
            val mem = getMemorySnapshot()

            when (type) {
                LagType.UI -> {
                    report.append("严重 UI 卡顿: ${uiDuration}ms\n")
                    if (uiDuration >= 1000) {
                        report.append(getMainThreadStackTrace())
                    }
                }

                LagType.CONTINUOUS_UI -> {
                    report.append("持续 UI 掉帧 (${currentConfig.continuousJankCount}/${currentConfig.continuousWindowMs}ms)\n")
                }

                LagType.LOOPER_BLOCK -> {
                    report.append("主线程 Looper 阻塞: ${uiDuration}ms\n")
                    if (uiDuration >= currentConfig.looperSevereMs) {
                        report.append(getMainThreadStackTrace())
                    }
                }

                LagType.AUDIO -> {
                    report.append("语音卡顿: Frozen=$frozenRate%, RTT=${rtt}ms\n")
                }
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


    private fun getMemorySnapshot(): String {
        val context = contextRef?.get() ?: return "N/A"
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            "${(mi.totalMem - mi.availMem) * 100 / mi.totalMem}% (Free:${mi.availMem / 1048576}M)"
        } catch (e: Exception) {
            "Err"
        }
    }

    private fun getMainThreadStackTrace(): String {
        return Looper.getMainLooper().thread.stackTrace
            .take(15)
            .joinToString("\n") { "  at $it" }
    }
}
