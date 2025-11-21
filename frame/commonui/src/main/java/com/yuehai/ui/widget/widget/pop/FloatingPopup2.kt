package com.yuehai.ui.widget.widget.pop

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.PopupWindow
import androidx.core.view.ViewCompat

class FloatingPopupWindow(
    private val context: Context,
    private val contentView: View,
    private val direction: Direction = Direction.BELOW,
    private val alignMode: AlignMode = AlignMode.CENTER,
    private val xOffset: Int = 0,
    private val yOffset: Int = 0,
    private val startMargin: Int = 0,
    private val endMargin: Int = 0,
    private val topMargin: Int = 0,
    private val bottomMargin: Int = 0,
    private val autoDismiss: Boolean = true, // 是否自动消失
    private val dismissDelayMillis: Long = 3000L // 自动消失延迟时间
) : PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false) {

    enum class Direction {
        ABOVE, BELOW, START, END
    }

    enum class AlignMode {
        START, CENTER, END
    }

    private var animator: ObjectAnimator? = null

    init {
        isOutsideTouchable = true
        isTouchable = false
        isFocusable = false
        setBackgroundDrawable(ColorDrawable(0x00000000)) // 透明背景

        // 设置浮动动画
        animator = ObjectAnimator.ofFloat(contentView, "translationY", 0f, -8f, 0f, 8f, 0f).apply {
            duration = 2000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    fun show(anchorView: View) {
        anchorView.post {
            val anchorRect = getAnchorRect(anchorView)
            contentView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val contentWidth = contentView.measuredWidth
            val contentHeight = contentView.measuredHeight

            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            val isRtl = ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_RTL

            val x = when (direction) {
                Direction.ABOVE, Direction.BELOW -> when (alignMode) {
                    AlignMode.START -> anchorRect.left + xOffset
                    AlignMode.CENTER -> anchorRect.centerX() - contentWidth / 2 + xOffset
                    AlignMode.END -> anchorRect.right - contentWidth - endMargin + xOffset
                }
                Direction.START -> if (isRtl) anchorRect.right + xOffset else anchorRect.left - contentWidth + xOffset
                Direction.END -> if (isRtl) anchorRect.left - contentWidth + xOffset else anchorRect.right + xOffset
            }.coerceIn(0, kotlin.math.max(0, screenWidth - contentWidth))

            val y = when (direction) {
                Direction.ABOVE -> anchorRect.top - contentHeight + yOffset
                Direction.BELOW -> anchorRect.bottom + yOffset
                Direction.START, Direction.END -> when (alignMode) {
                    AlignMode.START -> anchorRect.top + yOffset
                    AlignMode.CENTER -> anchorRect.centerY() - contentHeight / 2 + yOffset
                    AlignMode.END -> anchorRect.bottom - contentHeight + yOffset
                }
            }.coerceIn(
                topMargin,
                kotlin.math.max(topMargin, screenHeight - contentHeight - bottomMargin)
            )

            showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)

            if (autoDismiss) {
                contentView.postDelayed({ dismiss() }, dismissDelayMillis)
            }
        }
    }

    private fun getAnchorRect(view: View): Rect {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Rect(
            location[0],
            location[1],
            location[0] + view.width,
            location[1] + view.height
        )
    }

    override fun dismiss() {
        animator?.cancel()
        super.dismiss()
    }
}
