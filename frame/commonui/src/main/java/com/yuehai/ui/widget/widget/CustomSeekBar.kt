package com.yuehai.ui.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.graphics.drawable.toBitmap
import com.yuehai.ui.R
import com.yuehai.util.util.ext.dpf
import androidx.core.graphics.toColorInt

class CustomSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.seekBarStyle
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    private var thumbWidth = 24.dpf()
    private var thumbHeight = 24.dpf()
    private var trackColor = Color.LTGRAY
    private var progressColor = "#FF5722".toColorInt()
    private var trackRadius = 3.dpf()

    // 新增：进度条高度，默认8dp
    private var trackHeight = 8.dpf()

    private var thumbBitmap: Bitmap? = null

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var isUserSeekable = true

    fun setSeekable(enable: Boolean) {
        isUserSeekable = enable
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.CustomSeekBar, 0, 0).apply {
            try {
                thumbWidth = getDimension(R.styleable.CustomSeekBar_thumbWidth, 24.dpf())
                thumbHeight = getDimension(R.styleable.CustomSeekBar_thumbHeight, 24.dpf())
                trackColor = getColor(R.styleable.CustomSeekBar_trackColor, Color.LTGRAY)
                progressColor = getColor(R.styleable.CustomSeekBar_progressbarColor, Color.RED)
                trackRadius = getDimension(R.styleable.CustomSeekBar_trackRadius, 3.dpf())

                // 读取自定义属性 trackHeight，如果没有则用默认8dp
                trackHeight = getDimension(R.styleable.CustomSeekBar_trackHeight, 8.dpf())

                val drawable = getDrawable(R.styleable.CustomSeekBar_thumbDrawable)
                thumbBitmap = drawable?.toBitmap(thumbWidth.toInt(), thumbHeight.toInt())

                val maxFromXml = getInt(R.styleable.CustomSeekBar_maxValue, -1)
                if (maxFromXml > 0) max = maxFromXml
                isUserSeekable = getBoolean(R.styleable.CustomSeekBar_isSeekable, true)
            } finally {
                recycle()
            }
        }

        thumb = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (isUserSeekable) {
            super.onTouchEvent(event)
        } else {
            false
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val startX = paddingLeft.toFloat()
        val endX = width - paddingRight.toFloat()
        val centerY = height / 2f

        val progressX = startX + (progress.toFloat() / max) * (endX - startX)

        // 使用 trackHeight 计算上下边界
        val rect = RectF(
            startX,
            centerY - trackHeight / 2,
            endX,
            centerY + trackHeight / 2
        )
        trackPaint.color = trackColor
        canvas.drawRoundRect(rect, trackRadius, trackRadius, trackPaint)

        val progressRect = RectF(
            startX,
            centerY - trackHeight / 2,
            progressX,
            centerY + trackHeight / 2
        )
        trackPaint.color = progressColor
        canvas.drawRoundRect(progressRect, trackRadius, trackRadius, trackPaint)

        // thumb绘制位置同样相对trackHeight调整，如果需要，可以微调thumbY
        thumbBitmap?.let {
            val thumbX = progressX - thumbWidth / 2
            // 这里你想让thumb比track高，可以调整thumbY，例：thumbY比centerY减trackHeight高一点
            val thumbY = centerY - thumbHeight / 2
            canvas.drawBitmap(it, thumbX, thumbY, null)
        }
    }
}
