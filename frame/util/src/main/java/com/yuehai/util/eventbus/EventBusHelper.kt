package com.yuehai.util.eventbus

import org.greenrobot.eventbus.EventBus
import java.util.concurrent.ConcurrentHashMap

/**
 * EventBus 助手类
 * 优化点：
 * 1. 解决 10ms 内相同 BaseEvent 类型粘性事件被覆盖的问题
 * 2. 解决匿名内部类无法通过 Class 对象精确移除的问题
 */
object EventBusHelper {

    // 缓存所有发送出去的匿名粘性事件对象，方便后续统一移除
    // Key: 原始类的 Class (如 BaseEvent::class.java), Value: 当前内存中的匿名实例列表
    private val stickyWrappers = ConcurrentHashMap<Class<*>, MutableList<Any>>()

    // 统一注册
    fun register(subscriber: Any?) {
        if (subscriber == null) return
        if (!EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().register(subscriber)
        }
    }

    // 统一反注册
    fun unregister(subscriber: Any?) {
        if (subscriber == null) return
        if (EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().unregister(subscriber)
        }
    }

    // 发送普通事件
    fun post(event: BaseEvent<*>?) {
        if (event == null) return
        EventBus.getDefault().post(event)
    }

    /**
     * 发送粘性事件
     * 通过创建匿名内部类包装，确保每次 postSticky 在 EventBus 看来都是不同的 Class 类型，
     * 从而避免 10ms 内相同 BaseEvent 被 Map 覆盖的问题。
     */
    fun <T> postSticky(event: BaseEvent<T>?) {
        if (event == null) return

        // 1. 创建匿名包装对象
        val wrapper = object : BaseEvent<T>(event.eventType, event.data) {}

        // 2. 存入影子缓存，以便 removeSticky 时能找到它
        val clazz = BaseEvent::class.java
        val list = stickyWrappers.getOrPut(clazz) { mutableListOf() }
        synchronized(list) {
            // 如果业务要求同一 eventType 只能有一个粘性，可以在这里过滤
            list.add(wrapper)
        }

        // 3. 发送给 EventBus
        EventBus.getDefault().postSticky(wrapper)
    }

    /**
     * 移除指定类型的粘性事件
     * 优化：不仅移除原生 Class 匹配的事件，还移除所有通过此 Helper 发送的匿名包装事件
     */
    fun <T> removeSticky(eventType: Class<T>?) {
        if (eventType == null) return

        // 1. 移除 EventBus 原生匹配的事件
        val stickyEvent = EventBus.getDefault().getStickyEvent(eventType)
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent)
        }

        // 2. 移除影子缓存中对应的匿名包装事件（重点解决 BaseEvent 覆盖失效问题）
        val list = stickyWrappers[eventType]
        if (list != null) {
            synchronized(list) {
                list.forEach {
                    EventBus.getDefault().removeStickyEvent(it)
                }
                list.clear()
            }
            stickyWrappers.remove(eventType)
        }
    }

    /**
     * 移除特定的粘性事件实例
     * 建议在 @Subscribe 处理完粘性事件后立即调用此方法
     */
    fun removeStickyEvent(event: Any?) {
        if (event == null) return
        EventBus.getDefault().removeStickyEvent(event)

        // 同时清理影子缓存中的该对象
        stickyWrappers[BaseEvent::class.java]?.let { list ->
            synchronized(list) { list.remove(event) }
        }
    }
}