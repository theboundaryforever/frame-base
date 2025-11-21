package com.yuehai.ui.widget.widget

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.AppCompatTextView
import com.yuehai.util.util.getCompatString

class LoopingMarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    private val textView = AppCompatTextView(context).apply {
        isSingleLine = true
        ellipsize = null
        textSize=13f

    }

    private var scrollHandler = Handler(Looper.getMainLooper())
    private var scrollRunnable: Runnable? = null
    private var pauseDuration = 2000L
    private var scrollInterval = 16L
    private var scrollStep = 2
    private var isUserScrolling = false

    init {
        isHorizontalScrollBarEnabled = false
        addView(textView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        setTextColor(Color.WHITE)
    }

    fun setText(text: CharSequence) {
        textView.text = text
        postDelayed({ startLoopScroll() }, pauseDuration)
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
    }

    fun setSpan(span: SpannableString) {
        textView.text = span
    }

    private fun startLoopScroll() {
        scrollRunnable?.let { scrollHandler.removeCallbacks(it) }

        scrollRunnable = object : Runnable {
            override fun run() {
                if (isUserScrolling) return

                val maxScroll = textView.width - width
                if (scrollX >= maxScroll) {
                    // 回到头部 + 停留
                    scrollTo(0, 0)
                    scrollHandler.postDelayed(this, pauseDuration)
                } else {
                    scrollBy(scrollStep, 0)
                    scrollHandler.postDelayed(this, scrollInterval)
                }
            }
        }

        scrollHandler.post(scrollRunnable!!)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        isUserScrolling = true
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isUserScrolling = false
                scrollHandler.postDelayed({ startLoopScroll() }, pauseDuration)
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scrollRunnable?.let { scrollHandler.removeCallbacks(it) }
    }
}
