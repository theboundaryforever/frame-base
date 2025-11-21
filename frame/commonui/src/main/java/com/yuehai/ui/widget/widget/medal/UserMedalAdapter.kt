package com.yuehai.ui.widget.widget.medal

import android.util.Log
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonAdapterUserInfoMedalBinding
import com.yuehai.ui.widget.ext.loadPureUrl
import com.yuehai.ui.widget.ext.loadUrl
import com.yuehai.ui.widget.ext.show
import com.yuehai.util.util.svga.playPGA


//用户资料卡勋章
class UserMedalAdapter(val viewWidth: Int) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.common_adapter_user_info_medal) {
    override fun convert(
        holder: BaseViewHolder,
        item: String
    ) {
        val binding = CommonAdapterUserInfoMedalBinding.bind(holder.itemView)
        with(binding) {
            if (viewWidth > 0) {
                var currentParams = ivMedal.layoutParams
                currentParams.width = viewWidth
                currentParams.height = viewWidth
                ivMedal.layoutParams = currentParams

                var currentParams2 = ivLevel.layoutParams
                currentParams2.width = viewWidth
                currentParams2.height = viewWidth
                ivLevel.layoutParams = currentParams2

            }
            if (item.endsWith(".pag")) {
                ivMedal.show()
                ivMedal.playPGA(item)
            } else {
                ivLevel.show()
                ivLevel.loadPureUrl(item, ivLevel.context)
            }

        }

    }

}