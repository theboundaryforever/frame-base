package com.yuehai.util.eventbus

import org.greenrobot.eventbus.EventBus

object EventBusHelper {
    // 统一注册
    fun register(subscriber: Any?) {
        if (!EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().register(subscriber)
        }
    }

    // 统一反注册
    fun unregister(subscriber: Any?) {
        if (EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().unregister(subscriber)
        }
    }

    // 发送普通事件
    fun post(event: BaseEvent<*>?) {
        EventBus.getDefault().post(event)
    }

    // 发送粘性事件（Sticky Event）
    fun postSticky(event: BaseEvent<*>?) {
        EventBus.getDefault().postSticky(event)
    }

    // 移除指定类型的粘性事件
    fun <T> removeSticky(eventType: Class<T>?) {
        val stickyEvent: T = EventBus.getDefault().getStickyEvent(eventType)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }
    }
}
