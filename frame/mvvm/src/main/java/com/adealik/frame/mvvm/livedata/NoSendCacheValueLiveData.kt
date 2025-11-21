package com.adealik.frame.mvvm.livedata

import androidx.lifecycle.*
import com.adealik.frame.mvvm.disposable.Disposable
import com.adealik.frame.mvvm.disposable.RunnableDisposable
import com.adealik.frame.mvvm.util.runOnUiThread
import com.adealik.frame.mvvm.disposable.bind


open class NoSendCacheValueLiveData<T> : MutableLiveData<EventWrapper<T>>() {

    open fun send(event: T) = runOnUiThread {
        val eventWrapper = EventWrapper(event)
        value = eventWrapper
        eventWrapper.hasBeenHandled = true
    }

    /**
     * 由于默认[LiveData]的激活时间onStart->onStop，**在[PublishData]中使用此方法有可能造成消息丢失**,
     * 因为[publish]方法会将消息同步标记为已处理。除非很清楚你的消息只会在onStart->onStop之间发送,
     * 否则建议使用[observeAlive], 该方法可以在注册之后, [onDestroy] 之前始终接收消息
     */
    open fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<T>): Disposable {
        val mediator = createUnwrapMediator()
        mediator.observe(lifecycleOwner, observer)
        return RunnableDisposable {
            mediator.removeObserver(observer)
        }
    }

    open fun observe(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit): Disposable {
        return observe(lifecycleOwner, Observer<T> { observer(it) })
    }

    fun observeDisposable(observer: Observer<T>): Disposable {
        val mediator = createUnwrapMediator()
        mediator.observeForever(observer)
        return RunnableDisposable {
            mediator.removeObserver(observer)
        }
    }

    fun observeDisposable(observer: (T) -> Unit): Disposable {
        return observeDisposable(Observer { observer(it) })
    }

    /**
     * 在onDestroy之前始终处于激活状态，没有onStart才激活的限制
     */
    open fun observeAlive(lifecycleOwner: LifecycleOwner, onUpdate: (T) -> Unit) {
        this.observeDisposable(onUpdate).bind(lifecycleOwner.lifecycle)
    }

    /**
     * 获取上一次事件缓存值，可能为空
     */
    fun peekEvent(): T? {
        return value?.peekContent()
    }

    /**
     * 新建一个中间层. 用来筛掉那些已经被处理过的事件. 其实相当于注册一个 forever 的 observer
     */
    private fun createUnwrapMediator(): MediatorLiveData<T> {
        val mediator = MediatorLiveData<T>()
        mediator.addSource(this) { event ->
            if (!event.hasBeenHandled) {
                mediator.value = event.peekContent()
            }
        }
        return mediator
    }
}

open class EventWrapper<out T>(private val content: T) {

    var hasBeenHandled = false
        internal set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}