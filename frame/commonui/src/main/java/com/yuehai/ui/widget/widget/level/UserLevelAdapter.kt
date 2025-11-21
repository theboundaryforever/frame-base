package com.yuehai.ui.widget.widget.level

import android.view.LayoutInflater
import android.view.ViewGroup
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevelBinding
import com.yuehai.ui.widget.ext.loadImageWithOriginalSize
import com.yuehai.ui.widget.recycleview.adapter.BindingViewHolder
import com.yuehai.ui.widget.recycleview.adapter.multitype.ItemViewBinder
import com.yuehai.ui.widget.widget.LevelViewInfo

class UserLevelAdapter :
    ItemViewBinder<LevelViewInfo, BindingViewHolder<CommonAdapterUserInfoLevelBinding>>() {
    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): BindingViewHolder<CommonAdapterUserInfoLevelBinding> {
        val binding = CommonAdapterUserInfoLevelBinding.inflate(inflater, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BindingViewHolder<CommonAdapterUserInfoLevelBinding>,
        item: LevelViewInfo
    ) {
        val binding = CommonAdapterUserInfoLevelBinding.bind(holder.itemView)
        with(binding) {
            loadImageWithOriginalSize(item, binding.ivLevel)
        }
    }

}