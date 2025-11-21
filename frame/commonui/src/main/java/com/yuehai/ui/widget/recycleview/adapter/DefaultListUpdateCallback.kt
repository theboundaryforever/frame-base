package com.yuehai.ui.widget.recycleview.adapter

import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView

class DefaultListUpdateCallback(
    private val adapter: RecyclerView.Adapter<*>,
    private val preventFullUpdateAnim: Boolean,
    private val updateWithoutAnim: Boolean = false //更新不需要动画
) : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        if (updateWithoutAnim) {
            adapter.notifyDataSetChanged()
        } else if (preventFullUpdateAnim && count == adapter.itemCount) {
            adapter.notifyDataSetChanged()
        } else {
            adapter.notifyItemRangeInserted(position, count)
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        if (updateWithoutAnim) {
            adapter.notifyDataSetChanged()
        } else if (preventFullUpdateAnim && adapter.itemCount == 0) {
            adapter.notifyDataSetChanged()
        } else {
            adapter.notifyItemRangeRemoved(position, count)
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        adapter.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        if (updateWithoutAnim) {
            adapter.notifyDataSetChanged()
        } else if (preventFullUpdateAnim && count == adapter.itemCount) {
            adapter.notifyDataSetChanged()
        } else {
            adapter.notifyItemRangeChanged(position, count, payload)
        }
    }

}
