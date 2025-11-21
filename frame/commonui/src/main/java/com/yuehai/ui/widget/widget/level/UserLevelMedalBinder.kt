package com.yuehai.ui.widget.widget.level

import android.view.LayoutInflater
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevel2Binding
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevelBinding
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevelMedal2Binding
import com.yuehai.ui.databinding.CommonAdapterUserInfoLevelMedalBinding
import com.yuehai.ui.databinding.CommonAdapterUserInfoMedalBinding
import com.yuehai.ui.widget.ext.loadImageWithOriginalSize
import com.yuehai.ui.widget.recycleview.adapter.BindingViewHolder
import com.yuehai.ui.widget.recycleview.adapter.multitype.ItemViewBinder
import com.yuehai.ui.widget.widget.LevelMedalViewInfo
import com.yuehai.ui.widget.widget.LevelViewInfo
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.ext.gone
import com.yuehai.util.util.ext.show
import com.yuehai.util.util.svga.playPGA


//用户勋章
class UserLevelMedalBinder(val viewPgaWidth: Int = 20.dp()) :

    ItemViewBinder<LevelMedalViewInfo, BindingViewHolder<CommonAdapterUserInfoLevelMedal2Binding>>() {


    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): BindingViewHolder<CommonAdapterUserInfoLevelMedal2Binding> {
        val binding = CommonAdapterUserInfoLevelMedal2Binding.inflate(inflater, parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BindingViewHolder<CommonAdapterUserInfoLevelMedal2Binding>,
        item: LevelMedalViewInfo
    ) {
        with(holder.binding) {

            val currentParams = ivMedal.layoutParams
            currentParams.width = viewPgaWidth
            currentParams.height = viewPgaWidth
            ivMedal.layoutParams = currentParams
            ivMedal.playPGA(item.levelUrl)
            ivMedal.show()
            ivLevel.gone()
        }
    }

}