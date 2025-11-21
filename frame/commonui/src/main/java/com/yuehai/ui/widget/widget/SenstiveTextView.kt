package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.yuehai.ui.R
import com.yuehai.ui.databinding.CommonSenstiveTextViewBinding
import androidx.core.content.withStyledAttributes
import com.yuehai.ui.widget.ext.hide
import com.yuehai.ui.widget.ext.show

class SenstiveTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: CommonSenstiveTextViewBinding =
        CommonSenstiveTextViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.withStyledAttributes(attrs, R.styleable.CustomInteractiveView) {
            val title = getString(R.styleable.CustomInteractiveView_customTitle) ?: ""
            binding.tvSenstiveTitle.text = title
            val customIcon = getBoolean(R.styleable.CustomInteractiveView_customIcon, false)

            if (customIcon) {
                binding.ivNext.show()
            } else {
                binding.ivNext.hide()
            }
        }
    }

    // 暴露公共方法来设置内容
    fun setText(text: String) {
        binding.tvSenstiveTitle.text = text

    }

    fun setIcon(iconResId: Int) {
        binding.ivIcon.setImageResource(iconResId)
    }
}