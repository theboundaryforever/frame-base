package com.yuehai.ui.widget.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

/**
 * 一个自定义布局，用于包裹横向滚动的组件（如 ViewPager2、Banner 等）。
 * 它会拦截水平滑动事件，并放行竖直滑动事件，从而解决与父容器 RecyclerView 的滑动冲突。
 */
class VerticalScrollDisablerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var initialX = 0f
    private var initialY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()

    // 关键修正：使用枚举状态来管理滑动模式，避免逻辑混乱
    private var scrollState = ScrollState.IDLE

    private enum class ScrollState {
        IDLE,
        VERTICAL,
        HORIZONTAL
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = ev.x
                initialY = ev.y
                // 按下时重置状态
                scrollState = ScrollState.IDLE
                // 在 ACTION_DOWN 时，始终不拦截，让子视图（Banner）有机会接收事件
                return false
            }
            MotionEvent.ACTION_MOVE -> {
                // 如果已经确定了滑动方向，直接返回相应的结果
                if (scrollState != ScrollState.IDLE) {
                    return scrollState == ScrollState.HORIZONTAL
                }

                val dx = ev.x - initialX
                val dy = ev.y - initialY

                // 只有当滑动距离超过容错距离时，才开始判断方向并更新状态
                if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                    if (abs(dx) > abs(dy)) {
                        // 水平滑动，进入 HORIZONTAL 状态，并拦截
                        scrollState = ScrollState.HORIZONTAL
                        return true
                    } else {
                        // 竖直滑动，进入 VERTICAL 状态，不拦截
                        scrollState = ScrollState.VERTICAL
                        return false
                    }
                }
                // 未达到容错距离，不拦截
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 事件结束时，重置状态
                scrollState = ScrollState.IDLE
                return false
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    // 移除 onTouchEvent，让父类处理
}