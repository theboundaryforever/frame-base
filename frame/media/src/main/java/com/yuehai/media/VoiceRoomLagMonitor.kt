package com.yuehai.media

import android.app.ActivityManager
import android.content.Context
import android.os.*
import android.util.Log
import android.view.Choreographer
import java.lang.ref.WeakReference


object VoiceRoomLagMonitor {
    private const val TAG = "LagMonitor"


    private const val JANK_THRESHOLD = 200L      // 轻微卡顿
    private const val CRITICAL_THRESHOLD = 500L  // 严重卡顿（此时抓堆栈）
    private const val ALERT_COOLDOWN = 3000L     // 同类报警冷却时间

    private var isMonitoring = false
    private var lastFrameTimeNanos = 0L
    private var lastReportTime = 0L
    private var contextRef: WeakReference<Context>? = null


    var isAppInBackground = false

    private val analyzerHandler by lazy {
        val thread = HandlerThread("LagAnalyzer")
        thread.start()
        Handler(thread.looper)
    }

    fun init(context: Context) {
        this.contextRef = WeakReference(context.applicationContext)
    }

    fun start() {
        if (isMonitoring) return
        isMonitoring = true
        lastFrameTimeNanos = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
        Log.i(TAG, "Monitor Started")
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring || isAppInBackground) return

            if (lastFrameTimeNanos != 0L) {
                val duration = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                if (duration > JANK_THRESHOLD) {
                    processLag(duration)
                }
            }
            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private fun processLag(duration: Long) {
        val now = System.currentTimeMillis()
        if (now - lastReportTime < ALERT_COOLDOWN) return
        lastReportTime = now

        analyzerHandler.post {
            val stackTrace = if (duration >= CRITICAL_THRESHOLD) {
                getMainThreadStackTrace()
            } else {
                "Minor jank, stack trace skipped."
            }

            val mem = getMemorySnapshot()

            // 最终上报模型
            val logOutput = """
                [Lag Alert] Duration: ${duration}ms
                [Env] Memory Usage: $mem
                [Analysis] ${if(duration >= CRITICAL_THRESHOLD) "Critical Lag Detected!" else "Normal Jank"}
                $stackTrace
            """.trimIndent()

            Log.e(TAG, logOutput)
        }
    }

    private fun getMemorySnapshot(): String {
        val context = contextRef?.get() ?: return "N/A"
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val usage = (mi.totalMem - mi.availMem) * 100 / mi.totalMem
            "$usage% (Free: ${mi.availMem / 1024 / 1024}MB)"
        } catch (e: Exception) { "Error" }
    }

    private fun getMainThreadStackTrace(): String {
        val mainThread = Looper.getMainLooper().thread
        return mainThread.stackTrace.take(15).joinToString("\n") { "  at $it" }
    }

    fun stop() {
        isMonitoring = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}