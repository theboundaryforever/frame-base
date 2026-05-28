package com.yuehai.util.util.log


import android.os.Handler
import android.os.HandlerThread
import android.util.Log


object FalconLog {

    // ====================================================================================
    // 1. 配置中心 (可动态调整或绑定项目的 BuildConfig/配置中心)
    // ====================================================================================

    // 全局总开关：如果是 Release 包或者线上需要关闭日志，将其设为 false 即可全盘静音
    var isLogEnabled: Boolean = true

    // 日志级别控制阈值（数字越小，级别越低。默认为 Log.VERBOSE 2，即全量打印）
    var logLevelThreshold: Int = Log.VERBOSE

    // ====================================================================================
    // 2. 线程隔离架构
    // ====================================================================================

    // 专属异步后台线程，将主线程的 I/O 和序列化完全剥离
    private val logThread = HandlerThread("Falcon-Log-Thread").apply { start() }
    val logHandler = Handler(logThread.looper)

    // 高频日志采样计数器
    val sampleCounter = java.util.concurrent.atomic.AtomicInteger(0)

    // ====================================================================================
    // 3. 异步 0 阻塞核心打印接口 (主线程调用瞬间返回)
    // ====================================================================================

    /**
     * 异步 Debug 日志
     * 使用 inline + crossinline 确保 Lambda 表达式在被拦截时【0 对象创建】
     */
    inline fun d(tag: String, crossinline message: () -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.DEBUG) return
        logHandler.post {
            Log.d(tag, message())
        }
    }

    /**
     * 异步 Info 日志
     */
    inline fun i(tag: String, crossinline message: () -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.INFO) return
        logHandler.post {
            Log.i(tag, message())
        }
    }

    /**
     * 异步 Warning 日志
     */
    inline fun w(tag: String, crossinline message: () -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.WARN) return
        logHandler.post {
            Log.w(tag, message())
        }
    }

    /**
     * 异步 Error 日志 (可带异常堆栈)
     */
    inline fun e(tag: String, throwable: Throwable? = null, crossinline message: () -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.ERROR) return
        logHandler.post {
            if (throwable != null) {
                Log.e(tag, message(), throwable)
            } else {
                Log.e(tag, message())
            }
        }
    }

    // ====================================================================================
    // 4. 进阶高级版：高频/复杂对象专属
    // ====================================================================================

    /**
     * 专属方案：解决主线程高频【反射转 JSON】卡顿
     * 传入任意复杂的 Data Object，并在后台线程异步执行 toJson 转换，主线程耗时为 0
     * @param jsonSerializer 传入你项目现有的 JSON 转换闭包，如 { Gson().toJson(it) } 或 { toJsonErrorNull(it) }
     */
    fun <T> json(tag: String, dataObject: T?, jsonSerializer: (T) -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.DEBUG || dataObject == null) return

        logHandler.post {
            try {
                // 在异步线程执行耗时的反射/序列化操作
                val jsonStr = jsonSerializer(dataObject)
                Log.d(tag, " [JSON_ASYNC] -> $jsonStr")
            } catch (e: Exception) {
                Log.e(tag, "FalconLog json serialize failed", e)
            }
        }
    }

    /**
     * 专属方案：解决【滑动/音频波形等每秒几十次的高频回调】卡顿（采样降频技术）
     * @param sampleInterval 采样间隔，默认 30。即每隔 30 次触发，才真正打印一条日志
     */
    inline fun dSample(tag: String, sampleInterval: Int = 30, crossinline message: () -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.DEBUG) return

        // 主线程只做轻量的原子自增计算，不通过则直接被清洗，不丢进线程池
        if (sampleCounter.incrementAndGet() % sampleInterval == 0) {
            logHandler.post {
                Log.d(tag, "[Sampled] ${message()}")
            }
            sampleCounter.set(0) // 归零防止数字溢出
        }
    }

    // ====================================================================================
    // 5. 极端场景逃生通道 (强制同步写入，防止进程暴毙时日志在内存队列中丢失)
    // ====================================================================================

    /**
     * 同步 Error 打印：专用于 UncaughtExceptionHandler 崩溃捕获或 onDestroy 等应用退出的紧要关头
     * 此时会短暂阻塞调用线程，但能 100% 保证日志不遗漏、成功刷入系统底层
     */
    inline fun syncE(tag: String, throwable: Throwable? = null, message: () -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.ERROR) return
        if (throwable != null) {
            Log.e(tag, message(), throwable)
        } else {
            Log.e(tag, message())
        }
    }
}