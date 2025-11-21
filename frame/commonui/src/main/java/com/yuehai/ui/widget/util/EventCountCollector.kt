import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

object SmartEventManager {

    private val eventMap = mutableMapOf<String, MutableStateFlow<Any?>>()
    private val eventSharedMap = mutableMapOf<String, MutableSharedFlow<Any>>() // 新增事件流
    private val unreadCountMap = mutableMapOf<String, Long>()
    private val activeCollectorMap = mutableMapOf<String, Long>()
    private val lock = Any()

    private val _totalUnreadFlow = MutableStateFlow(0L)
    val totalUnreadFlow: StateFlow<Long> = _totalUnreadFlow

    private fun updateTotalUnread() {
        _totalUnreadFlow.value = unreadCountMap.values.sum()
    }

    /**
     * 设置未读数（覆盖模式），并发送事件
     */
    fun post(tag: String, unread: Long = 1, setUnread: Boolean = true) {
        synchronized(lock) {
            if (setUnread) {
                unreadCountMap[tag] = unread
                updateTotalUnread()
            }
            // 通知 StateFlow 观察者（状态）
            val flow = getOrCreateFlow(tag)
            @Suppress("UNCHECKED_CAST")
            (flow as MutableStateFlow<Any?>).value = System.currentTimeMillis() // 保证每次变化

            // 通知 SharedFlow 观察者（事件）
            getOrCreateSharedFlow(tag).tryEmit(Any())
        }
    }

    fun getUnreadCount(tag: String): Long {
        return synchronized(lock) {
            unreadCountMap[tag] ?: 0
        }
    }

    fun clearUnread(tag: String) {
        synchronized(lock) {
            unreadCountMap.remove(tag)
            updateTotalUnread()
        }
    }

    fun clearAll() {
        synchronized(lock) {
            unreadCountMap.clear()
            eventMap.clear()
            eventSharedMap.clear()
            activeCollectorMap.clear()
            updateTotalUnread()
        }
    }

    fun onCollectorStart(tag: String) {
        synchronized(lock) {
            activeCollectorMap[tag] = (activeCollectorMap[tag] ?: 0) + 1
            if (unreadCountMap.remove(tag) != null) {
                updateTotalUnread()
            }
        }
    }

    fun onCollectorStop(tag: String) {
        synchronized(lock) {
            activeCollectorMap[tag] = (activeCollectorMap[tag] ?: 1) - 1
        }
    }

    fun getTotalUnread(): Long {
        return synchronized(lock) {
            unreadCountMap.values.sum()
        }
    }

    private fun getOrCreateFlow(tag: String): MutableStateFlow<Any?> {
        return eventMap.getOrPut(tag) { MutableStateFlow(null) }
    }

    private fun getOrCreateSharedFlow(tag: String): MutableSharedFlow<Any> {
        return eventSharedMap.getOrPut(tag) { MutableSharedFlow(extraBufferCapacity = 1) }
    }

    /**
     * 观察状态（StateFlow）
     */
    fun <T> observe(tag: String): StateFlow<T?> {
        return getOrCreateFlow(tag) as StateFlow<T?>
    }

    /**
     * 观察事件（SharedFlow）
     */
    fun observeEvent(tag: String): SharedFlow<Any> {
        return getOrCreateSharedFlow(tag)
    }
}
