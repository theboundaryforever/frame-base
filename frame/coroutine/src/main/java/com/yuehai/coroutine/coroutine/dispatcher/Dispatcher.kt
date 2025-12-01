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
import java.util.concurrent.*

/**
 * 统一线程池 Dispatcher
 * 结构说明：
 * - UI 主线程 1 条
 * - 高优先级 HandlerThread（单线程序列化）
 * - 低优先级 HandlerThread（单线程序列化）
 * - 高优先级线程池（用于多数任务）
 * - 低优先级线程池（用于耗时&不重要任务，避免抢占 CPU）
 * - 低优先级 Backup（有界兜底，防止内存爆炸）
 */
object Dispatcher {

    private val AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors()

    private const val UI_CORE_POOL_SIZE = 1
    private const val HIGH_SERIAL_CORE_POOL_SIZE = 1

    /**
     * 正式版 IO/CPU Thread 数计算（不再恒为 2）
     *
     * 限制：
     * - 至少 2
     * - 最多 4（避免过度竞争）
     * - 自动根据设备性能调整
     */
    private val IO_CORE_POOL_SIZE = ((AVAILABLE_PROCESSORS - 1) / 2)
        .coerceAtLeast(2)
        .coerceAtMost(4)

    private val CPU_CORE_POOL_SIZE = (AVAILABLE_PROCESSORS - 1)
        .coerceAtLeast(2)
        .coerceAtMost(4)

    init {
        Log.d(
            "Dispatcher",
            "CPU=$AVAILABLE_PROCESSORS, IO_CORE_POOL_SIZE=$IO_CORE_POOL_SIZE, CPU_CORE_POOL_SIZE=$CPU_CORE_POOL_SIZE"
        )
    }

    // -----------------------------------------------------------------------
    // UI Dispatcher
    // -----------------------------------------------------------------------

    val UI: CoroutineDispatcher by lazy {
        Handler(Looper.getMainLooper()).asCoroutineDispatcher("ui")
    }

    // -----------------------------------------------------------------------
    // High-Serial 单线程序列化（核心场景）
    // -----------------------------------------------------------------------

    private val highSerialHandler: Handler by lazy {
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
        } else {
            highSerialHandler.postDelayed(runnable, delay)
        }
    }

    fun removeFromHighSerialThread(runnable: Runnable) {
        highSerialHandler.removeCallbacks(runnable)
    }

    val HIGH_SERIAL: CoroutineDispatcher by lazy { highSerialHandler.asCoroutineDispatcher() }

    // -----------------------------------------------------------------------
    // Low-Serial 单线程序列化（非核心序列化）
    // -----------------------------------------------------------------------

    private val lowSerialHandler: Handler by lazy {
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
        } else {
            lowSerialHandler.postDelayed(runnable, delay)
        }
    }

    fun removeFromLowSerialThread(runnable: Runnable) {
        lowSerialHandler.removeCallbacks(runnable)
    }

    val LOW_SERIAL: CoroutineDispatcher by lazy { lowSerialHandler.asCoroutineDispatcher() }

    // -----------------------------------------------------------------------
    // 高优先级线程池（用于多数任务，CPU 限制最多 4）
    // -----------------------------------------------------------------------

    val highExecutor: ExecutorService by lazy {
        Executors.newFixedThreadPool(
            CPU_CORE_POOL_SIZE,
            NamedThreadFactory("global-high-thread", NORM_PRIORITY)
        )
    }

    val HIGH: CoroutineDispatcher by lazy { highExecutor.asCoroutineDispatcher() }

    // -----------------------------------------------------------------------
    // 低优先级 Backup（有限界队列 + 低优先级）
    // -----------------------------------------------------------------------

    private val lowBackupExecutor by lazy {
        ThreadPoolExecutor(
            IO_CORE_POOL_SIZE,
            IO_CORE_POOL_SIZE,
            30,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(2000), // 有界：防止内存爆炸
            NamedThreadFactory("global-low-backup-thread", NORM_PRIORITY - 2)
        ).apply {
            allowCoreThreadTimeOut(true)
        }
    }

    // -----------------------------------------------------------------------
    // 低优先级线程池 - 有界队列 + 回压策略（不会 OOM）
    // -----------------------------------------------------------------------

    val lowExecutor by lazy {
        ThreadPoolExecutor(
            IO_CORE_POOL_SIZE,
            (AVAILABLE_PROCESSORS * 2).coerceAtLeast(4),
            30,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(300), // 有界队列
            NamedThreadFactory("global-low-thread", NORM_PRIORITY - 1),
            RejectedExecutionHandler { r, executor ->
                // 当 lowExecutor 饱和 → 先回压当前线程
                try {
                    r.run() // 回压策略（最安全）
                } catch (e: Throwable) {
                    // 如果依然失败 → 扔到 backup（backup 有界）
                    lowBackupExecutor.execute(r)
                }
            }
        ).apply {
            allowCoreThreadTimeOut(true)
        }
    }

    val LOW: CoroutineDispatcher by lazy { lowExecutor.asCoroutineDispatcher() }

    // -----------------------------------------------------------------------
    // Scheduled
    // -----------------------------------------------------------------------

    val scheduledExecutor by lazy {
        Executors.newScheduledThreadPool(
            1,
            NamedThreadFactory("global-scheduled-thread", NORM_PRIORITY - 2)
        )
    }

    // -----------------------------------------------------------------------
    // Handler 扩展
    // -----------------------------------------------------------------------

    fun Handler.submit(runnable: Runnable, delay: Long = 0L) {
        if (delay == 0L && Looper.myLooper() === looper) {
            runnable.run()
        } else {
            postDelayed(runnable, delay)
        }
    }

    fun Handler.remove(runnable: Runnable) {
        removeCallbacks(runnable)
    }
}
