package com.yuehai.ui.widget.recycleview.layoutmanager

import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yuehai.util.util.ext.dp
import kotlin.math.max

private const val TAG = "RocketDynamicLayout"

class RocketDynamicLayoutManager : RecyclerView.LayoutManager() {
    private val GAP_PX = 12.dp() // 水平和垂直间隙
    private val BOTTOM_SAFETY_MARGIN_PX = 15

    private val allItemRects = mutableListOf<Rect>()
    private var contentTotalHeight: Int = 0
    private var lastWidthSpec: Int = 0
    private var lastHeightSpec: Int = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollHorizontally() = false
    override fun canScrollVertically() = false

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        lastWidthSpec = widthSpec
        lastHeightSpec = heightSpec

        val desiredWidth = View.MeasureSpec.getSize(widthSpec)

        if (View.MeasureSpec.getMode(heightSpec) == View.MeasureSpec.EXACTLY) {
            setMeasuredDimension(desiredWidth, View.MeasureSpec.getSize(heightSpec))
        } else {
            setMeasuredDimension(desiredWidth, contentTotalHeight)
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0 || width == 0) {
            removeAndRecycleAllViews(recycler)
            contentTotalHeight = 0
            return
        }

        val itemCount = state.itemCount
        val contentWidth = width - paddingLeft - paddingRight

        detachAndScrapAttachedViews(recycler)
        allItemRects.clear()

        // 1. 预测量所有子项（拿固有宽高）
        val childViews = (0 until itemCount).map { recycler.getViewForPosition(it) }
        val itemSizes = mutableListOf<Pair<Int, Int>>()

        val widthSpecForMeasure =
            View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.AT_MOST)
        val heightSpecForMeasure =
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

        childViews.forEachIndexed { index, view ->
            addView(view)
            measureChildWithMargins(view, widthSpecForMeasure, heightSpecForMeasure)
            val w = getDecoratedMeasuredWidth(view)
            val h = getDecoratedMeasuredHeight(view)
            itemSizes.add(Pair(w, h))
            detachAndScrapView(view, recycler)
        }

        // 2️⃣ 布局逻辑
        contentTotalHeight = when (itemCount) {
            1 -> layoutOneItem(itemSizes, contentWidth)
            2 -> layoutTwoItems(itemSizes, contentWidth)

            // ⭐ 修复版：Item 3 的第一项按固有宽度居中
            3 -> layoutThreeItems(itemSizes, contentWidth)

            4 -> layoutFourItems(itemSizes, contentWidth)
            5 -> layoutFiveItems(itemSizes, contentWidth)
            else -> layoutFallback(itemSizes, contentWidth)
        }

        reMeasureSelf()

        // 计算垂直居中偏移
        val availableHeight = height - paddingTop - paddingBottom
        var verticalOffset = 0
        if (availableHeight > contentTotalHeight) {
            verticalOffset = (availableHeight - contentTotalHeight) / 2
        }

        // 4️⃣ 真正开始布局
        for (i in 0 until itemCount) {
            val view = childViews[i]
            addView(view)
            val rect = allItemRects[i]

            val widthSpec = View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY)
            measureChildWithMargins(view, widthSpec, heightSpec)

            val finalLeft = rect.left + paddingLeft
            val finalTop = rect.top + verticalOffset + paddingTop
            val finalRight = rect.right + paddingLeft
            val finalBottom = rect.bottom + verticalOffset + paddingTop

            layoutDecorated(view, finalLeft, finalTop, finalRight, finalBottom)
        }
    }

    private fun reMeasureSelf() {
        val widthSize = View.MeasureSpec.getSize(lastWidthSpec)
        val heightMode = View.MeasureSpec.getMode(lastHeightSpec)
        if (heightMode != View.MeasureSpec.EXACTLY) {
            setMeasuredDimension(widthSize, contentTotalHeight)
        }
    }

    // ========================== 1 ITEM ==========================
    private fun layoutOneItem(itemSizes: List<Pair<Int, Int>>, contentWidth: Int): Int {
        val w0 = itemSizes[0].first
        val h0 = itemSizes[0].second

        val leftOffset = max(0, (contentWidth - w0) / 2)

        allItemRects.add(Rect(leftOffset, 0, leftOffset + w0, h0))
        return h0 + BOTTOM_SAFETY_MARGIN_PX
    }

    // ========================== 2 ITEMS ==========================
    private fun layoutTwoItems(itemSizes: List<Pair<Int, Int>>, contentWidth: Int): Int {
        val (w0, h0) = itemSizes[0]
        val (w1, h1) = itemSizes[1]

        val rowWidth = w0 + w1 + GAP_PX
        val rowHeight = max(h0, h1)

        val leftOffset = max(0, (contentWidth - rowWidth) / 2)

        allItemRects.add(Rect(leftOffset, 0, leftOffset + w0, rowHeight))
        allItemRects.add(Rect(leftOffset + w0 + GAP_PX, 0, leftOffset + w0 + GAP_PX + w1, rowHeight))

        return rowHeight + BOTTOM_SAFETY_MARGIN_PX
    }

    // ========================== ⭐ 3 ITEMS（修复版） ==========================
    private fun layoutThreeItems(itemSizes: List<Pair<Int, Int>>, contentWidth: Int): Int {
        var top = 0

        // 第一行：1 个 item（按自身宽度居中）
        val w0 = itemSizes[0].first
        val h0 = itemSizes[0].second

        val left0 = max(0, (contentWidth - w0) / 2)
        allItemRects.add(Rect(left0, top, left0 + w0, top + h0))

        top += h0 + GAP_PX

        // 第二行：2 个 item（居中）
        val w1 = itemSizes[1].first
        val h1 = itemSizes[1].second
        val w2 = itemSizes[2].first
        val h2 = itemSizes[2].second

        val rowWidth = w1 + w2 + GAP_PX
        val rowHeight = max(h1, h2)
        val leftOffset = max(0, (contentWidth - rowWidth) / 2)

        allItemRects.add(Rect(leftOffset, top, leftOffset + w1, top + rowHeight))
        allItemRects.add(Rect(leftOffset + w1 + GAP_PX, top, leftOffset + w1 + GAP_PX + w2, top + rowHeight))

        top += rowHeight

        return top + BOTTOM_SAFETY_MARGIN_PX
    }

    // ========================== 4 ITEMS ==========================
    private fun layoutFourItems(itemSizes: List<Pair<Int, Int>>, contentWidth: Int): Int {
        var top = 0

        val (w0, h0) = itemSizes[0]
        val (w1, h1) = itemSizes[1]

        val row0Width = w0 + w1 + GAP_PX
        val row0Height = max(h0, h1)
        val leftOffset0 = max(0, (contentWidth - row0Width) / 2)

        allItemRects.add(Rect(leftOffset0, top, leftOffset0 + w0, top + row0Height))
        allItemRects.add(Rect(leftOffset0 + w0 + GAP_PX, top, leftOffset0 + w0 + GAP_PX + w1, top + row0Height))

        top += row0Height + GAP_PX

        val (w2, h2) = itemSizes[2]
        val (w3, h3) = itemSizes[3]

        val row1Width = w2 + w3 + GAP_PX
        val row1Height = max(h2, h3)
        val leftOffset1 = max(0, (contentWidth - row1Width) / 2)

        allItemRects.add(Rect(leftOffset1, top, leftOffset1 + w2, top + row1Height))
        allItemRects.add(Rect(leftOffset1 + w2 + GAP_PX, top, leftOffset1 + w2 + GAP_PX + w3, top + row1Height))

        top += row1Height
        return top + BOTTOM_SAFETY_MARGIN_PX
    }

    // ========================== 5 ITEMS ==========================
    private fun layoutFiveItems(itemSizes: List<Pair<Int, Int>>, contentWidth: Int): Int {
        var top = 0

        val (w0, h0) = itemSizes[0]
        val (w1, h1) = itemSizes[1]

        val row0Width = w0 + w1 + GAP_PX
        val row0Height = max(h0, h1)
        val leftOffset0 = max(0, (contentWidth - row0Width) / 2)

        allItemRects.add(Rect(leftOffset0, top, leftOffset0 + w0, top + row0Height))
        allItemRects.add(Rect(leftOffset0 + w0 + GAP_PX, top, leftOffset0 + w0 + GAP_PX + w1, top + row0Height))

        top += row0Height + GAP_PX

        val w2 = itemSizes[2].first
        val w3 = itemSizes[3].first
        val w4 = itemSizes[4].first

        val hRow = maxOf(itemSizes[2].second, itemSizes[3].second, itemSizes[4].second)
        val row1Width = w2 + w3 + w4 + 2 * GAP_PX

        val leftOffset1 = max(0, (contentWidth - row1Width) / 2)

        var currentLeft = leftOffset1

        allItemRects.add(Rect(currentLeft, top, currentLeft + w2, top + hRow))
        currentLeft += w2 + GAP_PX

        allItemRects.add(Rect(currentLeft, top, currentLeft + w3, top + hRow))
        currentLeft += w3 + GAP_PX

        allItemRects.add(Rect(currentLeft, top, currentLeft + w4, top + hRow))

        top += hRow
        return top + BOTTOM_SAFETY_MARGIN_PX
    }

    // ========================== fallback ==========================
    private fun layoutFallback(itemSizes: List<Pair<Int, Int>>, contentWidth: Int): Int {
        var top = 0
        itemSizes.forEachIndexed { index, (w, h) ->
            if (index > 0) top += GAP_PX
            val left = max(0, (contentWidth - w) / 2)
            allItemRects.add(Rect(left, top, left + w, top + h))
            top += h
        }
        return top + BOTTOM_SAFETY_MARGIN_PX
    }
}
