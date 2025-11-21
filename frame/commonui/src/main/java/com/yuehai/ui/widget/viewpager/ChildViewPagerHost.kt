package com.yuehai.ui.widget.viewpager


import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.absoluteValue
import kotlin.math.sign


class ChildViewPagerHost @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var touchSlop = 0
    private var initialX = 0f

    private val child: View? get() = if (childCount > 0) getChildAt(0) else null

    init {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private fun canChildScrollHorizontally(delta: Float): Boolean {
        val direction = -delta.sign.toInt()
        return child?.canScrollHorizontally(direction) ?: false
    }

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        handleInterceptTouchEvent(e)
        return super.onInterceptTouchEvent(e)
    }

    private fun handleInterceptTouchEvent(e: MotionEvent) {
        if (e.action == MotionEvent.ACTION_DOWN) {
            initialX = e.x
            parent.requestDisallowInterceptTouchEvent(true)
        } else if (e.action == MotionEvent.ACTION_MOVE) {
            val dx = e.x - initialX
            val scaledDx = dx.absoluteValue * .5f
            if (scaledDx > touchSlop) {
                if (canChildScrollHorizontally(dx)) {

                    parent.requestDisallowInterceptTouchEvent(true)
                } else {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
    }

    companion object {
        const val TAG = "ChildViewPagerHost"
    }
}