package com.yuehai.util.language.collection

import java.util.concurrent.CopyOnWriteArrayList


class ConcurrentList<L> {

    private val list = CopyOnWriteArrayList<L>()

    fun getCopyList(): List<L> = ArrayList(list)

    fun add(l: L) {
        if (!list.contains(l)) {
            list.add(l)
        }
    }

    fun add(index: Int, l: L) {
        list.add(index, l)
    }

    fun addAll(l: List<L>) {
        l.forEach {
            if (!list.contains(it)) {
                list.add(it)
            }
        }
    }

    fun remove(l: L) {
        list.remove(l)
    }

    fun removeAll(l: List<L>) {
        list.removeAll(l.toSet())
    }

    fun removeLastOrNull() {
        if (list.isNotEmpty()) {
            list.removeAt(list.lastIndex)
        }
    }

    fun update(l: L, predicate: (L) -> Boolean): Int {
        val index = list.indexOfFirst(predicate)
        if (index >= 0) {
            list[index] = l
        }
        return index
    }

    fun dispatch(c: (l: L) -> Unit) {
        // 快照遍历，避免并发修改
        list.toList().forEach(c)
    }

    fun clear() {
        list.clear()
    }

    fun clearAndAddAll(l: List<L>) {
        list.clear()
        addAll(l)
    }

    fun isEmpty() = list.isEmpty()

    fun size(): Int = list.size

    override fun toString(): String = "$list"
}