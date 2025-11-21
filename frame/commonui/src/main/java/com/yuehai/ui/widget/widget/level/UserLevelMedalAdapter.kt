package com.yuehai.ui.widget.widget.level

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonAdapterUserInfoMedalBinding
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.ext.gone
import com.yuehai.util.util.ext.show
import com.yuehai.util.util.svga.playPGA


//用户等级，勋章
class UserLevelMedalAdapter(val viewPgaWidth: Int = 20.dp()) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.common_adapter_user_info_level_medal) {
    override fun convert(
        holder: BaseViewHolder,
        item: String
    ) {
        val binding = CommonAdapterUserInfoMedalBinding.bind(holder.itemView)
        with(binding) {
            if (item.endsWith(".pag")) {
                var currentParams = ivMedal.layoutParams
                currentParams.width = viewPgaWidth
                currentParams.height = viewPgaWidth
                ivMedal.layoutParams = currentParams
                ivMedal.playPGA(item)
                ivMedal.show()
                ivLevel.gone()
            } else {
                ivMedal.gone()
                var currentParams = ivLevel.layoutParams
                currentParams.width = 44.dp()
                currentParams.height = 18.dp()
                ivLevel.show()
                ivLevel.layoutParams = currentParams
                Glide.with(ivLevel.context).load(item).into(ivLevel)
            }

        }

    }

}