package com.yuehai.ui.widget.recycleview.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 *  处理GridSpacingItemDecoration对于占满一整行的item在另起一行时依旧存在左间距的问题
 */
class GridSpacingMultiSpanSizeItemDecoration(
    private val spanCount: Int,
    private val spacingH: Int,
    private val spacingV: Int,
    private val includeEdge: Boolean,
    private val extraBottom: Int = -1,
    private val extraTop: Int = -1
) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val totalCount = state.itemCount
        val position = parent.getChildAdapterPosition(view) // item position
        val column = (parent.layoutManager as? GridLayoutManager)?.spanSizeLookup?.getSpanIndex(
            position,
            spanCount
        ) ?: (position % spanCount) // item column
        val row = (parent.layoutManager as? GridLayoutManager)?.spanSizeLookup?.getSpanGroupIndex(
            position,
            spanCount
        ) ?: (position < spanCount)


        val isRtl = parent.layoutDirection == View.LAYOUT_DIRECTION_RTL
        val isSingleColumnItem =
            (view.layoutParams as? GridLayoutManager.LayoutParams)?.spanSize == spanCount

        if (includeEdge) {
            val left =
                spacingH - column * spacingH / spanCount // spacing - column * ((1f / spanCount) * spacing)
            val right =
                (column + 1) * spacingH / spanCount // (column + 1) * ((1f / spanCount) * spacing)
            outRect.left = if (isRtl) right else left
            outRect.right = if (isRtl) left else right

            if (row == 0) { // top edge
                outRect.top = spacingV
            }
            outRect.bottom = spacingV // item bottom
            if (isSingleColumnItem) {
                outRect.left = spacingH
                outRect.right = spacingH
            }
        } else {
            val left = column * spacingH / spanCount // column * ((1f / spanCount) * spacing)
            val right =
                spacingH - (column + 1) * spacingH / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            outRect.left = if (isRtl) right else left
            outRect.right = if (isRtl) left else right
            if (row != 0) {
                outRect.top = spacingV // item top
            }
            if (isSingleColumnItem) {
                outRect.left = 0
                outRect.right = 0
            }
        }

        if (extraBottom != -1) {
            val temp = totalCount % spanCount
            val lastRowCount = if (temp == 0) spanCount else temp
            val isLastRow = totalCount - 1 - position < lastRowCount
            if (isLastRow) {
                outRect.bottom += extraBottom
            }
        }

        if (extraTop != -1) {
            val isFirstRow = row == 0
            if (isFirstRow) {
                outRect.top += extraTop
            }
        }
    }
}