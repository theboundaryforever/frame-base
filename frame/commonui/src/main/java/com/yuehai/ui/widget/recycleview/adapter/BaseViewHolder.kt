package com.yuehai.ui.widget.recycleview.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.yuehai.ui.widget.recycleview.adapter.withheader.BaseItemData

/**
 * Created by suruijia on 2019/8/7
 * Comment:
 *
 * modified by yucan on 2020/6/2
 * Comment: All itemViewHolder must extend this to implement UI update
 */
abstract class BaseViewHolder<T : BaseItemData, VB : ViewBinding>(protected val mViewBinding: VB) :
    RecyclerView.ViewHolder(mViewBinding.root) {
    companion object {
        private const val TAG = "BaseViewHolder"
    }

    lateinit var mBaseAdapter: BaseRecyclerAdapter

    protected val mContext: Context = itemView.context
    var attachFragment: Fragment? = null

    protected var mPosition = 0

    protected fun getPageId(): String? {
        return ""
    }

    open fun initView() {}

    abstract fun updateItem(data: T, position: Int)

    fun updateItemParse(data: BaseItemData?, position: Int) {
        mPosition = position
        try {
            (data as? T)?.let {
                updateItem(it, position)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateItemParse error, class: $javaClass, position: $position")
        }
    }

    fun updateItemParse(data: BaseItemData?, position: Int, payloads: MutableList<Any?>) {
        mPosition = position
        val needFullRefresh = try {
            (data as? T)?.let {
                updateViewPartly(it, position, payloads)
            } ?: true
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder error, class: $javaClass, position: $position")
            true
        }
        if (needFullRefresh) {
            updateItemParse(data, position)
        }
    }

    /**
     * update part view with payloads
     * @return return is need to do a "full" refresh
     */
    protected open fun updateViewPartly(
        data: T, position: Int, payloads: List<Any?>
    ): Boolean {
        return true
    }

    interface BaseHolderProxy {
        fun newHolderInstance(inflater: LayoutInflater, parent: ViewGroup): BaseViewHolder<*, *>

        fun getLayoutId(): Int
    }
}