package com.yuehai.ui.widget.recycleview.adapter

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "MultiTypeListAdapter"

open class MultiTypeListAdapter<T : Any>(
    diffCallback: DiffUtil.ItemCallback<T> = DefaultDiffCallback(),
    private val preventFullUpdateAnim: Boolean = true, //设置为true可以防止submitList(forceUpdate=true)时因list动效引起的闪烁问题
    private val updateWithoutAnim: Boolean = false, //更新不需要动画
) : ExtMultiTypeAdapter() {

    companion object {
        var backgroundExecutorService = ThreadPoolExecutor(
            2,
            2,
            1,
            TimeUnit.MINUTES,
            LinkedBlockingQueue()
        ).apply { allowCoreThreadTimeOut(true) }
    }

    private val itemsToSubmit = mutableListOf<T>()

    final override var items: List<Any>
        get() = differ.currentList
        set(value) {}

    private var submittingCount = AtomicInteger(0)

    private val listener = AsyncListDiffer.ListListener<T> { previousList, currentList ->
        this@MultiTypeListAdapter.onCurrentListChanged(previousList, currentList)
        onListChangedListener?.invoke(previousList, currentList)
    }

    var onListChangedListener: ((previousList: List<T>, currentList: List<T>) -> Unit)? = null

    ////主线程使用默认的Looper.getMainLooper()
    open val differ: AsyncListDiffer<T> by lazy(LazyThreadSafetyMode.NONE) {
        AsyncListDiffer(
            DefaultListUpdateCallback(this, preventFullUpdateAnim, updateWithoutAnim),
            AsyncDifferConfig.Builder(diffCallback)
                .setBackgroundThreadExecutor(backgroundExecutorService)
                .build()
        ).apply {
            addListListener(listener)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getCurrentList(): List<T> {
        return differ.currentList
    }

    fun submitList(list: List<T>, forceUpdate: Boolean = false, committed: () -> Unit = {}) {
        if (forceUpdate) {
            itemsToSubmit.clear()
            differ.submitList(null) {
                itemsToSubmit.addAll(list)
                submitInner(committed)
            }
        } else {
            itemsToSubmit.clear()
            itemsToSubmit.addAll(list)
            submitInner(committed)
        }
    }

    private fun submitInner(committed: () -> Unit = {}) {

        //重新生成一个实例，否则列表不更新
        differ.submitList(itemsToSubmit.toMutableList()) {
            committed()

        }
    }

    open fun onCurrentListChanged(previousList: List<T>, currentList: List<T>) {}

    fun addList(list: List<T>) {
        itemsToSubmit.addAll(list)
        submitInner()
    }

    fun insert(index: Int, list: List<T>) {
        itemsToSubmit.addAll(index, list)
        submitInner()
    }

    fun insert(index: Int, data: T) {
        itemsToSubmit.add(index, data)
        submitInner()
    }

    fun addFirst(data: T) {
        insert(0, data)
    }

    fun addEnd(data: T) {
        itemsToSubmit.add(data)
        submitInner()
    }

    fun removeRange(index: Int, num: Int) {
        if (num > 0 && index >= 0 && index + num <= itemsToSubmit.size) {
            itemsToSubmit.subList(index, num + index)
                .clear()
            submitInner()
        }
    }

    fun remove(data: T) {
        removeRange(findIndex(data), 1)
    }

    fun removeFirst(num: Int = 1) {
        removeRange(0, num)
    }

    fun removeLast(num: Int = 1) {
        removeRange(itemsToSubmit.size - num, num)
    }

    fun replace(index: Int, data: T) {
        itemsToSubmit.removeAt(index)
        itemsToSubmit.add(index, data)
        submitInner()
    }

    fun clear() {
        itemsToSubmit.clear()
        submitInner()
    }

    fun getItem(position: Int): T? {
        if (position < 0 || position >= itemsToSubmit.size) {
            return null
        }
        return itemsToSubmit[position]
    }

    fun size(): Int = itemsToSubmit.size

    fun contains(data: T): Boolean {
        return findIndex(data) != -1
    }

    private fun findIndex(data: T): Int {
        return itemsToSubmit.indexOf(data)
    }

}
