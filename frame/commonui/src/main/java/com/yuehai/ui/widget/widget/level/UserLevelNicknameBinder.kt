package com.yuehai.ui.widget.widget.level

import android.view.LayoutInflater
import android.view.ViewGroup
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevelNameBinding
import com.yuehai.ui.widget.ext.setLevelNameColor
import com.yuehai.ui.widget.ext.subName
import com.yuehai.ui.widget.recycleview.adapter.BindingViewHolder
import com.yuehai.ui.widget.recycleview.adapter.multitype.ItemViewBinder
import com.yuehai.ui.widget.widget.UserInfoLevel
import com.yuehai.util.util.getCompatColor

class UserLevelNicknameBinder :
    ItemViewBinder<UserInfoLevel, BindingViewHolder<CommonAdapterUserInfoLevelNameBinding>>() {
    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): BindingViewHolder<CommonAdapterUserInfoLevelNameBinding> {
        val binding = CommonAdapterUserInfoLevelNameBinding.inflate(inflater, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BindingViewHolder<CommonAdapterUserInfoLevelNameBinding>,
        item: UserInfoLevel
    ) {
        with(holder.binding) {
            tvLevelName.text = item.info.nickname.subName(10)
            tvLevelName.setLevelNameColor(item.info.vipLevel, getCompatColor(R.color.color_FFFFFF))
        }
    }
}