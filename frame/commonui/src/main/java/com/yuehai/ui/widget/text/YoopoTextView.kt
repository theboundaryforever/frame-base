package com.yuehai.ui.widget.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.yuehai.ui.R

/**
 * 可设置文字描边的TextView
 * 默认文字描边颜色 #FF0A8399
 */
open class YoopoTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    //描边
    private val strokePaint = Paint()
    private var strokeColor: Int = 0
    private var strokeWidth: Float = 0f

    //阴影
    private val shadowPaint = Paint()
    private var shadowDx: Float = 0f
    private var shadowDy: Float = 0f
    private var shadowRadius: Float = 0f
    private var shadowColor: Int = 0

    //文字渐变色
    private val gradientPaint = Paint()
    private var gradientOrientation = VERTICAL_GRADIENT
    private var gradientColors: Array<Int>? = null
    private var gradientColorPositions: Array<Float>? = null

    private val SRC_ATOP_X_FERMODE = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.WenextTextView,
            0, 0
        ).apply {
            strokeColor = getColor(
                R.styleable.WenextTextView_stroke_color,
                resources.getColor(android.R.color.black)
            )
            strokeWidth = getDimension(R.styleable.WenextTextView_stroke_width, 0f)

            shadowDx = getFloat(R.styleable.WenextTextView_shadow_dx, 0f)
            shadowDy = getFloat(R.styleable.WenextTextView_shadow_dy, 0f)
            shadowRadius = getFloat(R.styleable.WenextTextView_shadow_radius, 0f)
            shadowColor = getColor(
                R.styleable.WenextTextView_shadow_color,
                resources.getColor(android.R.color.black)
            )

            val gradientColors = getString(R.styleable.WenextTextView_gradient_colors)
            val gradientPositions = getString(R.styleable.WenextTextView_gradient_position)
            gradientOrientation =
                getInt(R.styleable.WenextTextView_gradient_orientation, VERTICAL_GRADIENT)
            if (gradientColors != null && gradientPositions != null) {
                val cc = gradientColors.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val pp = gradientPositions.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (cc.isNotEmpty() && cc.size == pp.size) {
                    this@YoopoTextView.gradientColors =
                        cc.map { Color.parseColor(it) }.toTypedArray()
                    gradientColorPositions = pp.map { it.toFloat() }.toTypedArray()
                    setTextColor(Color.WHITE)
                }
            }

            recycle()
        }
        updatePaintAndInvalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setStroke(
            strokeColor,
            strokeWidth
        )
        setVerticalLinearGradient(
            gradientColors?.toList() ?: emptyList(),
            gradientColorPositions?.toList() ?: emptyList()
        )
    }

    fun setColors(strokeColor: Int, shadowColor: Int) {
        this.strokeColor = strokeColor
        this.shadowColor = shadowColor
        updatePaintAndInvalidate()
    }

    fun setStroke(color: Int, width: Float) {
        strokeColor = color
        strokeWidth = width
        updatePaintAndInvalidate()
    }

    fun setShadow(radius: Float, dx: Float, dy: Float, color: Int) {
        shadowRadius = radius
        shadowDx = dx
        shadowDy = dy
        shadowColor = color
        updatePaintAndInvalidate()
    }

    fun setVerticalLinearGradient(colors: List<Int>, positions: List<Float>) {
        gradientColors = colors.toTypedArray()
        gradientColorPositions = positions.toTypedArray()
        updatePaintAndInvalidate()
    }

    private fun updatePaintAndInvalidate() {
        //阴影画笔
        shadowPaint.isAntiAlias = true
        shadowPaint.isDither = true
        shadowPaint.isFilterBitmap = true
        shadowPaint.strokeWidth = strokeWidth
        shadowPaint.strokeJoin = Paint.Join.ROUND
        shadowPaint.strokeCap = Paint.Cap.ROUND
        shadowPaint.style = Paint.Style.FILL_AND_STROKE
        if (shadowDx > 0 && shadowDy > 0) {
            shadowPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)
        } else {
            shadowPaint.clearShadowLayer()
        }

        //描边画笔
        strokePaint.isAntiAlias = true
        strokePaint.isDither = true
        strokePaint.isFilterBitmap = true
        strokePaint.strokeWidth = strokeWidth
        strokePaint.shader = LinearGradient(
            0f, 0f,
            0f, lineHeight.toFloat(),
            strokeColor,
            strokeColor,
            Shader.TileMode.CLAMP
        )
        strokePaint.strokeJoin = Paint.Join.ROUND
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.style = Paint.Style.STROKE
        strokePaint.xfermode = SRC_ATOP_X_FERMODE

        //渐变画笔
        gradientPaint.isAntiAlias = true
        gradientPaint.isDither = true
        gradientPaint.isFilterBitmap = true
        val gradientColors = gradientColors
        val gradientPositions = gradientColorPositions
        if (gradientColors != null && gradientPositions != null
            && gradientColors.isNotEmpty()
            && gradientColors.size == gradientPositions.size
        ) {
            gradientPaint.shader = if (gradientOrientation == VERTICAL_GRADIENT) {
                LinearGradient(
                    0f,
                    0f,
                    0f,
                    lineHeight.toFloat(),
                    gradientColors.toIntArray(),
                    gradientPositions.toFloatArray(),
                    Shader.TileMode.CLAMP
                )
            } else {
                LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    0f,
                    gradientColors.toIntArray(),
                    gradientPositions.toFloatArray(),
                    Shader.TileMode.CLAMP
                )
            }
        } else {
            gradientPaint.shader = null
        }
        gradientPaint.strokeJoin = Paint.Join.ROUND
        gradientPaint.strokeCap = Paint.Cap.ROUND
        gradientPaint.style = Paint.Style.FILL
        strokePaint.xfermode = SRC_ATOP_X_FERMODE

        postInvalidate()
    }

    private fun isDrawShadow(): Boolean {
        return shadowDx > 0 && shadowDy > 0
    }

    private fun isDrawStroke(): Boolean {
        return strokeWidth > 0
    }

    private fun isDrawGradient(): Boolean {
        val gradientColors = gradientColors
        val gradientPositions = gradientColorPositions
        return gradientColors != null && gradientPositions != null
                && gradientColors.isNotEmpty()
                && gradientColors.size == gradientPositions.size
    }

    fun setShadowLayer(radius: Float, dx: Float, dy: Float, color: String?) {
        shadowPaint.setShadowLayer(radius, dx, dy, Color.parseColor(color))
    }

    override fun onDraw(canvas: Canvas) {
        //阴影
        if (isDrawShadow()) {
            drawShadow {
                super.onDraw(canvas)
            }
        }

        //描边
        if (isDrawStroke()) {
            drawStroke {
                super.onDraw(canvas)
            }
        }

        if (isDrawGradient()) {
            //渐变
            drawGradient {
                super.onDraw(canvas)
            }
        } else {
            super.onDraw(canvas)
        }
    }

    private fun drawShadow(callDraw: () -> Unit) {
        val originIsAntiAlias = paint.isAntiAlias
        val originIsDither = paint.isDither
        val originIsFilterBitmap = paint.isFilterBitmap
        val originStrokeWidth = paint.strokeWidth
        val originTextColor = textColors
        val originStrokeJoin = paint.strokeJoin
        val originStrokeCap = paint.strokeCap
        val originStyle = paint.style

        paint.isAntiAlias = shadowPaint.isAntiAlias
        paint.isDither = shadowPaint.isDither
        paint.isFilterBitmap = shadowPaint.isFilterBitmap
        paint.strokeWidth = shadowPaint.strokeWidth
        //paint.color = shadowPaint.color
        paint.strokeJoin = shadowPaint.strokeJoin
        paint.strokeCap = shadowPaint.strokeCap
        paint.style = shadowPaint.style
        paint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)

        callDraw.invoke()

        paint.isAntiAlias = originIsAntiAlias
        paint.isDither = originIsDither
        paint.isFilterBitmap = originIsFilterBitmap
        paint.strokeWidth = originStrokeWidth
        paint.strokeJoin = originStrokeJoin
        paint.strokeCap = originStrokeCap
        paint.style = originStyle
        paint.clearShadowLayer()
    }

    private fun drawStroke(callDraw: () -> Unit) {
        val originIsAntiAlias = paint.isAntiAlias
        val originIsDither = paint.isDither
        val originIsFilterBitmap = paint.isFilterBitmap
        val originStrokeWidth = paint.strokeWidth
        val originShader = paint.shader
        val originStrokeJoin = paint.strokeJoin
        val originStrokeCap = paint.strokeCap
        val originStyle = paint.style

        paint.isAntiAlias = strokePaint.isAntiAlias
        paint.isDither = strokePaint.isDither
        paint.isFilterBitmap = strokePaint.isFilterBitmap
        paint.strokeWidth = strokePaint.strokeWidth
        paint.shader = strokePaint.shader
        paint.strokeJoin = strokePaint.strokeJoin
        paint.strokeCap = strokePaint.strokeCap
        paint.style = strokePaint.style

        callDraw.invoke()

        paint.isAntiAlias = originIsAntiAlias
        paint.isDither = originIsDither
        paint.isFilterBitmap = originIsFilterBitmap
        paint.strokeWidth = originStrokeWidth
        paint.shader = originShader
        paint.strokeJoin = originStrokeJoin
        paint.strokeCap = originStrokeCap
        paint.style = originStyle
        paint.clearShadowLayer()
    }

    private fun drawGradient(callDraw: () -> Unit) {
        val originIsAntiAlias = paint.isAntiAlias
        val originIsDither = paint.isDither
        val originIsFilterBitmap = paint.isFilterBitmap
        val originShader = paint.shader
        val originStrokeJoin = paint.strokeJoin
        val originStrokeCap = paint.strokeCap
        val originStyle = paint.style

        paint.isAntiAlias = gradientPaint.isAntiAlias
        paint.isDither = gradientPaint.isDither
        paint.isFilterBitmap = gradientPaint.isFilterBitmap
        paint.shader = gradientPaint.shader
        paint.strokeJoin = gradientPaint.strokeJoin
        paint.strokeCap = gradientPaint.strokeCap
        paint.style = gradientPaint.style

        callDraw.invoke()

        paint.isAntiAlias = originIsAntiAlias
        paint.isDither = originIsDither
        paint.isFilterBitmap = originIsFilterBitmap
        paint.shader = originShader
        paint.strokeJoin = originStrokeJoin
        paint.strokeCap = originStrokeCap
        paint.style = originStyle
    }

    companion object {
        const val VERTICAL_GRADIENT = 0
        const val HORIZONTAL_GRADIENT = 1
    }

}