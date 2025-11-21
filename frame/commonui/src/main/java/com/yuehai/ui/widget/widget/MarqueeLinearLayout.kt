package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.LinearLayoutCompat

class SimpleMarqueeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    private val container = LinearLayoutCompat(context).apply {
        orientation = LinearLayoutCompat.HORIZONTAL
    }

    private var scrollSpeed = 2
    private var isScrolling = false

    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (!isScrolling || container.childCount == 0) return

            smoothScrollBy(scrollSpeed, 0)

            val contentWidth = container.measuredWidth
            val visibleWidth = width

            if (scrollX >= contentWidth - visibleWidth) {
                scrollTo(0, 0)
            }

            postDelayed(this, 16L)
        }
    }

    init {
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        addView(container, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        // 将 XML 中的子 View 移动到 container 中
        val views = mutableListOf<View>()
        for (i in 0 until childCount - 1) {
            views.add(getChildAt(i))
        }
        views.forEach {
            removeView(it)
            container.addView(it)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { checkAndStartScroll() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopScroll()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // 再次检测是否满足滚动条件（防止第一次测量没成功）
        post { checkAndStartScroll() }
    }

    private fun checkAndStartScroll() {
        if (!isScrolling && container.measuredWidth > width) {
            startScroll()
        }
    }

    private fun startScroll() {
        isScrolling = true
        post(scrollRunnable)
    }

    private fun stopScroll() {
        isScrolling = false
        removeCallbacks(scrollRunnable)
    }

    fun setScrollSpeed(speed: Int) {
        scrollSpeed = speed
    }

    fun pause() = stopScroll()
    fun resume() = checkAndStartScroll()
}
