package com.yuehai.ui.widget.recycleview.adapter

import androidx.recyclerview.widget.RecyclerView
import com.yuehai.ui.widget.recycleview.adapter.multitype.MultiTypeAdapter

/**
 * 无限循环
 */
open class ScrollMultiTypeAdapter : MultiTypeAdapter() {

    private var canScroll = true

    fun setCanScroll(scroll: Boolean) {
        canScroll = scroll
    }

    override fun getItemCount(): Int {
        return if (canScroll) {
            Int.MAX_VALUE
        } else {
            items.size
        }
    }

    override fun getItemId(position: Int): Long {
        if (items.isEmpty()) {
            return RecyclerView.NO_ID
        }
        val item = items[position % items.size]
        val itemViewType = getItemViewType(position)
        return types.getType<Any>(itemViewType).delegate.getItemId(item)
    }

    override fun getItemViewType(position: Int): Int {
        if (items.isEmpty()) {
            return 0
        }
        return indexInTypesOf(position, items[position % items.size])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        if (items.isEmpty()) {
            return
        }
        val item = items[position % items.size]
        getOutDelegateByViewHolder(holder).onBindViewHolder(holder, item, payloads)
    }

    fun getRealItemCount(): Int {
        return items.size
    }

}