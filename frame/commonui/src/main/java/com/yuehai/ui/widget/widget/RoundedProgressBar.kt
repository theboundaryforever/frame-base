package com.yuehai.ui.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.yuehai.ui.R

class RoundedProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var progressColor: Int = Color.GREEN
    private var backgroundColor: Int = Color.YELLOW
    private var borderColor: Int = Color.DKGRAY
    private var borderWidth: Float = 4f
    private var cornerRadius: Float = 30f

    private var maxProgress: Int = 100
    private var currentProgress: Int = 0

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RoundedProgressBar).apply {
            progressColor = getColor(R.styleable.RoundedProgressBar_progressColor, progressColor)
            backgroundColor = getColor(R.styleable.RoundedProgressBar_backgroundColor, backgroundColor)
            borderColor = getColor(R.styleable.RoundedProgressBar_borderColor, borderColor)
            borderWidth = getDimension(R.styleable.RoundedProgressBar_borderWidth, borderWidth)
            cornerRadius = getDimension(R.styleable.RoundedProgressBar_cornerRadius, cornerRadius)
            maxProgress = getInt(R.styleable.RoundedProgressBar_maxProgress, maxProgress)
            currentProgress = getInt(R.styleable.RoundedProgressBar_currentProgress, currentProgress)
            recycle()
        }

        backgroundPaint.color = backgroundColor
        backgroundPaint.style = Paint.Style.FILL

        borderPaint.color = borderColor
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth

        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.FILL
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val halfBorder = borderWidth / 2f

        // 1. 背景（黄底）
        val bgRect = RectF(halfBorder, halfBorder, widthF - halfBorder, heightF - halfBorder)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, backgroundPaint)

        // 2. 边框（棕色）
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, borderPaint)

        // 3. 进度条（绿色，避开边框）
        val progressRatio = currentProgress.toFloat() / maxProgress
        val progressMaxWidth = widthF - borderWidth
        val progressWidth = progressRatio * progressMaxWidth

        if (progressWidth > 0) {
            val progressRect = RectF(
                borderWidth,                      // left
                borderWidth,                      // top
                borderWidth + progressWidth,      // right
                heightF - borderWidth             // bottom
            )
            canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint)
        }
    }

    // 公共方法：外部设置进度或最大值
    fun setMaxProgress(max: Int) {
        maxProgress = max.coerceAtLeast(1)
        invalidate()
    }

    fun setProgress(progress: Int) {
        currentProgress = progress.coerceIn(0, maxProgress)
        invalidate()
    }

    fun setProgressColor(@ColorInt color: Int) {
        progressColor = color
        progressPaint.color = color
        invalidate()
    }

    fun setBackgroundColorCustom(@ColorInt color: Int) {
        backgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }

    fun setBorderColor(@ColorInt color: Int) {
        borderColor = color
        borderPaint.color = color
        invalidate()
    }

    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }
}
