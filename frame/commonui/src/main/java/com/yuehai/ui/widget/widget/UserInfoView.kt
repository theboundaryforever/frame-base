package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.yuehai.data.collection.path.login.SEX
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonUserInfoViewBinding
import com.yuehai.ui.widget.ext.gone
import com.yuehai.ui.widget.ext.loadUrl
import com.yuehai.ui.widget.ext.setLevelNameColor
import com.yuehai.ui.widget.ext.show
import com.yuehai.ui.widget.ext.subName
import com.yuehai.util.util.getCompatColor

const val viewPlace = "------"

class UserInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyleAttr) {
    private val binding =
        CommonUserInfoViewBinding.inflate(LayoutInflater.from(context), this, true)

    private fun updateUserInfo(
        nickname: String?,
        ivSex: Int,
        country: String?,
        textSize: Float?,
        textColor: Int = 0,
        vipLevel: Int = 0
    ) {
        binding.tvName.text = nickname.subName()
        binding.tvName.setLevelNameColor(vipLevel, textColor)
        binding.ivSex.setImageResource(
            if (ivSex == SEX.MALE.sex) R.mipmap.sex_male_ic else R.mipmap.sex_female_ic
        )
        if (textSize != null) {
            binding.tvName.textSize = textSize
        }
        if (textColor != 0) {
            binding.tvName.setTextColor(textColor)
        }
        if (!country.isNullOrEmpty()) {
            binding.ivCountry.loadUrl(country ?: "", binding.ivCountry.context)
        }

    }

    private fun updateUserInfo2(
        nickname: String?,
        ivSex: Int,
        country: String?,
        textSize: Float?,
        vipLevel: Int = 0,
        textColor: Int = getCompatColor(R.color.color_16151A)
    ) {
        binding.tvName.setTextColor(textColor)
        binding.tvName.setLevelNameColor(vipLevel)
        binding.tvName.text = nickname.subName()
        binding.ivSex.setImageResource(
            if (ivSex == SEX.MALE.sex) R.mipmap.sex_male_ic else R.mipmap.sex_female_ic
        )
        if (textSize != null) {
            binding.tvName.textSize = textSize
        }
        if (country.isNullOrEmpty()) {
            binding.ivCountry.gone()
        } else {
            binding.ivCountry.show()
        }
        binding.ivCountry.loadUrl(country ?: "", binding.ivCountry.context)
    }

    fun setUserInfo(
        nickname: String? = null,
        ivSex: Int = 0,
        country: String? = null,
        textColor: Int = 0,
        vipLevel: Int = 0
    ) {
        updateUserInfo(nickname, ivSex, country, null, textColor)
    }

    fun setUserInfo2(
        nickname: String? = null,
        ivSex: Int = 0,
        country: String? = null,
        roomOwner: Boolean = false,
        roomManager: Boolean = false,
        vipLevel: Int = 0,
        textColor: Int = 0,
    ) {
        updateUserInfo2(nickname, ivSex, country, null, vipLevel, textColor)
        if (roomOwner) {
            binding.ivRoleLabel.show()
            binding.ivRoleLabel.setImageResource(R.mipmap.room_user_info_operateion_owner_ic)
        } else if (roomManager) {
            binding.ivRoleLabel.show()
            binding.ivRoleLabel.setImageResource(R.mipmap.room_user_info_operation_manager_ic)
        } else {
            binding.ivRoleLabel.gone()
        }
    }

    fun setUserInfoEmpty() {
        binding.tvName.text = viewPlace
        binding.ivCountry.gone()
        binding.ivSex.gone()
    }

    fun setSmallUserInfo(
        nickname: String? = null,
        ivSex: Int = 0,
        country: String? = null,
        textSize: Float = 12f
    ) {
        updateUserInfo(nickname, ivSex, country, textSize)
    }

    fun setUserInfo3(
        nickname: String? = null,
        textColor: Int = 0,
        vipLevel: Int = 0
    ) {
        binding.tvName.text = nickname.subName()
        binding.tvName.setLevelNameColor(vipLevel, textColor)
    }

}