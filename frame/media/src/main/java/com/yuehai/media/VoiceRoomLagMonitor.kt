package com.yuehai.media

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.*
import android.util.Log
import android.util.Printer
import android.view.Choreographer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 全局卡顿监控工具
 * 核心原理：主线程 Looper 日志监控 + 后台线程主动采样堆栈
 */
object VoiceRoomLagMonitor {

    private const val TAG = "LagMonitor"

    data class Config(
        var uiCriticalMs: Long = 300L,        // 判定卡顿的阈值 (ms)
        var sampleIntervalMs: Long = 80L,    // 堆栈采样频率 (ms)
        var alertCooldownMs: Long = 3000L,   // 同一 Activity 告警冷却 (ms)
        var enabled: Boolean = true          // 总开关
    )

    private var currentConfig = Config()
    private val isMonitoring = AtomicBoolean(false)
    private var application: Application? = null

    // 状态记录
    @Volatile
    private var topActivityName: String = "Unknown"
    @Volatile
    private var looperMsgStartTime = 0L
    private val lastReportTimes = mutableMapOf<String, Long>()

    // 堆栈缓存：Long 为时间戳，String 为堆栈内容
    private val stackCache = Collections.synchronizedList(mutableListOf<Pair<Long, String>>())
    private const val MAX_STACK_CACHE = 20

    // 线程模型
    private val mainHandler = Handler(Looper.getMainLooper())
    private var analyzerThread: HandlerThread? = null
    private var analyzerHandler: Handler? = null

    /**
     * 在 Application.onCreate 中初始化
     */
    fun install(app: Application, config: Config = Config()) {
        this.application = app
        this.currentConfig = config

        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            private var startedActivityCount = 0

            override fun onActivityResumed(activity: Activity) {
                topActivityName = activity.javaClass.simpleName
            }

            override fun onActivityStarted(activity: Activity) {
                startedActivityCount++
                if (startedActivityCount == 1) start()
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount--
                if (startedActivityCount == 0) stop()
            }

            override fun onActivityCreated(a: Activity, s: Bundle?) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, o: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })

        if (config.enabled) start()
    }

    private fun start() {
        if (!currentConfig.enabled || !isMonitoring.compareAndSet(false, true)) return

        ensureAnalyzerThread()
        Looper.getMainLooper().setMessageLogging(looperPrinter)

        // 启动后台采样循环
        startStackSampler()
        Log.i(TAG, ">>> 卡顿监控已启动 (阈值: ${currentConfig.uiCriticalMs}ms)")
    }

    private fun stop() {
        if (!isMonitoring.compareAndSet(true, false)) return
        Looper.getMainLooper().setMessageLogging(null)
        analyzerHandler?.removeCallbacksAndMessages(null)
        analyzerThread?.quitSafely()
        analyzerThread = null
        analyzerHandler = null
        Log.i(TAG, ">>> 卡顿监控已停止")
    }

    private fun ensureAnalyzerThread() {
        if (analyzerThread == null) {
            analyzerThread = HandlerThread("LagAnalyzer").apply { start() }
            analyzerHandler = Handler(analyzerThread!!.looper)
        }
    }

    /**
     * 后台循环采样：每隔固定时间抓取一次主线程堆栈
     */
    private fun startStackSampler() {
        analyzerHandler?.post(object : Runnable {
            override fun run() {
                if (!isMonitoring.get()) return

                // 1. 抓取当前主线程堆栈
                val stack = captureCurrentStack()

                // 2. 存入环形缓存
                synchronized(stackCache) {
                    if (stackCache.size >= MAX_STACK_CACHE) stackCache.removeAt(0)
                    stackCache.add(SystemClock.uptimeMillis() to stack)
                }

                // 3. 主动侦查：如果当前消息执行时间已经超过阈值，且还没结束，立即上报
                val startTime = looperMsgStartTime
                if (startTime != 0L) {
                    val diff = SystemClock.uptimeMillis() - startTime
                    if (diff > currentConfig.uiCriticalMs) {
                        reportLag("LONG_RUNNING_TASK", diff, stack)
                        // 重置时间防止同一条消息重复触发（直到下一条消息开始）
                        looperMsgStartTime = 0L
                    }
                }

                analyzerHandler?.postDelayed(this, currentConfig.sampleIntervalMs)
            }
        })
    }

    private fun captureCurrentStack(): String {
        val stackTrace = Looper.getMainLooper().thread.stackTrace
        return stackTrace.take(30) // 抓取深度30层
            .filter { !it.className.startsWith("java.lang.reflect") && !it.className.startsWith("dalvik.system") }
            .joinToString("\n") { element ->
                // 重点标记业务代码
                val isAppCode = element.className.startsWith("com.yuehai") // 替换为你的真实包名前缀
                val prefix = if (isAppCode) " 🔥 -> " else "    "
                "$prefix at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})"
            }
    }

    /**
     * Looper 消息监听器
     */
    private val looperPrinter = Printer { msg ->
        if (msg.startsWith(">>>>>")) {
            looperMsgStartTime = SystemClock.uptimeMillis()
        } else if (msg.startsWith("<<<<<")) {
            val startTime = looperMsgStartTime
            if (startTime != 0L) {
                val duration = SystemClock.uptimeMillis() - startTime
                if (duration > currentConfig.uiCriticalMs) {
                    // 消息结束时，发现超时，从缓存中取最匹配的堆栈
                    val stack = getBestStackFromCache(startTime, SystemClock.uptimeMillis())
                    reportLag("LOOPER_BLOCK", duration, stack)
                }
            }
            looperMsgStartTime = 0L
        }
    }

    private fun getBestStackFromCache(start: Long, end: Long): String {
        synchronized(stackCache) {
            // 找到卡顿期间抓取到的最后一个堆栈，通常最接近耗时方法
            return stackCache.lastOrNull { it.first in start..end }?.second
                ?: "No relevant stack trace captured."
        }
    }

    private fun reportLag(type: String, duration: Long, stack: String) {
        val now = System.currentTimeMillis()
        val lastTime = lastReportTimes[topActivityName] ?: 0L
        if (now - lastTime < currentConfig.alertCooldownMs) return

        lastReportTimes[topActivityName] = now

        val memInfo = getMemoryInfo()

        // 使用 Log.e 以便在 Logcat 中以红色突出显示
        Log.e(TAG, """
            
            ┌────── ⚠️ 卡顿检测报告 ──────────────────────────────────
            │ 页面: $topActivityName
            │ 类型: $type
            │ 耗时: ${duration}ms (阈值: ${currentConfig.uiCriticalMs}ms)
            │ 内存: $memInfo
            ├────── 关键堆栈 (🔥 为业务代码) ──────────────────────────
            $stack
            └──────────────────────────────────────────────────────
        """.trimIndent())
    }

    private fun getMemoryInfo(): String {
        val app = application ?: return "N/A"
        return try {
            val am = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            "可用:${mi.availMem / 1024 / 1024}MB / 总共:${mi.totalMem / 1024 / 1024}MB (低内存模式:${mi.lowMemory})"
        } catch (e: Exception) { "Error" }
    }
}