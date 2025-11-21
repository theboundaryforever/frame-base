package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import com.bumptech.glide.Glide
import com.yuehai.ui.databinding.CommonUserAvatarBinding
import com.yuehai.ui.widget.ext.gone
import com.yuehai.ui.widget.ext.loadCircleUrl
import com.yuehai.ui.widget.ext.show
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.svga.playSVGA
import com.yuehai.ui.R
import com.yuehai.ui.widget.ext.hide

class UserAvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding = CommonUserAvatarBinding.inflate(LayoutInflater.from(context), this, true)

    private var avatarMargin = 26.dp()
    private var avatarPadding = 0
    private var avatarWidth = 0
    private var avatarHeight = 0

    init {
        clipChildren = false
        clipToPadding = false
        context.obtainStyledAttributes(attrs, R.styleable.UserAvatarView).apply {
            avatarMargin =
                getDimensionPixelSize(R.styleable.UserAvatarView_avatarMargin, avatarMargin)
            avatarPadding =
                getDimensionPixelSize(R.styleable.UserAvatarView_avatarPadding, avatarPadding)
            avatarWidth = getDimensionPixelSize(R.styleable.UserAvatarView_avatarWidth, 0)
            avatarHeight = getDimensionPixelSize(R.styleable.UserAvatarView_avatarHeight, 0)
            recycle()
        }

        setPadding(avatarPadding, avatarPadding, avatarPadding, avatarPadding)
        updateAvatarSize()
    }

    /** 更新头像尺寸 */
    private fun updateAvatarSize() {
        val lp = binding.ivAvatar.layoutParams
        if (avatarWidth > 0) lp.width = avatarWidth
        if (avatarHeight > 0) lp.height = avatarHeight
        binding.ivAvatar.layoutParams = lp
    }

    /** 更新头饰尺寸 */
    private fun updateHeadwearSize() {
        val baseWidth =
            if (avatarWidth > 0) avatarWidth else binding.ivAvatar.width.takeIf { it > 0 }
                ?: binding.ivAvatar.measuredWidth
        val baseHeight =
            if (avatarHeight > 0) avatarHeight else binding.ivAvatar.height.takeIf { it > 0 }
                ?: binding.ivAvatar.measuredHeight

        val width = baseWidth + avatarMargin * 2
        val height = baseHeight + avatarMargin * 2

        listOf(binding.svgAvatarHeader, binding.ivAvatarHeader).forEach { header ->
            header.layoutParams = header.layoutParams.apply {
                this.width = width
                this.height = height
            }
            header.requestLayout()
        }
    }

    /** 设置头像和头饰 */
    fun setAvatar(url: String, headUrl: String? = null, margin: Int = avatarMargin) {
        avatarMargin = margin

        // 设置头像和 Margin
        binding.ivAvatar.loadCircleUrl(url, context)
        (binding.ivAvatar.layoutParams as? MarginLayoutParams)?.apply {
            setMargins(avatarMargin, avatarMargin, avatarMargin, avatarMargin)
        }
        binding.ivAvatar.layoutParams = binding.ivAvatar.layoutParams

        // 更新尺寸
        updateAvatarSize()
        updateHeadwearSize()

        // 默认隐藏头饰
        binding.ivAvatarHeader.hide()
        binding.svgAvatarHeader.hide()

        headUrl?.let { headerUrl ->
            if (headerUrl.endsWith(".svga", true)) {
                binding.svgAvatarHeader.show()
                updateHeadwearSize()
                binding.svgAvatarHeader.clear()
                // 先重置 layoutParams 再播放
                binding.svgAvatarHeader.layoutParams = binding.svgAvatarHeader.layoutParams.apply {
                    width = binding.ivAvatar.width + avatarMargin * 2
                    height = binding.ivAvatar.height + avatarMargin * 2
                }
                binding.svgAvatarHeader.playSVGA(headerUrl, -1)
            } else {
                binding.ivAvatarHeader.show()
                updateHeadwearSize()
                Glide.with(binding.ivAvatarHeader.context)
                    .load(headerUrl)
                    .into(binding.ivAvatarHeader)
            }
        }
    }

    fun setAvatarMargin(margin: Int) {
        avatarMargin = margin
        (binding.ivAvatar.layoutParams as? MarginLayoutParams)?.apply {
            setMargins(margin, margin, margin, margin)
        }
        binding.ivAvatar.layoutParams = binding.ivAvatar.layoutParams
        updateHeadwearSize()
    }

    fun setUserProfileAvatarMargin(margin: Int) {
        avatarMargin = margin
        (binding.ivAvatar.layoutParams as? MarginLayoutParams)?.apply {
            setMargins(margin, (-10).dp(), margin, margin)
        }
        binding.ivAvatar.layoutParams = binding.ivAvatar.layoutParams
        updateHeadwearSize()
    }

    fun setAvatarSize(width: Int, height: Int) {
        avatarWidth = width
        avatarHeight = height
        updateAvatarSize()
        updateHeadwearSize()
    }
}
