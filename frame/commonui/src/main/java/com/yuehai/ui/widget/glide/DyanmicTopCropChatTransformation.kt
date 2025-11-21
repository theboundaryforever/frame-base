package com.yuehai.ui.widget.glide

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.Log // 导入 Log
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.yuehai.ui.widget.util.BlurUtils
import com.yuehai.util.util.DisplayUtil
import com.yuehai.util.util.ext.dp
import java.security.MessageDigest
import java.util.*
import kotlin.math.max
import kotlin.math.min

class DynamicBottomUpCropWithBottomCropTransformation(
    private val context: Context,
    private val ratio: Float,
    private val activity: Activity,
    private val endMargin: Int = 0,
    private val bottomMargin: Int = 0,
    private val blurRadius: Int = 15,
    private val blurSampling: Int = 8,
    private val cornerRadius: Int = 4.dp()
) : BitmapTransformation() {

    private val TAG = "DynamicCropLog" // 使用新的 TAG 方便过滤日志

     override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap, // 动画帧的 Bitmap
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val screenWidth = DisplayUtil.getScreenRealWidth(context)
        val screenHeight = DisplayUtil.getScreenRealHeight(context)
        val isRtl =
            TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL

        // --- LOG 1: 原始输入 ---
        Log.d(TAG, "Input Frame: ${toTransform.width}x${toTransform.height}. Hash: ${toTransform.hashCode()}")

        // 1. 缩放
        val scale = screenWidth.toFloat() / toTransform.width
        val scaledHeight = (toTransform.height * scale).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(toTransform, screenWidth, scaledHeight, true)
        // ⚠️ 关键修正：不回收 toTransform，它是动画帧，交给 Glide 管理。

        // --- LOG 2: 缩放结果 ---
        Log.d(TAG, "Scaled Bitmap: ${scaledBitmap.width}x${scaledBitmap.height}. TargetRatio: $ratio")


        // 2. 裁剪
        val targetWidth = max(screenWidth - endMargin, 1)
        val targetHeight = max((screenHeight * ratio).toInt(), 1)
        val cropTop = max(scaledHeight - targetHeight - bottomMargin, 0)
        val availableHeightFromCropTop = scaledHeight - cropTop
        val finalCropHeight = max(min(targetHeight, availableHeightFromCropTop), 1)

        val croppedBitmap = Bitmap.createBitmap(scaledBitmap, 0, cropTop, screenWidth, finalCropHeight)
        pool.put(scaledBitmap) // 释放 scaledBitmap

        // --- LOG 3: 底部裁剪结果 ---
        Log.d(TAG, "Cropped Bitmap: ${croppedBitmap.width}x${croppedBitmap.height}. Top:$cropTop, Height:$finalCropHeight")


        // 3. 侧边裁剪 (End Margin Crop)
        var resultBitmap: Bitmap = if (isRtl) {
            val finalWidth = max(croppedBitmap.width - endMargin, 1)
            val tempBitmap = Bitmap.createBitmap(
                croppedBitmap, endMargin, 0, finalWidth, croppedBitmap.height
            )
            pool.put(croppedBitmap)
            tempBitmap
        } else {
            val tempBitmap = Bitmap.createBitmap(
                croppedBitmap, 0, 0, targetWidth, croppedBitmap.height
            )
            pool.put(croppedBitmap)
            tempBitmap
        }

        // --- LOG 4: 侧边裁剪结果 ---
        Log.d(TAG, "Final Cropped Bitmap: ${resultBitmap.width}x${resultBitmap.height}. Hash: ${resultBitmap.hashCode()}")


        // 4. 高斯模糊
        if (blurRadius > 0) {
            // --- LOG 5: 模糊前检查 ---
            Log.d(TAG, "Blur: Applying blur (R:$blurRadius, S:$blurSampling). Hash before: ${resultBitmap.hashCode()}")

            val blurredBitmap = BlurUtils.blur(context, resultBitmap, blurRadius, blurSampling)

            // --- LOG 6: 模糊后检查 ---
            Log.d(TAG, "Blur: Hash after BlurUtils: ${blurredBitmap.hashCode()}. BlurRadius: $blurRadius")

            if (blurredBitmap != resultBitmap) {
                pool.put(resultBitmap)
                resultBitmap = blurredBitmap
                // --- LOG 7: 模糊成功 ---
                Log.d(TAG, "Blur SUCCESS. Replaced Bitmap.")
            } else {
                // --- LOG 7: 模糊失败 ---
                Log.e(TAG, "Blur FAILED. Hash unchanged. Check BlurUtils implementation.")
            }
        }

        // 5. 圆角
        if (cornerRadius > 0) {
            resultBitmap = roundCrop(pool, resultBitmap, cornerRadius)
        }

        // --- LOG 8: 最终输出 ---
        Log.d(TAG, "Output Frame: ${resultBitmap.width}x${resultBitmap.height}. Hash: ${resultBitmap.hashCode()}")

        return resultBitmap
    }

    private fun roundCrop(pool: BitmapPool, source: Bitmap, radius: Int): Bitmap {
        val result = pool.get(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rectF = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        canvas.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, 0f, 0f, paint)

        pool.put(source)
        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("BottomUpCropWithBottomCrop.V4.$ratio.$endMargin.$bottomMargin.$blurRadius.$blurSampling.$cornerRadius.RTL".toByteArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DynamicBottomUpCropWithBottomCropTransformation
        return ratio == other.ratio
                && endMargin == other.endMargin
                && bottomMargin == other.bottomMargin
                && blurRadius == other.blurRadius
                && blurSampling == other.blurSampling
                && cornerRadius == other.cornerRadius
    }

    override fun hashCode(): Int {
        var result = (ratio.hashCode() * 31 + endMargin) * 31 + bottomMargin
        result = result * 31 + blurRadius
        result = result * 31 + blurSampling
        result = result * 31 + cornerRadius
        return result
    }
}