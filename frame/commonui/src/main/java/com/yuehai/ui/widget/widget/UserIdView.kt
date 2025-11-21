package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.yuehai.data.collection.path.Constants
import com.yuehai.ui.R
import com.yuehai.ui.databinding.LayoutCommonLevelBinding
import com.yuehai.ui.widget.ext.hide
import com.yuehai.ui.widget.ext.loadUrl
import com.yuehai.ui.widget.util.copyToClipBoard
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.ext.gone
import com.yuehai.util.util.ext.show
import com.yuehai.util.util.getCompatColor
import com.yuehai.util.util.getCompatDrawable
import com.yuehai.util.util.getCompatString
import com.yuehai.util.util.toast.showToast

class UserIdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = LayoutCommonLevelBinding.inflate(LayoutInflater.from(context), this, true)


    init {
        binding.ivCopyIc.setOnClickListener {
            val prefix = getCompatString(R.string.common_id_label_2).cleanForRtl()
            val idNumber = binding.tvId.text.toString()
                .cleanForRtl()
                .trim()
                .removePrefix(prefix)
                .trim()


            if (this.context.copyToClipBoard(idNumber, idNumber)) {
                showToast(getCompatString(R.string.common_copy_success_tip))
            }
        }
    }

    fun String.cleanForRtl(): String {
        // 移除常见的 Unicode 双向控制字符
        return this.replace(Regex("[\\u200E\\u200F\\u202A-\\u202E]"), "")
    }

    fun setIDNumber(
        idNumber: String?,
        copy: Boolean = false,
        haveBackground: Boolean = true,
        textColor: Int? = null
    ) {
        if (haveBackground) {
            binding.tvId.background = getCompatDrawable(R.drawable.rank_id_shape_bg)
            binding.tvId.setPadding(6.dp(), 2.dp(), 6.dp(), 2.dp())
        } else {
            binding.tvId.background = null
            binding.tvId.setPadding(0, 0, 0, 0)
        }
        if (textColor != null) {
            binding.tvId.setTextColor(textColor)
        } else {
            binding.tvId.setTextColor(binding.tvId.textColors)
        }
        binding.clId.background = null

        binding.ivIdLabel.gone()
        if (copy) {
            binding.ivCopyIc.show()
        } else {
            binding.ivCopyIc.gone()
        }

        binding.tvId.text = idNumber ?: ""
    }

    fun setIDNumber2(idNumber: String?, copy: Boolean = false) {
        binding.tvId.background = null
        binding.tvId.setTextColor(getCompatColor(R.color.color_9BA1B6))
        binding.ivCopyIc.setImageResource(R.mipmap.common_id_copy_ic)
        binding.ivIdLabel.gone()
        binding.clId.background = null
        if (copy) {
            binding.ivCopyIc.show()
        } else {
            binding.ivCopyIc.gone()
        }

        binding.tvId.text = idNumber ?: ""
    }

    fun setBeautyNumber(
        beautyNumber: String?,
        copy: Boolean = false,
        resId: Int = R.mipmap.common_id_copy_2_ic,
        beautyBg: Int = R.drawable.rank_id_beauty_shape_bg
    ) {
        binding.ivIdLabel.show()
        binding.tvId.text = beautyNumber ?: ""
        binding.ivCopyIc.setImageResource(resId)
        binding.clId.background = getCompatDrawable(beautyBg)
        binding.tvId.setTextColor(getCompatColor(R.color.color_FFD98B2B))
        binding.ivIdLabel.show()
        if (copy) {
            binding.ivCopyIc.show()
        } else {
            binding.ivCopyIc.gone()

        }
    }

    fun setBeautyNumber2(
        beautyNumber: String?,
        copy: Boolean = false,
        resId: Int = R.mipmap.common_id_copy_ic
    ) {
        binding.tvId.text = beautyNumber ?: ""
        binding.ivCopyIc.setImageResource(resId)
        binding.clId.background = getCompatDrawable(R.drawable.rank_id_beauty_shape_bg)
        binding.tvId.setTextColor(getCompatColor(R.color.color_FFD98B2B))
        binding.ivIdLabel.show()
        if (copy) {
            binding.ivCopyIc.show()
        } else {
            binding.ivCopyIc.gone()
        }
    }


    fun setDialogInfo(
        idNumber: String? = "",
        beautyNumber: String? = "",
        countryUrl: String? = "",
        copy: Boolean = false
    ) {
        binding.ivCopyIc.setImageResource(R.mipmap.common_id_copy_5_ic)
        if (idNumber.isNullOrEmpty().not()) {
            binding.tvId.text = idNumber
            binding.tvId.setTextColor(getCompatColor(R.color.color_80ffffff))
            binding.tvId.background = null
            binding.ivIdLabel.gone()
        } else {
            binding.tvId.text = beautyNumber
            binding.ivCopyIc.setImageResource(R.mipmap.common_id_copy_2_ic)
            binding.ivIdLabel.show()
            binding.tvId.setTextColor(getCompatColor(R.color.color_FFD98B2B))
            binding.tvId.background = getCompatDrawable(R.drawable.rank_id_beauty_shape_bg)
        }

        binding.ivCountry.loadUrl(countryUrl ?: "", context)
        binding.ivCountry.show()
        binding.ivIdLabel.gone()
        if (copy) {
            binding.ivCopyIc.show()
        } else {
            binding.ivCopyIc.gone()
        }
    }

    fun setEmptyNumber() {
        binding.tvId.text = viewPlace
        binding.ivIdLabel.gone()
        binding.ivCopyIc.gone()
        binding.tvId.setTextColor(getCompatColor(R.color.color_FFFFFF))
        binding.tvId.background = null
        binding.root.background = null
    }
}
