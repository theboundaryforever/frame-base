package com.yuehai.ui.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.yuehai.ui.R
import com.yuehai.util.util.getCompatColor
import kotlin.math.max

class BadgeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var count = 0L
    private var badgeText = ""
    private var badgeColor = Color.RED
    private val textColor = Color.WHITE
    private var textSizePx = 12f * context.resources.displayMetrics.scaledDensity
    private var maxHeightPx = 0  // 支持最大高度

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = badgeColor
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textSize = textSizePx
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }

    init {
        badgeColor = getCompatColor(R.color.color_E82323)
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.BadgeView) {
                textSizePx = getDimension(R.styleable.BadgeView_badgeTextSize, textSizePx)
                badgeColor = getColor(R.styleable.BadgeView_badgeBackgroundColor, badgeColor)
                maxHeightPx = getDimensionPixelSize(R.styleable.BadgeView_badgeMaxHeight, 0)

                paint.color = badgeColor
                textPaint.textSize = textSizePx
            }
        }
    }

    fun setCount(value: Long?) {
        count = value ?: 0
        badgeText = when {
            count <= 0 -> ""
            count > 99 -> "99+"
            else -> count.toString()
        }
        visibility = if (badgeText.isEmpty()) GONE else VISIBLE
        requestLayout()
        invalidate()
    }

    fun setBadgeTextSize(sp: Float) {
        textSizePx = sp * context.resources.displayMetrics.scaledDensity
        textPaint.textSize = textSizePx
        requestLayout()
        invalidate()
    }

    fun setBadgeMaxHeight(dp: Float) {
        maxHeightPx = (dp * context.resources.displayMetrics.density).toInt()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val padding = (8 * context.resources.displayMetrics.density).toInt()
        val fm = textPaint.fontMetrics
        val textHeight = (fm.bottom - fm.top)
        val minHeight = (20 * context.resources.displayMetrics.density).toInt()

        val textWidth = textPaint.measureText(badgeText)
        var height = max(textHeight.toInt() + padding, minHeight)

        // 考虑父布局限制
        val parentMode = MeasureSpec.getMode(heightMeasureSpec)
        val parentSize = MeasureSpec.getSize(heightMeasureSpec)
        if (parentMode == MeasureSpec.AT_MOST || parentMode == MeasureSpec.EXACTLY) {
            height = height.coerceAtMost(parentSize)
        }
        if (maxHeightPx > 0) {
            height = height.coerceAtMost(maxHeightPx)
        }

        val width = if (badgeText.length == 1) {
            height // 宽度 = 高度，做成圆形
        } else {
            max(textWidth.toInt() + padding * 2, height)
        }

        setMeasuredDimension(width, height)
    }



    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())

        val radius = if (badgeText.length == 1) {
            width / 2f // 圆形
        } else {
            height / 2f // 圆角矩形
        }

        canvas.drawRoundRect(rectF, radius, radius, paint)

        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(badgeText, x, y, textPaint)
    }

}
