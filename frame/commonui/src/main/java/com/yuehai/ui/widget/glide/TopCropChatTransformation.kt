package com.yuehai.ui.widget.glide

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.Log
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

class BottomUpCropWithBottomCropTransformation(
    private val context: Context,
    private val ratio: Float,           // 高度占屏幕比例
    private val activity: Activity,
    private val endMargin: Int = 0,    // 右边裁剪 px (LTR)
    private val bottomMargin: Int = 0, // 底部裁剪 px
    private val blurRadius: Int = 15,  // 模糊半径 (0 表示不模糊)
    private val blurSampling: Int = 8, // 模糊采样率
    private val cornerRadius: Int = 4.dp()
) : BitmapTransformation() {

    private val TAG = "BottomUpCrop"

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val screenWidth = DisplayUtil.getScreenRealWidth(context)
        val screenHeight = DisplayUtil.getScreenRealHeight(context)

        // 判断是否是 RTL 语言
        val isRtl =
            TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL

        // 1. 缩放 → 裁剪
        val scale = screenWidth.toFloat() / toTransform.width
        val scaledHeight = (toTransform.height * scale).toInt()
        val scaledBitmap = Bitmap.createScaledBitmap(toTransform, screenWidth, scaledHeight, true)

        val targetWidth = max(screenWidth - endMargin, 1)
        val targetHeight = max((screenHeight * ratio).toInt(), 1)
        val cropTop = max(scaledHeight - targetHeight - bottomMargin, 0)
        val cropHeight = min(targetHeight, scaledHeight - cropTop - bottomMargin)

        val croppedBitmap = Bitmap.createBitmap(scaledBitmap, 0, cropTop, screenWidth, cropHeight)

        var resultBitmap: Bitmap = if (isRtl) {
            // ⭐ RTL → 从左边裁 endMargin
            Bitmap.createBitmap(
                croppedBitmap,
                endMargin,
                0,
                max(croppedBitmap.width - endMargin, 1),
                croppedBitmap.height
            )
        } else {
            // ⭐ LTR → 从右边裁 endMargin
            Bitmap.createBitmap(
                croppedBitmap,
                0,
                0,
                targetWidth,
                croppedBitmap.height
            )
        }

        // 2. 高斯模糊
        if (blurRadius > 0) {
            resultBitmap = BlurUtils.blur(context, resultBitmap, blurRadius, blurSampling)
        }

        // 3. 圆角
        if (cornerRadius > 0) {
            resultBitmap = roundCrop(pool, resultBitmap, cornerRadius)
            Log.d(
                TAG,
                "Rounded: ${resultBitmap.width}x${resultBitmap.height}, radius=$cornerRadius, RTL=$isRtl"
            )
        }

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
        messageDigest.update("BottomUpCropWithBottomCrop.$ratio.$endMargin.$bottomMargin.$blurRadius.$blurSampling.$cornerRadius.RTL".toByteArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BottomUpCropWithBottomCropTransformation
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
