package com.adealik.frame.base.frame


import androidx.annotation.CallSuper
import com.yuehai.util.language.collection.ConcurrentList
import com.yuehai.coroutine.coroutine.dispatcher.Dispatcher
import com.yuehai.coroutine.coroutine.dispatcher.Dispatcher.remove
import com.yuehai.coroutine.coroutine.dispatcher.Dispatcher.submit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.android.asCoroutineDispatcher

/**
 * Created by ratel on 2024/1/2.
 */

interface IListener

interface IBaseFrame<L : IListener> {
    fun addListener(l: L)
    fun removeListener(l: L)
    fun clearListeners()

    /**
     * 清理数据
     */
    fun clear()
}

open class BaseFrame<L : IListener> : IBaseFrame<L>, CoroutineScope {

    private val listeners = ConcurrentList<L>()
    val serialHandler = Dispatcher.highSerialHandler
    override val coroutineContext = SupervisorJob() + serialHandler.asCoroutineDispatcher()

    fun dispatch(c: (l: L) -> Unit) {
        listeners.dispatch { c(it) }
    }

    fun isListenerEmpty(): Boolean {
        return listeners.isEmpty()
    }

    @CallSuper
    override fun addListener(l: L) {
        listeners.add(l)
    }

    @CallSuper
    override fun removeListener(l: L) {
        listeners.remove(l)
    }

    fun runOnSerialHandler(runnable: Runnable, delay: Long = 0L) {
        serialHandler.submit(runnable, delay)
    }

    fun removeFromSerialHandler(runnable: Runnable) {
        serialHandler.remove(runnable)
    }

    /**
     * 很多Mgr都是单例，这里的clearListeners谨慎调用
     */
    override fun clearListeners() {
        listeners.clear()
    }

    override fun clear() {

    }

}