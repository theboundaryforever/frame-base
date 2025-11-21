package com.yuehai.ui.widget.widget.radio

import android.content.Context
import android.util.AttributeSet

class SquareRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.radioButtonStyle
) : androidx.appcompat.widget.AppCompatRadioButton(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = measuredWidth.coerceAtLeast(measuredHeight)
        setMeasuredDimension(size, size)
    }
}
