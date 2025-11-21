package com.yuehai.ui.widget.recycleview.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration(
    private val verticalSpaceHeight: Int,
    private val lastSpaceHeight: Int = verticalSpaceHeight,
    private val firstSpaceHeight: Int = 0
) :
    RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val totalCount = parent.adapter!!.itemCount
        if (position == 0) {
            outRect.top = firstSpaceHeight
        }
        if (position == totalCount - 1) {
            outRect.bottom = lastSpaceHeight
        } else {
            outRect.bottom = verticalSpaceHeight
        }
    }
}