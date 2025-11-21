package com.yuehai.ui.widget.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class RVNestedScrollingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent3 {

    private val helper = NestedScrollingParentHelper(this)

    var recyclerView: RecyclerView? = null

    var divideView: View? = null

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        helper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        helper.onStopNestedScroll(target, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {

        if (target != recyclerView) {
            if (dy > 0) {
                //内层子scroll view上滑
                val divideViewTop = divideView?.top ?: return

                if (divideViewTop > 0) {
                    //未置顶
                    if (divideViewTop < dy) {
                        consumed[1] = divideViewTop
                        recyclerView?.scrollBy(0, divideViewTop)
                    } else {
                        consumed[1] = dy
                        recyclerView?.scrollBy(0, dy)
                    }
                }
            } /*else if (target is SmartRefreshLayout) {
                val canScroll = recyclerView?.canScrollVertically(dy) ?: true
                if (canScroll) {
                    consumed[1] = dy
                    recyclerView?.scrollBy(0, dy)
                }
            }*/
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {

        if (target != recyclerView && dyUnconsumed < 0) {
            //内层子scroll view下滑还有未消费滑动距离
            val canScroll = recyclerView?.canScrollVertically(dyUnconsumed) ?: true

            if (canScroll) {
                consumed[1] = dyUnconsumed
                recyclerView?.scrollBy(0, dyUnconsumed)
            }
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {

    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean,
    ): Boolean {
        return false
    }

    interface OnSetDivideViewListener {

        fun setDivideView(view: View)

    }

}