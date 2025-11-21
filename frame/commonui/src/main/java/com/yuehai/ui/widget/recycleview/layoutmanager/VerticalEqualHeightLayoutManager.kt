package com.yuehai.room.layout

import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yuehai.util.util.ext.dp
import kotlin.math.max
import kotlin.math.min

/**
 * 自定义 LayoutManager，用于垂直方向上所有子项高度均分 RecyclerView，并在 Item 之间添加固定间距。
 *
 * @param expectedItemCount 预期项目数量
 * @param itemSpacing Item 之间的间距（单位：像素）
 */
class VerticalEqualHeightLayoutManager(
    private val expectedItemCount: Int,
    private val itemSpacing: Int = 6.dp() // 默认间距 10px
) : RecyclerView.LayoutManager() {

    private val TAG = "EqualHeightLayoutManager"

    // 每个 Item 实际占用的高度（不包含它下面的间距）
    private var itemHeight = 0
    // 当前垂直方向的滚动偏移量
    private var verticalOffset = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollVertically(): Boolean = true

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0 || state.isPreLayout || expectedItemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }

        val totalHeight = height - paddingTop - paddingBottom
        val itemCount = state.itemCount

        // N个Item有 N-1 个间距
        val totalSpacing = max(0, itemCount - 1) * itemSpacing

        // 1. 计算每个 Item 实际分配到的高度
        val contentHeight = totalHeight - totalSpacing // Item 自身占用的总高度
        itemHeight = contentHeight / itemCount

        Log.d(TAG, "TotalHeight=$totalHeight, ItemCount=$itemCount, ItemHeight=$itemHeight, Spacing=$itemSpacing")

        detachAndScrapAttachedViews(recycler)
        verticalOffset = 0
        fill(recycler, state)
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (itemHeight == 0 || state.itemCount == 0) return 0

        val itemCount = state.itemCount
        // 总内容高度 = N * ItemHeight + (N-1) * ItemSpacing
        val totalContentHeight = itemCount * itemHeight + max(0, itemCount - 1) * itemSpacing

        val maxScrollOffset = max(0, totalContentHeight - height)

        var travel = dy
        val newOffset = verticalOffset + dy

        if (newOffset < 0) {
            travel = -verticalOffset
        } else if (newOffset > maxScrollOffset) {
            travel = maxScrollOffset - verticalOffset
        }

        verticalOffset += travel
        offsetChildrenVertical(-travel)
        fill(recycler, state)

        return travel
    }

    private fun fill(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemHeight == 0 || state.itemCount == 0) return

        val itemCount = state.itemCount

        // --- 1. 回收不可见的 ---
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i) ?: continue
            val pos = getPosition(child)

            // Item 顶部位置（包含它前面的所有间距）
            val itemTopContent = pos * itemHeight + pos * itemSpacing

            // Item 底部位置（如果不是最后一个，包含它下面的间距）
            val itemBottomContent = itemTopContent + itemHeight + (if (pos < itemCount - 1) itemSpacing else 0)

            // 检查 Item 是否在可见区域之外
            if (itemBottomContent < verticalOffset || itemTopContent > verticalOffset + height) {
                removeAndRecycleView(child, recycler)
            }
        }

        // --- 2. 计算可见范围 ---
        // 估算可见的第一个和最后一个 Item 索引。这里使用 (itemHeight + itemSpacing) 作为平均步长。
        val step = itemHeight + itemSpacing
        val firstVisible = max(0, verticalOffset / step)
        // 使用一个更精确的估计来确定最后一个可见Item
        val lastVisible = min(itemCount - 1, (verticalOffset + height) / step + 1)

        // --- 3. 布局可见 Item ---
        for (i in firstVisible..lastVisible) {
            if (i < 0 || i >= itemCount) continue // 边界检查
            if (findViewByPosition(i) != null) continue

            val view = recycler.getViewForPosition(i)
            addView(view)

            // 设置 Item 的实际高度（不含间距）
            val lp = view.layoutParams as RecyclerView.LayoutParams
            val childWidth = width - paddingLeft - paddingRight
            val childHeight = itemHeight

            lp.width = childWidth
            lp.height = childHeight
            view.layoutParams = lp

            measureChildWithMargins(view, 0, 0)

            // 计算当前 Item 之前的累计间距
            val currentSpacingOffset = i * itemSpacing

            val left = paddingLeft
            // top = Item 累计高度 + Item 累计间距 - 滚动偏移 + 顶部内边距
            val top = i * itemHeight + currentSpacingOffset - verticalOffset + paddingTop
            val right = left + childWidth
            val bottom = top + childHeight // Item 的底部位置（间距在它下方）

            layoutDecorated(view, left, top, right, bottom)
        }
    }
}