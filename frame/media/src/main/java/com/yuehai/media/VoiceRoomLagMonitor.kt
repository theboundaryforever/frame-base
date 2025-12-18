package com.yuehai.media

import android.app.ActivityManager
import android.content.Context
import android.os.*
import android.util.Log
import android.view.Choreographer
import java.lang.ref.WeakReference

object VoiceRoomLagMonitor {
    private const val TAG = "LagMonitor"

    // --- 动态配置参数 ---
    data class Config(
        var uiJankMs: Long = 200L,        // 普通卡顿阈值
        var uiCriticalMs: Long = 500L,    // 严重卡顿阈值（抓堆栈）
        var audioFrozenRate: Double = 10.0, // 音频卡顿阈值
        var alertCooldownMs: Long = 3000L,  // 两次报警间的最小间隔
        var continuousJankCount: Int = 3    // 连续多少帧掉帧才算“持续卡顿”
    )

    private var currentConfig = Config()

    /**
     * 提供一个动态更新配置的方法，可以对接服务器的 AppConfig
     */
    fun updateConfig(config: Config) {
        currentConfig = config
        Log.i(TAG, "Config Updated: $config")
    }

    private var isMonitoring = false
    private var lastFrameTimeNanos = 0L
    private var lastReportTime = 0L
    private var jankCounter = 0 // 用于计算连续卡顿

    private var contextRef: WeakReference<Context>? = null
    var isAppInBackground = false
    var onLagEventDetected: ((LagType, String) -> Unit)? = null

    enum class LagType { UI, AUDIO, CONTINUOUS_UI }

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
        jankCounter = 0
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring || isAppInBackground) return

            if (lastFrameTimeNanos != 0L) {
                val duration = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000

                if (duration > currentConfig.uiJankMs) {
                    jankCounter++
                    // 逻辑 A: 单次严重卡顿
                    if (duration > currentConfig.uiCriticalMs) {
                        processLagAsync(LagType.UI, duration, 0, 0.0)
                    }
                    // 逻辑 B: 连续轻微卡顿 (这种其实更影响用户体验)
                    else if (jankCounter >= currentConfig.continuousJankCount) {
                        processLagAsync(LagType.CONTINUOUS_UI, duration, 0, 0.0)
                        jankCounter = 0
                    }
                } else {
                    jankCounter = 0 // 恢复顺畅，重置计数器
                }
            }
            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun onAudioStatsUpdate(frozenRate: Double, rtt: Int) {
        if (!isMonitoring || isAppInBackground) return
        if (frozenRate > currentConfig.audioFrozenRate) {
            processLagAsync(LagType.AUDIO, 0L, rtt, frozenRate)
        }
    }

    private fun processLagAsync(type: LagType, uiDuration: Long, rtt: Int, frozenRate: Double) {
        val now = System.currentTimeMillis()
        if (now - lastReportTime < currentConfig.alertCooldownMs) return
        lastReportTime = now

        analyzerHandler.post {
            val report = StringBuilder()
            val mem = getMemorySnapshot()

            when (type) {
                LagType.UI -> report.append("严重单次卡顿: ${uiDuration}ms\n${getMainThreadStackTrace()}")
                LagType.CONTINUOUS_UI -> report.append("检测到持续掉帧 (Jank Count: ${currentConfig.continuousJankCount})\n")
                LagType.AUDIO -> report.append("语音卡顿: $frozenRate%, RTT: ${rtt}ms\n")
            }

            Log.e(TAG, "[$type Report]\n$report\nMem: $mem")

            onLagEventDetected?.let { callback ->
                Handler(Looper.getMainLooper()).post {
                    callback.invoke(type, if (type == LagType.AUDIO) "$frozenRate%" else "${uiDuration}ms")
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
            "${(mi.totalMem - mi.availMem) * 100 / mi.totalMem}% (Free:${mi.availMem/1048576}M)"
        } catch (e: Exception) { "Err" }
    }

    private fun getMainThreadStackTrace(): String {
        return Looper.getMainLooper().thread.stackTrace.take(15).joinToString("\n") { "  at $it" }
    }

    fun stop() {
        isMonitoring = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        analyzerHandler.removeCallbacksAndMessages(null)
    }
}