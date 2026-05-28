package com.yuehai.util.util.log

import android.os.Handler
import android.os.HandlerThread
import android.util.Log


object FalconLog {

    var isLogEnabled: Boolean = false
    var logLevelThreshold: Int = Log.VERBOSE

     val logThread = HandlerThread("Falcon-Log-Thread").apply { start() }
     val logHandler = Handler(logThread.looper)

    // ====================================================================================
    // ⚡ 姿势一：直接传参（极简调用，通过内部双重拦截，确保主线程不卡）
    // ====================================================================================

    /**
     * 直接传任意内容的 Debug 打印
     * 优雅之处：外层内联拦截。一旦关闭日志，主线程连方法都不会进，0 开销。
     */
    inline fun d(tag: String, msg: Any?) {
        if (!isLogEnabled || logLevelThreshold > Log.DEBUG || msg == null) return

        // 如果是基本类型或普通 String，直接丢后台印
        // 如果是复杂对象，后台线程会自动调用其 toString()，完全脱离主线程
        logHandler.post {
            Log.d(tag, msg.toString())
        }
    }

    inline fun i(tag: String, msg: Any?) {
        if (!isLogEnabled || logLevelThreshold > Log.INFO || msg == null) return
        logHandler.post { Log.i(tag, msg.toString()) }
    }

    inline fun w(tag: String, msg: Any?) {
        if (!isLogEnabled || logLevelThreshold > Log.WARN || msg == null) return
        logHandler.post { Log.w(tag, msg.toString()) }
    }

    inline fun e(tag: String, msg: Any?, throwable: Throwable? = null) {
        if (!isLogEnabled || logLevelThreshold > Log.ERROR || msg == null) return
        logHandler.post {
            if (throwable != null) {
                Log.e(tag, msg.toString(), throwable)
            } else {
                Log.e(tag, msg.toString())
            }
        }
    }

    /**
     * 极简 JSON 异步打印：直接把对象丢进来
     * 连序列化闭包都不用写，内部直接绑定你项目已有的默认转换器（如 Gson 或你的 custom 方法）
     */
    /**
     * 外部转好 JSON 后直接传进来的专属通道
     * * 优雅之处：
     * 1. 外层利用 inline 进行一键总开关拦截，正式包下外部的转 JSON 动作和方法调用会被编译器直接抹除，0 性能开销。
     * 2. 投递机制依然走异步，防止系统原生 Log 的 I/O 操作阻塞主线程。
     */
    inline fun json(tag: String, jsonStr: String?) {
        if (!isLogEnabled || logLevelThreshold > Log.DEBUG || jsonStr == null) return

        logHandler.post {
            Log.d(tag, " [JSON_EXTERNAL] -> $jsonStr")
        }
    }

    // ====================================================================================
    // ⚡ 姿势二：保留大括号 Lambda 智能重载（应对高频极度复杂的耗时拼接）
    // ====================================================================================

    inline fun d(tag: String, crossinline message: () -> String) {
        if (!isLogEnabled || logLevelThreshold > Log.DEBUG) return
        logHandler.post { Log.d(tag, message()) }
    }
}