package com.yuehai.ui.widget.recycleview.layoutmanager

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * 麦位网格布局
 * 支持第一行自定义显示数量，其余行自动居中
 */
class Mic12CenterGridLayoutManger(
    private val span: Int = 5,          // 每行最多显示5个
    private val firstRowCount: Int = 2  // 第一行显示数量，默认两个
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

        var viewIndex = 0
        var top = 0
        val itemWidth = width / span
        var itemHeight = 0

        while (viewIndex < state.itemCount) {
            val countInLine = if (viewIndex == 0) firstRowCount else span

            val leftPositions = if (viewIndex == 0) {
                // 第一行两个，分别在第二个和第四个格子
                listOf(1 * itemWidth, 3 * itemWidth)
            } else {
                // 其他行从左到右
                (0 until countInLine).map { it * itemWidth }
            }

            for (i in leftPositions.indices) {
                if (viewIndex >= state.itemCount) break
                itemHeight = addViewAndLayout(recycler, viewIndex, itemWidth, leftPositions[i], top)
                viewIndex++
            }

            top += itemHeight
        }
    }

    private fun addViewAndLayout(
        recycler: RecyclerView.Recycler,
        index: Int,
        itemWidth: Int,
        left: Int,
        top: Int
    ): Int {
        val itemView = recycler.getViewForPosition(index)
        addView(itemView)
        measureChildWithMargins(itemView, 0, 0)
        val itemHeight = itemView.measuredHeight
        layoutDecorated(itemView, left, top, left + itemWidth, top + itemHeight)
        return itemHeight
    }
}
