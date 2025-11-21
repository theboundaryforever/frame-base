package com.yuehai.ui.widget.recyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(
    private val dividerHeight: Int = 20,
    private val dividerColor: Int = Color.BLUE,
    private val horizontalPadding: Int = 0
) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dividerColor
        style = Paint.Style.FILL
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + horizontalPadding
        val right = parent.width - parent.paddingRight - horizontalPadding

        for (i in 0 until parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val top = child.bottom
            val bottom = top + dividerHeight
            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
        }
    }
}
