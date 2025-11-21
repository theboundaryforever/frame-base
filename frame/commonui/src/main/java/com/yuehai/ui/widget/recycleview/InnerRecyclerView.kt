package com.yuehai.ui.widget.recycleview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class InnerRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // 让内层优先处理滑动，滑动到底部或顶部才交给外层
        parent.requestDisallowInterceptTouchEvent(true)

        if (ev.action == MotionEvent.ACTION_MOVE) {
            val canScrollVerticallyUp = canScrollVertically(-1) // 是否能向上滑
            val canScrollVerticallyDown = canScrollVertically(1) // 是否能向下滑

            if (!canScrollVerticallyUp || !canScrollVerticallyDown) {
                // 到达顶部或底部，允许外层拦截
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
