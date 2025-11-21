package com.yuehai.ui.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import com.bumptech.glide.Glide
import com.yuehai.ui.widget.ext.gone
import com.yuehai.ui.widget.ext.loadCircleUrl
import com.yuehai.ui.widget.ext.show
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.svga.playSVGA
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonUserVipAvatarBinding
import com.yuehai.ui.widget.ext.hide
import com.yuehai.ui.widget.ext.loadCircleUrlWithContext
import com.yuehai.util.AppUtil

@SuppressLint("CustomViewStyleable")
class UserVipAvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding =
        CommonUserVipAvatarBinding.inflate(LayoutInflater.from(context), this, true)

    private var avatarMargin = 26.dp()
    private var avatarPadding = 0
    private var avatarWidth = 0
    private var avatarHeight = 0

    init {
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

    /** 更新头饰尺寸（不受头像 margin/padding 影响） */
    private fun updateHeadwearSize() {
        val baseWidth =
            if (avatarWidth > 0) avatarWidth else binding.ivAvatar.width.takeIf { it > 0 }
                ?: binding.ivAvatar.measuredWidth
        val baseHeight =
            if (avatarHeight > 0) avatarHeight else binding.ivAvatar.height.takeIf { it > 0 }
                ?: binding.ivAvatar.measuredHeight

        if (baseWidth == 0 || baseHeight == 0) return

        val width = baseWidth + avatarMargin * 2
        val height = baseHeight + avatarMargin * 2

        listOf(binding.svgAvatarHeader, binding.ivAvatarHeader).forEach { header ->
            val lp = (header.layoutParams as LayoutParams).apply {
                this.width = width
                this.height = height
                // 保证居中于父容器
                this.topToTop = LayoutParams.PARENT_ID
                this.bottomToBottom = LayoutParams.PARENT_ID
                this.startToStart = LayoutParams.PARENT_ID
                this.endToEnd = LayoutParams.PARENT_ID
            }
            header.layoutParams = lp
        }
    }

    /** 设置头像和头饰 */
    fun setAvatar2(url: String, headUrl: String? = null, margin: Int = avatarMargin) {
        avatarMargin = margin

        // 设置头像
        binding.ivAvatar.loadCircleUrl(url)
        (binding.ivAvatar.layoutParams as? MarginLayoutParams)?.apply {
            setMargins(avatarMargin, avatarMargin, avatarMargin, avatarMargin)
        }
        binding.ivAvatar.layoutParams = binding.ivAvatar.layoutParams

        // 更新尺寸
        updateAvatarSize()
//        binding.ivAvatar.doOnLayout {
//            updateHeadwearSize()
//        }

        // 默认隐藏头饰
        if (headUrl.isNullOrEmpty()) {
            binding.ivAvatarHeader.hide()
            binding.svgAvatarHeader.hide()
        } else {
            binding.ivAvatar.doOnLayout {
                updateHeadwearSize()
                if (headUrl.endsWith(".svga", true)) {
                    binding.svgAvatarHeader.show()
                    binding.svgAvatarHeader.clear()
                    binding.svgAvatarHeader.playSVGA(headUrl, -1)
                } else {
                    binding.ivAvatarHeader.show()
                    Glide.with(binding.ivAvatarHeader)
                        .load(headUrl)
                        .into(binding.ivAvatarHeader)
                }
            }
        }
    }

    fun setAvatar(url: String, headUrl: String? = null, margin: Int = avatarMargin) {
        avatarMargin = margin

        // 1. 清理和隐藏（优秀，保留此逻辑避免复用错乱）
        Glide.with(binding.ivAvatarHeader).clear(binding.ivAvatarHeader)
        binding.ivAvatarHeader.hide()
        binding.svgAvatarHeader.clear()
        binding.svgAvatarHeader.hide()

        // 2. 设置头像（主图）
        binding.ivAvatar.loadCircleUrlWithContext(url, AppUtil.appContext)


        // 3. 更新边距和强制请求布局
        (binding.ivAvatar.layoutParams as? MarginLayoutParams)?.apply {
            setMargins(avatarMargin, avatarMargin, avatarMargin, avatarMargin)
        }
        binding.ivAvatar.requestLayout() // ⚠️ 关键：强制 View 重新计算布局

        // 4. 更新头像尺寸
        updateAvatarSize()

        // 5. ⚠️ 优化加载时机和尺寸更新：
        //    将 updateHeadwearSize 放在外部，只用 doOnLayout 来处理初次布局或真正发生尺寸变化

        // 默认执行一次尺寸更新，应对静态情况
        updateHeadwearSize()

        if (!headUrl.isNullOrEmpty()) {
            // 如果 View 尚未布局完成，使用 doOnLayout 保证尺寸更新和加载
            if (binding.ivAvatar.isLayoutRequested) {
                binding.ivAvatar.doOnLayout {
                    updateHeadwearSize() // 再次确保尺寸正确
                    loadHeadwear(headUrl)
                }
            } else {
                // 如果 View 已经布局完成，立即加载头饰
                loadHeadwear(headUrl)
            }
        }
    }

    // 提取一个私有方法来处理头饰加载，避免代码重复
    private fun loadHeadwear(headUrl: String) {
        if (headUrl.endsWith(".svga", true)) {
            binding.svgAvatarHeader.show()
            binding.svgAvatarHeader.playSVGA(headUrl, -1)
        } else {
            binding.ivAvatarHeader.show()
            Glide.with(AppUtil.appContext)
                .load(headUrl)
                .into(binding.ivAvatarHeader)
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

    fun setAvatarSize(width: Int, height: Int) {
        avatarWidth = width
        avatarHeight = height
        updateAvatarSize()
        updateHeadwearSize()
    }
}
