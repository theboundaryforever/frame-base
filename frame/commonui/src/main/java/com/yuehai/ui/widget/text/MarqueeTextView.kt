package com.yuehai.ui.widget.text

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

class MarqueeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var textList: List<CharSequence> = emptyList()
    private var currentIndex = 0
    private var isRunning = false

    private val switchInterval = 5000L

    init {
        // 系统跑马灯配置
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        isFocusable = true
        isFocusableInTouchMode = true
        isSelected = true
        setHorizontallyScrolling(true)
    }

    /**
     * 设置要轮播的文字列表
     */
    fun setMarqueeTexts(list: List<CharSequence>) {
        textList = list
        currentIndex = 0
        isRunning = true
        if (textList.isNotEmpty()) {
            setTextAndAnimate(textList[0])
            postDelayed({ next() }, switchInterval)
        }
    }

    /**
     * 停止轮播
     */
    fun stop() {
        isRunning = false
        removeCallbacks(null)
    }

    /**
     * 切换到下一条并应用上下动画
     */
    private fun next() {
        if (!isRunning || textList.isEmpty()) return
        currentIndex = (currentIndex + 1) % textList.size
        val nextText = textList[currentIndex]

        animate()
            .translationY(-height.toFloat())
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                text = nextText
                translationY = height.toFloat()
                alpha = 0f
                animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(300)
                    .withEndAction {
                        postDelayed({ next() }, switchInterval)
                    }
                    .start()
            }.start()
    }

    private fun setTextAndAnimate(content: CharSequence) {
        text = content
        alpha = 1f
        translationY = 0f
    }

    // 为系统跑马灯强制保持 focus & selected
    override fun isFocused(): Boolean = true
}
