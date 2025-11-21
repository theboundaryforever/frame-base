package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class MaxHeightRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): RecyclerView(context, attrs, defStyleAttr) {

    var maxHeightPx = 0

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var newHeightSpec = heightSpec
        if (maxHeightPx > 0) {
            newHeightSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthSpec, newHeightSpec)
    }
}
