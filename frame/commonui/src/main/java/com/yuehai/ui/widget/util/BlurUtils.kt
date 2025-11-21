package com.yuehai.ui.widget.util

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log // 导入 Log
import kotlin.math.min

object BlurUtils {
    private const val TAG = "BlurUtils"
    private const val MAX_BLUR_RADIUS = 25f

    /**
     * 高效高斯模糊，优先使用 RenderScript，失败则尝试回退到软件模糊（如果实现）。
     */
    fun blur(context: Context, bitmap: Bitmap, radius: Int, sampling: Int): Bitmap {
        if (radius <= 0) return bitmap

        val width = (bitmap.width / sampling)
        val height = (bitmap.height / sampling)

        if (width <= 0 || height <= 0) return bitmap

        val inputBitmap = try {
            Bitmap.createScaledBitmap(bitmap, width, height, false)
        } catch (e: Exception) {
            return bitmap
        }

        // 尝试 RenderScript 模糊
        val blurredBitmap = try {
            blurWithRenderScript(context, inputBitmap, radius)
        } catch (e: Exception) {
            Log.e(TAG, "RenderScript blur failed! Falling back to software blur.", e)

            // RenderScript 失败时，执行回退
            // 注意：您需要自己实现或引入 StackBlur/FastBlur
            // 暂时返回未模糊的图（如果还没有实现软件回退）
            return inputBitmap // 或者调用您自己实现的软件模糊方法
        } finally {
            // 确保回收输入图
            if (inputBitmap != bitmap) {
                inputBitmap.recycle()
            }
        }

        return blurredBitmap
    }

    /**
     * 独立方法：使用 RenderScript 进行模糊处理
     */
    private fun blurWithRenderScript(context: Context, inputBitmap: Bitmap, radius: Int): Bitmap {
        val config = inputBitmap.config ?: Bitmap.Config.ARGB_8888
        val outputBitmap = Bitmap.createBitmap(inputBitmap.width, inputBitmap.height, config)

        var renderScript: RenderScript? = null
        var scriptIntrinsicBlur: ScriptIntrinsicBlur? = null
        var tmpIn: Allocation? = null
        var tmpOut: Allocation? = null

        try {
            // 1. 初始化 RenderScript
            renderScript = RenderScript.create(context)
            scriptIntrinsicBlur = ScriptIntrinsicBlur.create(
                renderScript,
                Element.U8_4(renderScript)
            )

            // 2. 创建分配
            tmpIn = Allocation.createFromBitmap(renderScript, inputBitmap)
            tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap)

            // 3. 执行模糊
            scriptIntrinsicBlur.setRadius(min(radius.toFloat(), MAX_BLUR_RADIUS))
            scriptIntrinsicBlur.setInput(tmpIn)
            scriptIntrinsicBlur.forEach(tmpOut)
            tmpOut.copyTo(outputBitmap)

            return outputBitmap

        } finally {
            // 4. 清理资源
            tmpIn?.destroy()
            tmpOut?.destroy()
            scriptIntrinsicBlur?.destroy()
            renderScript?.destroy()
            // 注意：这里不再回收 inputBitmap，因为在 blur() 主方法中统一处理了
        }
    }
}