package com.yuehai.ui.widget.recycleview.adapter

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.yuehai.ui.widget.recycleview.adapter.withheader.BaseItemData
import java.util.*

/**
 * Created by suruijia on 2019/8/7
 * Comment: Adapter for RecyclerView, easy to use and flexible to extend.
 * Note: Do not open the adapter class. Generally, use the adapter to set or update data.
 *
 * modified by yucan on 2020/6/2
 * change [BaseRecyclerAdapter] implement to easy understand [BaseViewHolder]
 */
open class BaseRecyclerAdapter(context: Context, private val fragment: Fragment? = null) : RecyclerView.Adapter<BaseViewHolder<*, *>>() {

    private val mTypeHolderProxy = SparseArray<BaseViewHolder.BaseHolderProxy>()
    private var mData: ArrayList<BaseItemData> = ArrayList()
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    val data: List<BaseItemData>
        get() = mData

    fun registerHolder(holderProxy: BaseViewHolder.BaseHolderProxy) {
        mTypeHolderProxy.put(holderProxy.getLayoutId(), holderProxy)
    }

    /**
     * 局部刷新RecyclerView
     */
    fun submitData(data: List<BaseItemData>, diffCallback: DiffUtil.Callback) {
        val diffResult = DiffUtil.calculateDiff(diffCallback, false)
        mData.clear()
        mData.addAll(data)
        diffResult.dispatchUpdatesTo(this)
    }

    open fun setData(data: List<BaseItemData>) {
        mData.clear()
        addData(data)
    }

    open fun addData(data: List<BaseItemData>) {
        mData.addAll(data)
        notifyDataSetChanged()
    }

    open fun addData(data: BaseItemData) {
        mData.add(data)
        notifyDataSetChanged()
    }

    fun insertData(position: Int, data: BaseItemData) {
        mData.add(position, data)
        notifyItemInserted(position)
    }

    fun removeData(position: Int) {
        if (position < 0) return
        if (position >= mData.size) return
        mData.removeAt(position)
        notifyItemRemoved(position)
    }

    fun moveData(fromPosition: Int, toPosition: Int) {
        if (fromPosition < 0 || toPosition < 0) return
        if (fromPosition > mData.size || toPosition > mData.size) return
        mData.add(toPosition, mData.removeAt(fromPosition))
        notifyItemMoved(fromPosition, toPosition)
    }

    fun clearData() {
        mData.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<out BaseItemData, out ViewBinding> {
        val holderProxy = mTypeHolderProxy.get(viewType)
        return holderProxy.newHolderInstance(mInflater, parent).apply {
            attachFragment = fragment
            mBaseAdapter = this@BaseRecyclerAdapter
            initView()
        }
    }

    open fun getItemData(position: Int): BaseItemData? {
        return if (position >= mData.size || position < 0) {
            null
        } else mData[position]
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*, *>, position: Int) {
        holder.updateItemParse(getItemData(position), position)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*, *>, position: Int, payloads: MutableList<Any?>) {
        holder.updateItemParse(getItemData(position), position, payloads)
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun getItemViewType(position: Int): Int {
        return getItemData(position)?.getItemType(position) ?: -1
    }

    companion object {
        private val TAG = BaseRecyclerAdapter::class.java.simpleName
    }

}
