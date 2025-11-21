package com.yuehai.ui.widget.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.yuehai.ui.R
import com.yuehai.ui.databinding.LayoutCommonTopBarBinding
import com.yuehai.ui.widget.ext.getActivity
import androidx.core.content.withStyledAttributes

class CommonTopBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    var backCallback: (() -> Unit)? = null
    var binding = LayoutCommonTopBarBinding.inflate(LayoutInflater.from(context), this)

    init {
        initView()
        context.withStyledAttributes(
            attrs,
            R.styleable.CommonTopBar
        ) {
            initTypedArray(this)
        }
    }

    private fun initView() {
        binding.viewBackClickArea.setOnClickListener {
            backCallback?.let {
                backCallback?.invoke()
                return@setOnClickListener
            }
            finishCurrentActivity()
        }
    }

    private fun finishCurrentActivity() {
        getActivity()?.finish()
    }

    private fun initTypedArray(typedArray: TypedArray) {
        typedArray.getString(R.styleable.CommonTopBar_top_bar_title)?.let { setTitle(it) }
        setBackDrawable(
            typedArray.getResourceId(
                R.styleable.CommonTopBar_back_drawable,
                R.mipmap.back_ic
            )
        )
        setRightDrawable(typedArray.getResourceId(R.styleable.CommonTopBar_right_drawable, 0))
        setTitleTextColor(
            typedArray.getResourceId(
                R.styleable.CommonTopBar_title_color,
                R.color.color_222222
            )
        )
        binding.root.setBackgroundResource(
            typedArray.getResourceId(
                R.styleable.CommonTopBar_bar_background,
                R.color.color_FFFFFF
            )
        )
    }

    fun setTitle(titleStr: String) {
        binding.title.text = titleStr
    }

    fun setTitle(@StringRes titleStrRes: Int) {
        binding.title.setText(titleStrRes)
    }

    fun setTitleAlpha(percent: Float) {
        binding.title.alpha = percent
    }

    fun setTitleTextColor(@ColorRes color: Int) {
        binding.title.setTextColor(resources.getColor(color))
    }

    fun setTitleTextSize(size: Float) {
        binding.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    fun setSubtitleTextColor(@ColorRes color: Int) {
        binding.subtitle.setTextColor(resources.getColor(color))
    }

    fun getTitleView(): TextView {
        return binding.title
    }

    fun getSubtitleView(): TextView {
        return binding.subtitle
    }

    fun setSubtitleTextSize(size: Float) {
        binding.subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    fun setTitleTextBold() {
        binding.title.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
    }

    fun setSubtitle(titleStr: String) {
        binding.subtitle.text = titleStr
    }

    fun setSubtitleVisible(visible: Boolean) {
        binding.subtitle.visibility = if (visible) VISIBLE else GONE
    }

    fun setBackDrawable(@DrawableRes drawableId: Int) {
        binding.back.setImageResource(drawableId)
    }

    fun setRightDrawable(@DrawableRes drawableId: Int) {
        if (drawableId == 0) {
            return
        }
        binding.right.visibility = View.VISIBLE
        binding.right.setImageResource(drawableId)
    }

    fun hideRightDrawable() {
        binding.right.visibility = View.GONE
    }

    fun getRightView(): View {
        return binding.right
    }

    fun setRight1Drawable(@DrawableRes drawableId: Int) {
        if (drawableId == 0) {
            return
        }
        binding.right1.visibility = View.VISIBLE
        binding.right1.setImageResource(drawableId)
    }

    fun hideRight1Drawable() {
        binding.right1.visibility = View.GONE
    }

    fun getRight1View(): View {
        return binding.right1
    }

}