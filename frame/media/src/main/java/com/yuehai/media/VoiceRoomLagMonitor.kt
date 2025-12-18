package com.yuehai.media

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Choreographer


object VoiceRoomLagMonitor {
    private const val TAG = "LagMonitor"

    private var uiJankThreshold = 200L
    private var audioFrozenThreshold = 10.0
    private var lastReportTime = 0L
    private const val REPORT_INTERVAL = 2000L

    private var isMonitoring = false
    private var lastFrameTimeNanos = 0L

    private val analyzerThread = HandlerThread("LagAnalyzer").apply { start() }
    private val analyzerHandler = Handler(analyzerThread.looper)

    var onUILagDetected: ((duration: Long, stackTrace: String) -> Unit)? = null

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring) return

            val now = frameTimeNanos
            if (lastFrameTimeNanos != 0L) {
                val frameDurationMs = (now - lastFrameTimeNanos) / 1_000_000
                if (frameDurationMs > uiJankThreshold) {
                    analyzerHandler.post {
                        handleUILagAsync(frameDurationMs)
                    }
                }
            }
            lastFrameTimeNanos = now
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start(uiThreshold: Long = 200L, audioThreshold: Double = 10.0) {
        if (isMonitoring) return
        this.uiJankThreshold = uiThreshold
        this.audioFrozenThreshold = audioThreshold
        this.isMonitoring = true
        this.lastFrameTimeNanos = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
        Log.i(TAG, "🚀 Monitoring started (UI: ${uiThreshold}ms, Audio: ${audioThreshold}%)")
    }

    fun stop() {
        isMonitoring = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        analyzerHandler.removeCallbacksAndMessages(null)
    }

    private fun handleUILagAsync(duration: Long) {
        val now = System.currentTimeMillis()
        if (now - lastReportTime < REPORT_INTERVAL) return // 抑制频繁上报
        lastReportTime = now

        val stackTrace = getMainThreadStackTrace()

        Log.e(TAG, "⚠️ 检测到 UI 卡顿: ${duration}ms\n可能原因堆栈:\n$stackTrace")

        Handler(Looper.getMainLooper()).post {
            onUILagDetected?.invoke(duration, stackTrace)
        }
    }


    private fun getMainThreadStackTrace(): String {
        val mainThread = Looper.getMainLooper().thread
        val stackElements = mainThread.stackTrace
        return stackElements.joinToString("\n") { "    at $it" }
    }


    fun onAudioStatsUpdate(frozenRate: Double, rtt: Int) {
        if (frozenRate > audioFrozenThreshold) {
            analyzerHandler.post {
                val now = System.currentTimeMillis()
                if (now - lastReportTime > REPORT_INTERVAL) {
                    Log.e(TAG, "🚨 音频卡顿报警: 卡顿率 ${frozenRate}%, 延迟 ${rtt}ms")
                    lastReportTime = now
                }
            }
        }
    }
}