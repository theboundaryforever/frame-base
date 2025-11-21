package com.yuehai.ui.widget.widget.pop

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat

class FloatingPopupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    enum class Direction {
        ABOVE, BELOW, START, END
    }

    enum class AlignMode {
        START, CENTER, END
    }

    private var animator: ObjectAnimator? = null

    init {
        isClickable = false
        isFocusable = false
    }

    companion object {

        fun show(
            rootView: ViewGroup,
            anchorView: View,
            contentView: View,
            direction: Direction = Direction.BELOW,
            alignMode: AlignMode = AlignMode.CENTER,
            xOffset: Int = 0,
            yOffset: Int = 0,
            startMargin: Int = 0,
            endMargin: Int = 0,
            topMargin: Int = 0,
            bottomMargin: Int = 0
        ): FloatingPopupView {
            val context = anchorView.context
            val popupView = FloatingPopupView(context)


            popupView.addView(contentView)
            rootView.addView(
                popupView,
                LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )
            )

            anchorView.post {
                val location = IntArray(2)
                anchorView.getLocationOnScreen(location)
                val anchorRect = Rect(
                    location[0],
                    location[1],
                    location[0] + anchorView.width,
                    location[1] + anchorView.height
                )

                contentView.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                val contentWidth = contentView.measuredWidth
                val contentHeight = contentView.measuredHeight

                val isRtl = ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_RTL
                val displayMetrics = context.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels

                val x: Int
                val y: Int

                when (direction) {
                    Direction.ABOVE -> {
                        x = when (alignMode) {
                            AlignMode.START -> anchorRect.left + xOffset
                            AlignMode.CENTER -> anchorRect.centerX() - contentWidth / 2 + xOffset
                            AlignMode.END -> anchorRect.right - contentWidth - endMargin + xOffset
                        }
                        y = anchorRect.top - contentHeight + yOffset
                    }

                    Direction.BELOW -> {
                        x = when (alignMode) {
                            AlignMode.START -> anchorRect.left + xOffset
                            AlignMode.CENTER -> anchorRect.centerX() - contentWidth / 2 + xOffset
                            AlignMode.END -> anchorRect.right - contentWidth - endMargin + xOffset
                        }
                        y = anchorRect.bottom + yOffset
                    }

                    Direction.START -> {
                        x = if (isRtl) anchorRect.right + xOffset
                        else anchorRect.left - contentWidth + xOffset

                        y = when (alignMode) {
                            AlignMode.START -> anchorRect.top + yOffset
                            AlignMode.CENTER -> anchorRect.centerY() - contentHeight / 2 + yOffset
                            AlignMode.END -> anchorRect.bottom - contentHeight + yOffset
                        }
                    }

                    Direction.END -> {
                        x = if (isRtl) anchorRect.left - contentWidth + xOffset
                        else anchorRect.right + xOffset

                        y = when (alignMode) {
                            AlignMode.START -> anchorRect.top + yOffset
                            AlignMode.CENTER -> anchorRect.centerY() - contentHeight / 2 + yOffset
                            AlignMode.END -> anchorRect.bottom - contentHeight + yOffset
                        }
                    }
                }

                val finalX = x.coerceIn(0, screenWidth - contentWidth)
                val finalY = y.coerceIn(topMargin, screenHeight - contentHeight - bottomMargin)

                popupView.translationX = finalX.toFloat()
                popupView.translationY = finalY.toFloat()

                popupView.animator = ObjectAnimator.ofFloat(contentView, "translationY", 0f, -8f, 0f, 8f, 0f).apply {
                    duration = 2000L
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    start()
                }
            }

            return popupView
        }
    }

    fun dismiss() {
        animator?.cancel()
        (parent as? ViewGroup)?.removeView(this)
    }
}
