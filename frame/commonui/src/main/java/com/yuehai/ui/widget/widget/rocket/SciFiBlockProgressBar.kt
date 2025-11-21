package com.yuehai.ui.widget.widget.rocket

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.yuehai.ui.R
import com.yuehai.util.util.ext.dpf
import androidx.core.graphics.toColorInt
import kotlin.math.ceil
import androidx.core.content.withStyledAttributes
import kotlin.math.min

class SciFiBlockProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var blockCount: Int = 20
        set(value) {
            field = value.coerceAtLeast(1)
            invalidate()
        }

    var blockSpacing: Float = 4.dpf()
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidate()
        }

    var fixedBlockHeight: Float = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
            invalidate()
        }

    private var activeColor: Int = Color.CYAN
    private var inactiveColor: Int = "#004D53".toColorInt()
    private var glowColor: Int = Color.CYAN
    private var frameColor: Int = Color.CYAN
    private val cornerRadius = 1.dpf()
    private val frameWidth = 3.dpf()

    private var pulseAlpha = 0f
    private val maxGlowRadius = 8.dpf()
    private val pulseAnimator: ValueAnimator by lazy { createPulseAnimator() }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val inactivePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, 100)
            postInvalidate()
        }

    init {
        context.withStyledAttributes(
            attrs,
            R.styleable.SciFiBlockProgressBar,
            defStyleAttr,
            0
        ) {
            blockCount = getInteger(R.styleable.SciFiBlockProgressBar_blockCount, blockCount)
            blockSpacing =
                getDimension(R.styleable.SciFiBlockProgressBar_blockSpacing, blockSpacing)
            fixedBlockHeight = // 读取新增的固定块高度属性
                getDimension(R.styleable.SciFiBlockProgressBar_blockHeight, 0f)
            activeColor = getColor(R.styleable.SciFiBlockProgressBar_activeColor, activeColor)
            inactiveColor = getColor(R.styleable.SciFiBlockProgressBar_inactiveColor, inactiveColor)
            glowColor = getColor(R.styleable.SciFiBlockProgressBar_glowColor, glowColor)
            frameColor = getColor(R.styleable.SciFiBlockProgressBar_frameColor, glowColor) // 读取外框颜色
        }
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        activePaint.color = activeColor
        inactivePaint.color = inactiveColor
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = frameWidth
        framePaint.color = frameColor
        framePaint.setShadowLayer(8f.dpf(), 0f, 0f, glowColor)
        textPaint.color = activeColor
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.setShadowLayer(5.dpf(), 0f, 0f, glowColor)
    }

    private fun createPulseAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0f, 1f, 0f)
        animator.addUpdateListener {
            pulseAlpha = it.animatedValue as Float
            if (progress < 100) {
                invalidate()
            }
        }
        animator.duration = 1000L
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        return animator
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode && progress < 100) {
            pulseAnimator.start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulseAnimator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (progress == 100 && pulseAnimator.isRunning) {
            pulseAnimator.cancel()
            pulseAlpha = 1f
        } else if (progress < 100 && !pulseAnimator.isRunning) {
            pulseAnimator.start()
        }

        if (blockCount <= 0) return

        val finalActiveBlocks: Int = calculateActiveBlocks(progress, blockCount)
        val frameContentInset = frameWidth + 1.dpf()
        val contentLeft = paddingLeft.toFloat() + frameContentInset
        val contentTop = paddingTop.toFloat() + frameContentInset
        val contentRight = width.toFloat() - paddingRight - frameContentInset
        val contentBottom = height.toFloat() - paddingBottom - frameContentInset

        val contentWidth = contentRight - contentLeft
        val totalAvailableHeight = contentBottom - contentTop

        val finalBlockHeight: Float
        val startingY: Float
        val blocksToDraw: Int

        if (fixedBlockHeight > 0f) {
            val blockAndSpacingHeight = fixedBlockHeight + blockSpacing
            val blocksThatFit = if (blockAndSpacingHeight > 0f) {
                ((totalAvailableHeight + blockSpacing) / blockAndSpacingHeight).toInt()
            } else {
                blockCount
            }
            blocksToDraw = min(blockCount, blocksThatFit)
            if (blocksToDraw <= 0) {
                return
            }
            finalBlockHeight = fixedBlockHeight
            val totalSpacingUsed = if (blocksToDraw > 0) blockSpacing * (blocksToDraw - 1) else 0f
            val totalUsedHeight = (finalBlockHeight * blocksToDraw) + totalSpacingUsed
            startingY = contentTop + (totalAvailableHeight - totalUsedHeight) / 2

        } else {
            val totalSpacing = blockSpacing * (blockCount - 1)
            finalBlockHeight = (totalAvailableHeight - totalSpacing) / blockCount
            startingY = contentTop
            blocksToDraw = blockCount
        }
        if (finalBlockHeight <= 0f) return

        for (i in 0 until blocksToDraw) {
            val top = startingY + i * (finalBlockHeight + blockSpacing)
            val bottom = top + finalBlockHeight
            rect.set(contentLeft, top, contentRight, bottom)
            val invertedIndex = blockCount - 1 - i
            val isActive = invertedIndex < finalActiveBlocks

            if (isActive) {
                activePaint.setShadowLayer(
                    maxGlowRadius * pulseAlpha,
                    0f, 0f,
                    glowColor
                )

                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, activePaint)
                activePaint.clearShadowLayer()
            } else {
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, inactivePaint)
            }
        }

        val textX = width / 2f
        // 垂直居中计算
        val textY = height / 2f - ((textPaint.descent() + textPaint.ascent()) / 2)
       // canvas.drawText("$progress%", textX, textY, textPaint)
        rect.set(
            paddingLeft + frameWidth / 2,
            paddingTop + frameWidth / 2,
            width.toFloat() - paddingRight - frameWidth / 2,
            height.toFloat() - paddingBottom - frameWidth / 2
        )

        canvas.drawRoundRect(rect, cornerRadius * 2, cornerRadius * 2, framePaint)
    }


    private fun calculateActiveBlocks(progress: Int, blockCount: Int): Int {
        if (progress == 100) {
            return blockCount
        }

        val rawActiveBlocks = progress.toFloat() * blockCount / 100f

        var calculatedBlocks = ceil(rawActiveBlocks).toInt()

        if (calculatedBlocks == blockCount && progress < 100) {
            calculatedBlocks = blockCount - 1
        }

        return calculatedBlocks.coerceIn(0, blockCount)
    }
}