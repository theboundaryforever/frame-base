package com.yuehai.coroutine.coroutine.dispatcher

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import android.util.Log
import com.yuehai.coroutine.thread.NamedThreadFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.lang.Thread.NORM_PRIORITY
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 线程池统一管理，管理策略：
 * 1. 1条UI主线程
 * 2. 1条高优先级单线程，用于核心场景序列化并发控制
 * 3. 1条低优先级单线程，用于非核心场景序列化并发控制
 * 4. 1个高优先级线程池，用于大部分场景的任务执行
 * 5. 1个低优先级线程池，用于处理耗时&不高优任务，这类任务一定要使用它，避免对核心场景CPU的抢占
 */
object Dispatcher {

    private val AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors()
    private const val UI_CORE_POOL_SIZE = 1
    private const val HIGH_SERIAL_CORE_POOL_SIZE = 1

    // -----------------------------------------------------------------------
    // 优化点 1/3: 限制 IO_CORE_POOL_SIZE 最大为 2 (防止低优先级并发过高)
    private val IO_CORE_POOL_SIZE =
        2.coerceAtMost(2.coerceAtLeast((AVAILABLE_PROCESSORS - UI_CORE_POOL_SIZE - HIGH_SERIAL_CORE_POOL_SIZE) / 2))

    // 优化点 2/3: 限制 CPU_CORE_POOL_SIZE 最大为 2 (防止高优先级任务抢占过多CPU)
    private val CPU_CORE_POOL_SIZE =
        2.coerceAtMost(2.coerceAtLeast(AVAILABLE_PROCESSORS - UI_CORE_POOL_SIZE - HIGH_SERIAL_CORE_POOL_SIZE))
    // -----------------------------------------------------------------------


    init {
        Log.d(
            "Dispatcher",
            "AVAILABLE_PROCESSORS:$AVAILABLE_PROCESSORS, IO_CORE_POOL_SIZE:$IO_CORE_POOL_SIZE, CPU_CORE_POOL_SIZE:$CPU_CORE_POOL_SIZE"
        )
    }

    val UI: CoroutineDispatcher by lazy {
        Handler(Looper.getMainLooper()).asCoroutineDispatcher("ui")
    }

    val highSerialHandler by lazy {
        Handler(
            HandlerThread(
                "high_serial_handle_thread",
                Process.THREAD_PRIORITY_MORE_FAVORABLE
            ).apply { start() }.looper
        )
    }

    fun runOnHighSerialThread(runnable: Runnable, delay: Long = 0L) {
        if (delay == 0L && Looper.myLooper() === highSerialHandler.looper) {
            runnable.run()
            return
        }

        highSerialHandler.postDelayed(runnable, delay)
    }

    /**
     * 从哪里run就行哪里remove，别remove错了！！！
     */
    fun removeFromHighSerialThread(runnable: Runnable) {
        highSerialHandler.removeCallbacks(runnable)
    }

    /**
     * 高优先级 单线程序列化操作，核心场景使用（例如：语聊房）
     */
    val HIGH_SERIAL: CoroutineDispatcher by lazy { highSerialHandler.asCoroutineDispatcher() }

    private val lowSerialHandler by lazy {
        Handler(
            HandlerThread(
                "low_serial_handle_thread",
                Process.THREAD_PRIORITY_DEFAULT
            ).apply { start() }.looper
        )
    }

    fun runOnLowSerialThread(runnable: Runnable, delay: Long = 0L) {
        if (delay == 0L && Looper.myLooper() === lowSerialHandler.looper) {
            runnable.run()
            return
        }

        lowSerialHandler.postDelayed(runnable, delay)
    }

    /**
     * 从哪里run就行哪里remove，别remove错了！！！
     */
    fun removeFromLowSerialThread(runnable: Runnable) {
        lowSerialHandler.removeCallbacks(runnable)
    }

    /**
     * 低优先级 单线程序列化操作，非核心序列化场景使用
     */
    val LOW_SERIAL: CoroutineDispatcher by lazy { lowSerialHandler.asCoroutineDispatcher() }

    /**
     * 高优先级执行器，程序中尽量使用该执行器
     * 核心线程数已通过 CPU_CORE_POOL_SIZE 限制为最大 2
     */
    val highExecutor: ExecutorService by lazy {
        Executors.newFixedThreadPool(
            CPU_CORE_POOL_SIZE,
            NamedThreadFactory("global-high-thread", NORM_PRIORITY)
        )
    }

    val HIGH: CoroutineDispatcher by lazy { highExecutor.asCoroutineDispatcher() }

    private val lowBackupExecutor by lazy {
        ThreadPoolExecutor(
            IO_CORE_POOL_SIZE, // 核心线程数已限制为最大 2
            IO_CORE_POOL_SIZE, // 最大线程数跟随核心线程数（已限制为最大 2）
            30,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            NamedThreadFactory("global-low-backup-thread", NORM_PRIORITY - 1)
        ).apply {
            // 允许核心线程超时时关闭
            allowCoreThreadTimeOut(true)
        }
    }

    /**
     * 低优先级执行器，用于处理耗时&不高优任务，这类任务一定要使用它（参考AsyncTask设计）
     */
    val lowExecutor by lazy {
        ThreadPoolExecutor(
            // 优化点 3/3: 核心线程数设置为 2（取代硬编码的 1），保证基础并发，同时不超过最大限制
            2,
            20.coerceAtLeast(AVAILABLE_PROCESSORS), // 保持最大线程数不变
            30,
            TimeUnit.SECONDS,
            SynchronousQueue(),
            NamedThreadFactory("global-low-thread", NORM_PRIORITY - 1)
        ).apply {
            rejectedExecutionHandler = RejectedExecutionHandler { r, _ ->
                // As a last ditch fallback, run it on an executor with an unbounded queue.
                // Create this executor lazily, hopefully almost never.
                lowBackupExecutor.execute(r)
            }
        }
    }

    val LOW: CoroutineDispatcher by lazy { lowExecutor.asCoroutineDispatcher() }

    val scheduledExecutor by lazy {
        Executors.newScheduledThreadPool(
            1,
            NamedThreadFactory("global-scheduled-thread", NORM_PRIORITY - 2)
        )
    }

    /**
     * 如果在当前线程会立即执行，不想立即执行的情况使用post
     */
    fun Handler.submit(runnable: Runnable, delay: Long = 0L) {
        if (delay == 0L && Looper.myLooper() === looper) {
            runnable.run()
            return
        }

        postDelayed(runnable, delay)
    }

    fun Handler.remove(runnable: Runnable) {
        removeCallbacks(runnable)
    }
}