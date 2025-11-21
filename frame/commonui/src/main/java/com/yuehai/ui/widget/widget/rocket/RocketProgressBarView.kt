package com.yuehai.ui.widget.widget.rocket

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.yuehai.ui.R

private const val DEFAULT_RADIUS_DP = 16f
private const val DEFAULT_TEXT_SIZE_SP = 14f
private const val DEFAULT_BORDER_WIDTH_DP = 1f
private const val DEFAULT_PADDING_DP = 2f

class RocketCustomProgressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 画笔
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 区域
    private val backgroundRect = RectF()
    private val progressRect = RectF()

    // 属性（自动有 getter/setter）
    var maxProgress = 100
        set(value) {
            field = value.coerceAtLeast(1)
            currentProgress = currentProgress.coerceIn(0, field)
            invalidate()
        }

    var currentProgress = 50
        set(value) {
            field = value.coerceIn(0, maxProgress)
            invalidate()
        }

    private var cornerRadiusPx: Float
    private var borderStrokeWidthPx: Float
    private var paddingPx: Float

    init {
        val density = context.resources.displayMetrics.density
        val scaledDensity = context.resources.displayMetrics.scaledDensity

        @ColorInt val defaultBgColor = Color.WHITE
        @ColorInt val defaultFillColor = Color.parseColor("#00AEEF")
        @ColorInt val defaultBorderColor = Color.parseColor("#CCCCCC")
        @ColorInt val defaultTextColor = Color.parseColor("#222222")

        cornerRadiusPx = DEFAULT_RADIUS_DP * density
        borderStrokeWidthPx = DEFAULT_BORDER_WIDTH_DP * density
        paddingPx = DEFAULT_PADDING_DP * density

        val ta = context.obtainStyledAttributes(attrs, R.styleable.CustomProgressBarView)

        val bgColor = ta.getColor(
            R.styleable.CustomProgressBarView_progressBarBackgroundColor,
            defaultBgColor
        )
        val fillColor = ta.getColor(
            R.styleable.CustomProgressBarView_progressBarFillColor,
            defaultFillColor
        )
        val borderColor = ta.getColor(
            R.styleable.CustomProgressBarView_progressBarBorderColor,
            defaultBorderColor
        )
        val textColor = ta.getColor(
            R.styleable.CustomProgressBarView_progressBarTextColor,
            defaultTextColor
        )

        cornerRadiusPx = ta.getDimension(
            R.styleable.CustomProgressBarView_progressBarRadius,
            cornerRadiusPx
        )
        borderStrokeWidthPx = ta.getDimension(
            R.styleable.CustomProgressBarView_progressBarBorderWidth,
            borderStrokeWidthPx
        )
        paddingPx = ta.getDimension(
            R.styleable.CustomProgressBarView_progressBarPadding,
            paddingPx
        )
        val textSizePx = ta.getDimension(
            R.styleable.CustomProgressBarView_progressBarTextSize,
            DEFAULT_TEXT_SIZE_SP * scaledDensity
        )

        maxProgress = ta.getInteger(
            R.styleable.CustomProgressBarView_progressBarmaxProgress,
            maxProgress
        )
        currentProgress = ta.getInteger(
            R.styleable.CustomProgressBarView_progressBarcurrentProgress,
            currentProgress
        )

        ta.recycle()

        backgroundPaint.color = bgColor
        backgroundPaint.style = Paint.Style.FILL

        borderPaint.color = borderColor
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderStrokeWidthPx

        progressPaint.color = fillColor
        progressPaint.style = Paint.Style.FILL

        textPaint.color = textColor
        textPaint.textSize = textSizePx
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val defaultHeight = (40 * context.resources.displayMetrics.density).toInt()
        val resultHeight = resolveSize(defaultHeight, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resultHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // 背景
        backgroundRect.set(0f, 0f, w, h)
        canvas.drawRoundRect(backgroundRect, cornerRadiusPx, cornerRadiusPx, backgroundPaint)

        // 边框
        val borderOffset = borderStrokeWidthPx / 2f
        backgroundRect.set(borderOffset, borderOffset, w - borderOffset, h - borderOffset)
        canvas.drawRoundRect(backgroundRect, cornerRadiusPx, cornerRadiusPx, borderPaint)

        // 进度
        if (currentProgress > 0) {
            val inset = borderStrokeWidthPx + paddingPx
            val availableWidth = w - inset * 2
            val progressWidth = availableWidth * currentProgress / maxProgress

            progressRect.set(
                inset,
                inset,
                inset + progressWidth,
                h - inset
            )

            val innerRadius = (cornerRadiusPx - inset).coerceAtLeast(0f)
            canvas.drawRoundRect(progressRect, innerRadius, innerRadius, progressPaint)
        }

        // 文本
//        val percentage = (currentProgress.toFloat() / maxProgress * 100).toInt()
//      //  val text = "$percentage%"
//
//        val x = w / 2
//        val y = h / 2 - (textPaint.descent() + textPaint.ascent()) / 2
//       // canvas.drawText(text, x, y, textPaint)
    }

    /** 避免 JVM 冲突的自定义方法 */
    fun getProgressValue(): Int = currentProgress
    fun getMaxProgressValue(): Int = maxProgress
}
