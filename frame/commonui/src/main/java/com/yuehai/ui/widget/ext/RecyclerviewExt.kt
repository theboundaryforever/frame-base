package com.yuehai.ui.widget.ext

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 修复 ViewPager2 与横向 RecyclerView 的滑动冲突
 */
/**
 * 修复 ViewPager2 与横向 RecyclerView 的滑动冲突（最终版）
 */
@SuppressLint("ClickableViewAccessibility")
fun RecyclerView.fixViewPager2ScrollConflict() {
    var startX = 0f
    var startY = 0f
    var isDragging = false

    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            val parent = rv.parent
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = e.x
                    startY = e.y
                    isDragging = false
                    // 禁止父控件立刻拦截
                    parent?.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = e.x - startX
                    val dy = e.y - startY

                    if (!isDragging && abs(dx) > abs(dy) && abs(dx) > 8) {
                        isDragging = true
                    }

                    if (isDragging) {
                        val canScroll =
                            (dx > 0 && rv.canScrollHorizontally(-1)) ||
                                    (dx < 0 && rv.canScrollHorizontally(1))

                        // 只有还能滑动时才拦截父控件
                        parent?.requestDisallowInterceptTouchEvent(canScroll)
                    } else {
                        // 垂直方向或滑到边缘，交给父控件（ViewPager2）
                        parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent?.requestDisallowInterceptTouchEvent(false)
                    isDragging = false
                }
            }
            return false // 不拦截自身事件
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    })
}