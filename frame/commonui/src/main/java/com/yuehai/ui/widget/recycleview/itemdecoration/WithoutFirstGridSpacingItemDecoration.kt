package com.yuehai.ui.widget.recycleview.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView



/**
 * GridLayoutManager 间距装饰，支持不影响第一个item，
 * 支持边缘间距(includeEdge)，
 * 解决左右边距不对称问题，采用浮点计算防止截断误差。
 */
class WithoutFirstGridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val topSpacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) {
            outRect.set(0, 0, 0, 0)
            return
        }
        if (position == 0) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val column = position % spanCount

        val doubleSpacing = spacing * 2

        if (includeEdge) {
            // 左边第一列，左间距 2倍 spacing，右间距 spacing/2
            if (column == 0) {
                outRect.left = doubleSpacing
                outRect.right = spacing / 2
            }
            // 右边最后一列，右间距 2倍 spacing，左间距 spacing/2
            else if (column == spanCount - 1) {
                outRect.left = spacing / 2
                outRect.right = doubleSpacing
            }
            // 中间列，左右间距都 spacing/2
            else {
                outRect.left = spacing / 2
                outRect.right = spacing / 2
            }

            outRect.top = if (position < spanCount) 0 else topSpacing
        } else {
            // 不包含边缘时，左右间距都用 spacing/2
            if (column == 0) {
                outRect.left = doubleSpacing
                outRect.right = spacing / 2
            } else if (column == spanCount - 1) {
                outRect.left = spacing / 2
                outRect.right = doubleSpacing
            } else {
                outRect.left = spacing / 2
                outRect.right = spacing / 2
            }

            outRect.top = if (position < spanCount) 0 else topSpacing
        }
    }

}
