package com.yuehai.ui.widget.recycleview.layoutmanager

import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 我的勋章 布局管理器
 * 所有行居中显示
 */
class CenterGridLayoutManger(
    private val span: Int // 每行最多放多少个Item
) : RecyclerView.LayoutManager() {

    private val mOrientationHelper =
        OrientationHelper.createOrientationHelper(this, RecyclerView.HORIZONTAL)

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
        super.onLayoutChildren(recycler, state)
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        detachAndScrapAttachedViews(recycler)

        onLayout(recycler, state.itemCount)
        recycleChildren(recycler)
    }

    private fun onLayout(recycler: RecyclerView.Recycler, itemCount: Int) {
        val itemWidth = width / span

        var viewIndex = 0
        var top = 0
        var itemHeight = 0

        while (viewIndex < itemCount) {
            val remaining = itemCount - viewIndex
            val countInLine = if (remaining >= span) span else remaining

            // ✅ 居中每一行
            var left = (width - countInLine * itemWidth) / 2

            for (i in 0 until countInLine) {
                itemHeight = addViewAndLayout(recycler, viewIndex, itemWidth, left, top)
                viewIndex++
                left += itemWidth
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

        val wSpec = getChildMeasureSpec(
            mOrientationHelper.totalSpace,
            widthMode,
            0,
            itemWidth,
            false
        )
        val hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        itemView.measure(wSpec, hSpec)

        layoutDecorated(
            itemView,
            left,
            top,
            left + itemWidth,
            top + itemView.measuredHeight
        )
        return itemView.measuredHeight
    }

    private fun initGuideLine(): Map<Int, List<Int>> {
        val blockCount = (2 * span - 1) * 2
        val blockWith = width / blockCount
        val locationMap = hashMapOf<Int, MutableList<Int>>()
        for (i in 1..span) {
            val points = locationMap[i] ?: mutableListOf()
            points.add(((i - 1) * 2 + 1) * blockWith)
            locationMap[i] = points
        }
        return locationMap
    }

    private fun getViewLocation(viewIndex: Int, itemCount: Int): Point {
        if (viewIndex < span - 1) return Point(0, viewIndex)
        val line1Count = itemCount % span
        val row = (viewIndex - line1Count) / span + 1
        val col = (viewIndex - line1Count) % span
        return Point(row, col)
    }

    private fun recycleChildren(recycler: RecyclerView.Recycler) {
        val scrapList = recycler.scrapList
        for (holder in scrapList) {
            removeView(holder.itemView)
            recycler.recycleView(holder.itemView)
        }
    }
}
