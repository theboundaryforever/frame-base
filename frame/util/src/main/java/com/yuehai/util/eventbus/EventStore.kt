package com.yuehai.util.eventbus

/**
 * 事件存储工具类（优化版）
 * 支持按 ID 分类存储多个事件 + bagId 单独事件存储
 */
object EventStore {

    // 普通事件：事件ID -> 事件列表
    private val eventMap = mutableMapOf<String, MutableList<Any>>()

    // 按 bagId 存储的独立事件：bagId -> 最新事件
    val bagIdEventMap = mutableMapOf<String, Any?>()

    private val lock = Any() // 用于同步

    /**
     * 添加普通事件（集合存储）
     */
    fun <T : Any> addEvent(id: String, event: T) {
        synchronized(lock) {
            val list = eventMap.getOrPut(id) { mutableListOf() }
            list.add(event)
        }
    }

    /**
     * 获取普通事件列表
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getEvents(id: String): List<T> {
        synchronized(lock) {
            return (eventMap[id]?.toList() as? List<T>) ?: emptyList()
        }
    }

    /**
     * 清空某个ID下的普通事件
     */
    fun clearEvents(id: String) {
        synchronized(lock) {
            eventMap[id]?.clear()
        }
    }

    /**
     * 清空所有普通事件
     */
    fun clearAll() {
        synchronized(lock) {
            eventMap.clear()
            bagIdEventMap.clear()
        }
    }

    /**
     * 是否存在某个ID的普通事件
     */
    fun hasEvents(id: String): Boolean {
        synchronized(lock) {
            return !eventMap[id].isNullOrEmpty()
        }
    }

    // ===================== bagId 事件 =====================

    /**
     * 设置 bagId 事件（覆盖旧事件）
     * @throws IllegalArgumentException 如果 bagId 为空或 null
     */
    fun <T : Any> addBagIdEvent(bagId: String, event: T) {
        require(bagId.isNotBlank()) { "bagId 不能为空或空字符串" }
        synchronized(lock) {
            bagIdEventMap[bagId] = event
        }
    }

    /**
     * 获取 bagId 事件，如果不存在返回 null
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getBagIdEvent(bagId: String): T? {
        if (bagId.isBlank()) return null
        synchronized(lock) {
            return bagIdEventMap[bagId] as? T
        }
    }

    /**
     * 移除指定 bagId 事件
     */
    fun removeBagIdEvent(bagId: String) {
        if (bagId.isBlank()) return
        synchronized(lock) {
            bagIdEventMap.remove(bagId)
        }
    }

    /**
     * 清空所有 bagId 事件
     */
    fun clearBagIdEvents() {
        synchronized(lock) {
            bagIdEventMap.clear()
        }
    }

    /**
     * 是否存在指定 bagId 事件
     */
    fun hasBagIdEvent(bagId: String): Boolean {
        if (bagId.isBlank()) return false
        synchronized(lock) {
            return bagIdEventMap.containsKey(bagId)
        }
    }
}
