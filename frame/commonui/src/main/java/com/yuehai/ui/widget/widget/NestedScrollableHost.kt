package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

import androidx.viewpager2.widget.ViewPager2

class NestedScrollableHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var initialX = 0f
    private var initialY = 0f
    private val touchSlop = 10 // 灵敏度阈值，可调节

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (childCount > 0) {
            val viewPager = getChildAt(0) as? ViewPager2
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = ev.x
                    initialY = ev.y
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.x - initialX
                    val dy = ev.y - initialY

                    val isHorizontal = abs(dx) > abs(dy) && abs(dx) > touchSlop

                    if (viewPager != null && isHorizontal) {
                        val currentItem = viewPager.currentItem
                        val itemCount = viewPager.adapter?.itemCount ?: 0

                        if (currentItem == 0 && dx > 0) {
                            // 第一页且向右滑，允许父拦截（外层滑动）
                            parent.requestDisallowInterceptTouchEvent(false)
                        } else if (currentItem == itemCount - 1 && dx < 0) {
                            // 最后一页且向左滑，允许父拦截
                            parent.requestDisallowInterceptTouchEvent(false)
                        } else {
                            // 中间页或非边界滑动，禁止父拦截（内层滑动）
                            parent.requestDisallowInterceptTouchEvent(true)
                        }
                    } else if (abs(dy) > touchSlop && abs(dy) > abs(dx)) {
                        // 纵向滑动，允许父控件拦截（如果需要）
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}
