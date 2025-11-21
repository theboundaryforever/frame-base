package com.yuehai.ui.widget.recycleview.layoutmanager

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 支持多行竖直居中排列的 Grid LayoutManager
 * 横向从左排列，每列纵向居中显示
 * 配合 GridSpacingItemDecoration 使用
 */
class VerticalCenterGridLayoutManager(
    private val span: Int, // 每行列数
    private val spacingV: Int = 0 // 行间距
) : RecyclerView.LayoutManager() {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollHorizontally(): Boolean = false
    override fun canScrollVertically(): Boolean = false
    override fun isAutoMeasureEnabled(): Boolean = true

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }

        detachAndScrapAttachedViews(recycler)
        layoutChildren(recycler, state.itemCount)
    }

    private fun layoutChildren(recycler: RecyclerView.Recycler, itemCount: Int) {
        if (itemCount == 0) return

        val itemWidth = width / span

        // 1️⃣ 先测量每个 item 高度，计算每行最高高度
        val rowHeights = mutableListOf<Int>()
        var tempIndex = 0
        while (tempIndex < itemCount) {
            val countInLine = minOf(span, itemCount - tempIndex)
            var maxHeightInLine = 0
            for (i in 0 until countInLine) {
                val view = recycler.getViewForPosition(tempIndex + i)
                addView(view)
                measureChildWithMargins(view, 0, 0)
                maxHeightInLine = maxOf(maxHeightInLine, view.measuredHeight)
                detachAndScrapView(view, recycler)
            }
            rowHeights.add(maxHeightInLine)
            tempIndex += countInLine
        }

        val totalHeight =
            rowHeights.sum() + spacingV * (rowHeights.size - 1) // 总高度包含行间距
        var topOffset = (height - totalHeight) / 2 // 竖直居中
        var viewIndex = 0

        // 2️⃣ 布局每个 item
        for (rowHeight in rowHeights) {
            val remaining = itemCount - viewIndex
            val countInLine = minOf(span, remaining)
            var left = 0

            for (i in 0 until countInLine) {
                val itemView = recycler.getViewForPosition(viewIndex)
                addView(itemView)
                val wSpec = View.MeasureSpec.makeMeasureSpec(itemWidth, View.MeasureSpec.EXACTLY)
                val hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                itemView.measure(wSpec, hSpec)

                val top = topOffset + (rowHeight - itemView.measuredHeight) / 2 // 行内垂直居中
                val bottom = top + itemView.measuredHeight
                layoutDecorated(itemView, left, top, left + itemWidth, bottom)
                left += itemWidth
                viewIndex++
            }

            topOffset += rowHeight + spacingV
        }
    }
}