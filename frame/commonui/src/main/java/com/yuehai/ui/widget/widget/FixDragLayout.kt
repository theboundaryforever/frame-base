package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.yuehai.ui.R
import kotlin.math.abs

/**
 * 解决ViewPager2不同方向的嵌套，如ViewPager2嵌套垂直滚动的recyclerview，在垂直滑动的过程中容易触发左右切换的问题
 */
class FixDragLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var downX: Float = 0f
    private var downY: Float = 0f
    private var isDragged = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop / 2
    private var orientation = LinearLayout.VERTICAL

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.FixDragLayout)
        orientation = ta.getInt(R.styleable.FixDragLayout_android_orientation, LinearLayout.VERTICAL)
        ta.recycle()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                isDragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDragged) {
                    val dx = abs(ev.x - downX)
                    val dy = abs(ev.y - downY)
                    if (orientation == LinearLayout.HORIZONTAL) {
                        isDragged = dx > touchSlop && dx > dy
                    } else if (orientation == LinearLayout.VERTICAL) {
                        isDragged = dy > touchSlop && dy > dx
                    }
                }
                parent.requestDisallowInterceptTouchEvent(isDragged)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragged = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}