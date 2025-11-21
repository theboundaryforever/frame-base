package com.yuehai.ui.widget.widget.level

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yuehai.data.collection.path.json.toJsonErrorNull
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevel2Binding
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevelBinding
import com.yuehai.ui.widget.ext.loadImageWithOriginalSize
import com.yuehai.ui.widget.recycleview.adapter.BindingViewHolder
import com.yuehai.ui.widget.recycleview.adapter.multitype.ItemViewBinder
import com.yuehai.ui.widget.widget.LevelViewInfo

class UserLevelAdapter2 :
    ItemViewBinder<LevelViewInfo, BindingViewHolder<CommonAdapterUserInfoLevel2Binding>>() {
    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): BindingViewHolder<CommonAdapterUserInfoLevel2Binding> {
        val binding = CommonAdapterUserInfoLevel2Binding.inflate(inflater, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BindingViewHolder<CommonAdapterUserInfoLevel2Binding>,
        item: LevelViewInfo
    ) {
        val binding = CommonAdapterUserInfoLevelBinding.bind(holder.itemView)
        with(binding) {
            loadImageWithOriginalSize(item, binding.ivLevel)
        }
    }

}