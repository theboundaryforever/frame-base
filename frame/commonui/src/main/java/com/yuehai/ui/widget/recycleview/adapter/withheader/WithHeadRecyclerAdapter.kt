package com.yuehai.ui.widget.recycleview.adapter.withheader

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.yuehai.ui.widget.recycleview.adapter.BaseRecyclerAdapter
import com.yuehai.ui.widget.recycleview.adapter.BaseViewHolder

class WithHeadRecyclerAdapter(context: Context, fragment: Fragment? = null) : BaseRecyclerAdapter(context, fragment) {
    private var mHeaderView: View? = null

    fun setHeaderView(headerView: View) {
        mHeaderView = headerView
        notifyItemInserted(0)
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (mHeaderView != null) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        if (mHeaderView != null) {
            return if (position == 0) {
                TYPE_HEADER
            } else {
                super.getItemViewType(position - 1)
            }
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out BaseItemData, out ViewBinding> {
        if (viewType == TYPE_HEADER) {
            mHeaderView?.let {
                return HeaderViewHolder(it)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*, *>, position: Int) {
        if (mHeaderView != null) {
            if (getItemViewType(position) == TYPE_HEADER) return
            super.onBindViewHolder(holder, position - 1)
            return
        }
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*, *>, position: Int, payloads: MutableList<Any?>) {
        if (mHeaderView != null) {
            if (getItemViewType(position) == TYPE_HEADER) return
            super.onBindViewHolder(holder, position - 1, payloads)
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            val oriSpanSizeLookup = manager.spanSizeLookup
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == TYPE_HEADER)
                        manager.spanCount
                    else oriSpanSizeLookup.getSpanSize(position)
                }
            }
        }
    }

    companion object {
        const val TYPE_HEADER = 0
    }

    private class HeaderViewHolder(headerView: View) : BaseViewHolder<BaseItemData, ViewBinding>(ViewBinding { headerView }) {
        override fun updateItem(data: BaseItemData, position: Int) {
            //ntd.
        }
    }
}