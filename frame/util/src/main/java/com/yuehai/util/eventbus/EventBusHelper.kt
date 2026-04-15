package com.yuehai.util.eventbus

import org.greenrobot.eventbus.EventBus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 终极稳固版 - 15轨道 EventBusHelper
 * 解决：极端高频（10ms内多条不同类型消息）并发下的覆盖问题
 */
object EventBusHelper {

    // --- 15条物理轨道定义 ---
    // 改为 internal 确保 EventBus 索引器在编译期能正确识别这些类
    internal class Track0<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track1<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track2<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track3<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track4<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track5<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track6<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track7<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track8<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track9<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track10<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track11<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track12<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track13<T>(type: String, data: T) : BaseEvent<T>(type, data)
    internal class Track14<T>(type: String, data: T) : BaseEvent<T>(type, data)

    // 原子计数器，确保多线程下轮询不冲突
    private val trackCounter = AtomicInteger(0)

    // 影子缓存：Key 是 BaseEvent.class，Value 是当前活跃在 EventBus 里的所有轨道实例
    private val stickyWrappers = ConcurrentHashMap<Class<*>, MutableList<Any>>()

    fun register(subscriber: Any?) {
        if (subscriber == null) return
        if (!EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().register(subscriber)
        }
    }

    fun unregister(subscriber: Any?) {
        if (subscriber == null) return
        if (EventBus.getDefault().isRegistered(subscriber)) {
            EventBus.getDefault().unregister(subscriber)
        }
    }

    fun post(event: BaseEvent<*>?) {
        if (event == null) return
        EventBus.getDefault().post(event)
    }

    /**
     * 发送粘性事件
     * 逻辑：通过 15 条不同的 Class 轨道轮询，避开 EventBus 内部 Map 的 Key 覆盖
     */
    fun <T> postSticky(event: BaseEvent<T>?) {
        if (event == null) return

        // 轮询分配 (0-14)
        val index = Math.abs(trackCounter.getAndIncrement() % 15)
        val wrapper = when (index) {
            0 -> Track0(event.eventType, event.data)
            1 -> Track1(event.eventType, event.data)
            2 -> Track2(event.eventType, event.data)
            3 -> Track3(event.eventType, event.data)
            4 -> Track4(event.eventType, event.data)
            5 -> Track5(event.eventType, event.data)
            6 -> Track6(event.eventType, event.data)
            7 -> Track7(event.eventType, event.data)
            8 -> Track8(event.eventType, event.data)
            9 -> Track9(event.eventType, event.data)
            10 -> Track10(event.eventType, event.data)
            11 -> Track11(event.eventType, event.data)
            12 -> Track12(event.eventType, event.data)
            13 -> Track13(event.eventType, event.data)
            else -> Track14(event.eventType, event.data)
        }

        // 管理影子缓存：同一 eventType 的消息，新的覆盖旧的，保持内存干净
        val clazz = BaseEvent::class.java
        val list = stickyWrappers.getOrPut(clazz) { mutableListOf() }
        synchronized(list) {
            list.removeAll { (it as? BaseEvent<*>)?.eventType == event.eventType }
            list.add(wrapper)
        }

        EventBus.getDefault().postSticky(wrapper)
    }

    /**
     * 移除所有 BaseEvent 相关的粘性轨道
     */
    fun <T> removeSticky(eventType: Class<T>?) {
        if (eventType == null) return

        // 移除原生位置
        val nativeSticky = EventBus.getDefault().getStickyEvent(eventType)
        if (nativeSticky != null) EventBus.getDefault().removeStickyEvent(nativeSticky)

        // 移除 15 条轨道中缓存的所有分身
        stickyWrappers[eventType]?.let { list ->
            synchronized(list) {
                list.forEach { EventBus.getDefault().removeStickyEvent(it) }
                list.clear()
            }
        }
    }

    /**
     * 移除单条（精准移除）
     * 建议在 onEvent 处理完逻辑后立即调用
     */
    fun removeStickyEvent(event: Any?) {
        if (event == null) return
        EventBus.getDefault().removeStickyEvent(event)

        stickyWrappers[BaseEvent::class.java]?.let { list ->
            synchronized(list) { list.remove(event) }
        }
    }
}