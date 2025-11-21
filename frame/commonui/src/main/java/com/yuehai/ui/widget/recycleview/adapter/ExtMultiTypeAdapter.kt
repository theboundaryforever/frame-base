package com.yuehai.ui.widget.recycleview.adapter

import com.yuehai.ui.widget.recycleview.adapter.multitype.MultiTypeAdapter

open class ExtMultiTypeAdapter : MultiTypeAdapter() {

    inline fun <reified T> notifyItemChanged(list: List<T>, compare: (old: T, new: T) -> Boolean) {
        val positions = arrayListOf<Int>()
        items.forEachIndexed { index, any ->
            val old = any as? T
            if (old != null) {
                list.forEach {
                    if (compare(old, it)) {
                        positions.add(index)
                    }
                }
            }
            if (positions.size == list.size) {
                return@forEachIndexed
            }
        }
        positions.forEach {
            notifyItemChanged(it)
        }
    }

    inline fun <reified T> notifyItemChanged(compare: (item: T) -> Boolean) {
        val positions = arrayListOf<Int>()
        items.forEachIndexed { index, any ->
            val item = any as? T
            if (item != null) {
                if (compare(item)) {
                    positions.add(index)
                    return@forEachIndexed
                }
            }
        }
        positions.forEach {
            notifyItemChanged(it)
        }
    }

    inline fun <reified T> notifyFirstItemChanged(compare: (item: T) -> Boolean) {
        val index = items.indexOfFirst {
            val item = it as? T
            if (item != null) compare(it) else false
        }
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

}