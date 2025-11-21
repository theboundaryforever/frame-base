package com.yuehai.ui.widget.recycleview.layoutmanager

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val horizontalSpace: Int,
    private val verticalSpace: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            left = horizontalSpace
            right = horizontalSpace
            top = verticalSpace
            bottom = verticalSpace
        }
    }
}