package com.yuehai.media

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import android.util.Printer
import android.view.Choreographer
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object VoiceRoomLagMonitor {

    private const val TAG = "LagMonitor"
    private const val PREF_NAME = "VoiceRoomLagMonitorPrefs"

    data class Config(
        var uiJankMs: Long = 200L,          // 判定为单帧掉帧
        var uiCriticalMs: Long = 500L,      // 判定为严重卡顿，抓取堆栈
        var audioFrozenRate: Double = 10.0,
        var alertCooldownMs: Long = 5000L,  // 告警冷却
        var looperBlockMs: Long = 300L,     // Looper单次消息耗时阈值
        var sampleIntervalMs: Long = 100L   // 采样间隔
    )

    @Volatile
    private var currentConfig = Config()
    private val isMonitoring = AtomicBoolean(false)
    private var contextRef: WeakReference<Context>? = null
    private var prefs: SharedPreferences? = null

    // 堆栈采样相关
    private val stackCache = Collections.synchronizedList(mutableListOf<Pair<Long, String>>())
    private const val MAX_STACK_CACHE = 10

    // 线程处理
    private val mainHandler = Handler(Looper.getMainLooper())
    private var analyzerThread: HandlerThread? = null
    private var analyzerHandler: Handler? = null

    fun init(context: Context) {
        contextRef = WeakReference(context.applicationContext)
        prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun start() {
        if (!isMonitoring.compareAndSet(false, true)) return
        ensureAnalyzerThread()

        // 1. 开启主线程 Looper 监控
        Looper.getMainLooper().setMessageLogging(looperPrinter)

        // 2. 开启 Choreographer 帧率监控
        mainHandler.post { Choreographer.getInstance().postFrameCallback(frameCallback) }

        // 3. 启动后台定时采样堆栈
        startStackSampler()

        Log.i(TAG, "LagMonitor Started")
    }

    private fun ensureAnalyzerThread() {
        if (analyzerThread == null) {
            analyzerThread = HandlerThread("LagAnalyzer").apply { start() }
            analyzerHandler = Handler(analyzerThread!!.looper)
        }
    }

    private fun startStackSampler() {
        analyzerHandler?.post(object : Runnable {
            override fun run() {
                if (isMonitoring.get()) {
                    captureMainThreadStack()
                    analyzerHandler?.postDelayed(this, currentConfig.sampleIntervalMs)
                }
            }
        })
    }

    // ---------------- 核心逻辑：采样与提取 ----------------

    private fun captureMainThreadStack() {
        val stackTrace = Looper.getMainLooper().thread.stackTrace
        val formatted = stackTrace.take(20).joinToString("\n") { "  at $it" }

        synchronized(stackCache) {
            if (stackCache.size >= MAX_STACK_CACHE) {
                stackCache.removeAt(0)
            }
            stackCache.add(SystemClock.uptimeMillis() to formatted)
        }
    }

    /**
     * 获取最近一次卡顿期间最相关的堆栈
     */
    private fun getRelevantStack(startTime: Long, endTime: Long): String {
        synchronized(stackCache) {
            // 过滤出落在卡顿时间区间的堆栈记录
            val relevant = stackCache.filter { it.first in startTime..endTime }
            return if (relevant.isNotEmpty()) {
                // 返回最后一次记录，通常最接近卡顿卡住的位置
                "Captured during lag:\n${relevant.last().second}"
            } else {
                "No stack trace captured during the lag interval."
            }
        }
    }

    // ---------------- 监控接口 ----------------

    private var looperMsgStartTime = 0L

    private val looperPrinter = Printer { msg ->
        if (!isMonitoring.get()) return@Printer
        if (msg.startsWith(">>>>>")) {
            looperMsgStartTime = SystemClock.uptimeMillis()
        } else if (msg.startsWith("<<<<<")) {
            val endTime = SystemClock.uptimeMillis()
            val cost = endTime - looperMsgStartTime
            if (cost >= currentConfig.looperBlockMs) {
                val stack = getRelevantStack(looperMsgStartTime, endTime)
                processLagAsync(LagType.LOOPER_BLOCK, cost, stack)
            }
        }
    }

    private var lastFrameTimeNanos = 0L
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring.get()) return
            if (lastFrameTimeNanos != 0L) {
                val durationMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                if (durationMs >= currentConfig.uiCriticalMs) {
                    val endTime = SystemClock.uptimeMillis()
                    val stack = getRelevantStack(endTime - durationMs, endTime)
                    processLagAsync(LagType.UI, durationMs, stack)
                }
            }
            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    // ---------------- 报告处理 ----------------

    private fun processLagAsync(type: LagType, duration: Long, stack: String) {
        if (!canReport(type)) return
        updateLastReportTime(type)

        analyzerHandler?.post {
            val mem = getMemorySnapshot()
            val report = """
                [Lag Detected]
                Type: $type
                Duration: ${duration}ms
                Memory: $mem
                Stack Trace:
                $stack
            """.trimIndent()

            Log.e(TAG, report)

            // 外部回调
            onLagEventDetected?.let { callback ->
                mainHandler.post { callback.invoke(type, "${duration}ms") }
            }
        }
    }

    // ---------------- 常规工具 ----------------

    private fun getMemorySnapshot(): String {
        val context = contextRef?.get() ?: return "N/A"
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return "Free:${mi.availMem / 1024 / 1024}MB / Total:${mi.totalMem / 1024 / 1024}MB"
    }

    private fun canReport(type: LagType): Boolean {
        val last = prefs?.getLong(type.name, 0L) ?: 0L
        return System.currentTimeMillis() - last >= currentConfig.alertCooldownMs
    }

    private fun updateLastReportTime(type: LagType) {
        prefs?.edit()?.putLong(type.name, System.currentTimeMillis())?.apply()
    }

    enum class LagType { UI, LOOPER_BLOCK, AUDIO }

    var onLagEventDetected: ((LagType, String) -> Unit)? = null

    fun stop() {
        if (!isMonitoring.compareAndSet(true, false)) return
        Looper.getMainLooper().setMessageLogging(null)
        analyzerHandler?.removeCallbacksAndMessages(null)
        analyzerThread?.quitSafely()
        analyzerThread = null
        analyzerHandler = null
    }
}