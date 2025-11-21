package com.yuehai.ui.widget.widget

import android.content.Context
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import com.yuehai.ui.R

class RatioImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    enum class Direction { TOP, BOTTOM, START, END }

    var ratio: Float = 1f
        set(value) {
            field = value.coerceIn(0.01f, 1f)
            invalidate() // 当比例改变时，请求重新绘制
        }

    var direction: Direction = Direction.BOTTOM
        set(value) {
            field = value
            invalidate() // 当方向改变时，请求重新绘制
        }

    init {
        // 在初始化块中处理自定义属性
        context.withStyledAttributes(attrs, R.styleable.RatioImageView) {
            ratio = getFloat(R.styleable.RatioImageView_ratio, 1f)
            val dir = getInt(R.styleable.RatioImageView_direction, 1)
            direction = when (dir) {
                0 -> Direction.TOP
                1 -> Direction.BOTTOM
                2 -> Direction.START
                3 -> Direction.END
                else -> Direction.BOTTOM
            }
        }
    }



    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()
    private var bitmapShader: BitmapShader? = null

    override fun onDraw(canvas: Canvas) {
        // 检查 Drawable 是否存在且是 BitmapDrawable 类型
        val bitmapDrawable = drawable as? BitmapDrawable ?: run {
            super.onDraw(canvas)
            return
        }

        val bitmap = bitmapDrawable.bitmap ?: run {
            super.onDraw(canvas)
            return
        }

        // 计算剪裁区域和绘制区域
        val viewWidth = width
        val viewHeight = height

        // 如果视图尺寸为0，则不绘制
        if (viewWidth <= 0 || viewHeight <= 0) {
            return
        }

        // 图片的原始尺寸
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // 确保图片不为空
        if (bitmapWidth <= 0 || bitmapHeight <= 0) {
            return
        }

        // 根据 scaleType（例如 centerCrop）计算图片的缩放和位置
        val scaleX = viewWidth.toFloat() / bitmapWidth
        val scaleY = viewHeight.toFloat() / bitmapHeight
        val scale = maxOf(scaleX, scaleY)

        val scaledWidth = bitmapWidth * scale
        val scaledHeight = bitmapHeight * scale
        val offsetX = (viewWidth - scaledWidth) / 2
        val offsetY = (viewHeight - scaledHeight) / 2

        // 定义要绘制的源矩形（在原图上的剪裁区域）
        // 为了简化，这里直接根据 view 的尺寸来计算，通常是比较准确的
        val srcRect = Rect(0, 0, bitmapWidth, bitmapHeight)

        // 定义目标矩形（在 canvas 上的绘制区域）
        val destRect = when (direction) {
            Direction.TOP -> Rect(0, 0, viewWidth, (viewHeight * ratio).toInt())
            Direction.BOTTOM -> Rect(0, (viewHeight * (1 - ratio)).toInt(), viewWidth, viewHeight)
            Direction.START -> Rect(0, 0, (viewWidth * ratio).toInt(), viewHeight)
            Direction.END -> Rect((viewWidth * (1 - ratio)).toInt(), 0, viewWidth, viewHeight)
        }

        // 绘制剪裁后的 Bitmap
        canvas.drawBitmap(bitmap, srcRect, destRect, null)
    }
}