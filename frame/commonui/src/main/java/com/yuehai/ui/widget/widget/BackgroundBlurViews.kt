package com.yuehai.ui.widget.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import com.yuehai.ui.widget.util.BlurUtils // 假设 BlurUtils 路径正确
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 高效安全的半透明高斯模糊背景 View
 * * 修复了在 Release 环境中可能出现的 View 绘制时序和资源管理问题。
 */
class BackgroundBlurView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ViewTreeObserver.OnPreDrawListener {

    private val TAG = "BackgroundBlurView"

    /** 模糊半径 */
    var blurRadius = 20f

    /** 缩放系数，越小越高效（推荐 0.2~0.5） */
    var downscaleFactor = 0.25f

    /** 半透明蒙版颜色 */
    var overlayColor = Color.argb(80, 255, 255, 255) // 约 30% 白色透明

    /** 自动更新模糊 */
    var autoUpdate = true

    /** 需要模糊的目标 View */
    var blurTarget: View? = null
        set(value) {
            // 移除旧监听器
            value?.setLayerType(View.LAYER_TYPE_SOFTWARE, null) // 尝试强制软件层
            field?.viewTreeObserver?.removeOnPreDrawListener(this)
            field = value
            // 添加新监听器
            value?.viewTreeObserver?.addOnPreDrawListener(this)
        }

    private var captureBitmap: Bitmap? = null
    private var blurredBitmap: Bitmap? = null
    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 1. 绘制模糊后的背景图
        blurredBitmap?.let {
            // 使用 null Rect 作为源矩形，让 drawBitmap 自动使用整个 Bitmap
            // 目标矩形是自身的边界
            canvas.drawBitmap(it, null, Rect(0, 0, width, height), drawPaint)
        }
        // 2. 绘制半透明蒙版
        canvas.drawColor(overlayColor)
    }

    override fun onPreDraw(): Boolean {
        if (!autoUpdate) return true
        val target = blurTarget ?: return true

        // **增强检查**: 确保自身和目标 View 都已完成布局，防止绘制时序问题
        if (width == 0 || height == 0 || !isLaidOut || !target.isLaidOut) {
            return true
        }

        try {
            // 1️⃣ 计算缩小后的尺寸
            val scaledWidth = max(1, (width * downscaleFactor).roundToInt())
            val scaledHeight = max(1, (height * downscaleFactor).roundToInt())

            // 2️⃣ 创建或复用 captureBitmap
            if (captureBitmap == null ||
                captureBitmap?.width != scaledWidth ||
                captureBitmap?.height != scaledHeight ||
                captureBitmap?.isRecycled == true // 额外的安全检查
            ) {
                captureBitmap?.recycle()
                captureBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
            }

            val currentCaptureBitmap = captureBitmap!!

            // 3️⃣ 捕获目标 View
            val canvas = Canvas(currentCaptureBitmap)

            // **新增清除**: 清除画布，确保旧的绘制内容不残留
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            // 设置缩放和位移
            canvas.scale(downscaleFactor, downscaleFactor)

            // 翻译坐标，以便在 captureBitmap 上正确绘制目标 View 的内容
            // 假设 BackgroundBlurView 和 blurTarget 都在同一个父容器中
            canvas.translate(-left.toFloat(), -top.toFloat())

            target.draw(canvas)

            // 4️⃣ 模糊处理
            val newBlurredBitmap = BlurUtils.blur(context, currentCaptureBitmap, blurRadius.roundToInt(), 1)

            // 检查是否获得了新的模糊图
            if (newBlurredBitmap != currentCaptureBitmap) {
                // 成功生成新的模糊图，更新并回收旧的
                blurredBitmap?.recycle()
                blurredBitmap = newBlurredBitmap
            } else {
                // 模糊失败（例如 RenderScript 失败并返回了输入图），只更新日志，不回收
                Log.w(TAG, "BlurUtils returned input bitmap, indicating blur failure.")
            }

            // 5️⃣ 刷新自己
            invalidate()
        } catch (e: Exception) {
            // 打印异常，帮助您通过正式环境的日志收集工具定位问题
            Log.e(TAG, "Critical error during drawing or blurring in onPreDraw.", e)
        }

        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        // 确保监听器先被移除，这是最佳实践
        blurTarget?.viewTreeObserver?.removeOnPreDrawListener(this)

        // 资源回收
        captureBitmap?.recycle()
        blurredBitmap?.recycle()
        captureBitmap = null
        blurredBitmap = null
    }
}