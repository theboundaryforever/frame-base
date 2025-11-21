package com.yuehai.ui.widget.glide

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.ByteBuffer
import java.security.MessageDigest

class CustomRoundedCornersTransformation(
    context: Context,           // 新增 Context 参数
    dpRadius: Int,            // 传入 dp
    @ColorInt private val overlayColor: Int = Color.TRANSPARENT
) : BitmapTransformation() {

    private val radius: Float = dpRadius * context.resources.displayMetrics.density

    private val ID = "com.yuehai.glide.CustomRoundedCornersTransformation"
    private val ID_BYTES = ID.toByteArray(Charsets.UTF_8)

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update(ByteBuffer.allocate(4).putFloat(radius).array())
        messageDigest.update(ByteBuffer.allocate(4).putInt(overlayColor).array())
    }

    override fun equals(other: Any?): Boolean {
        if (other is CustomRoundedCornersTransformation) {
            return radius == other.radius && overlayColor == other.overlayColor
        }
        return false
    }

    override fun hashCode(): Int {
        return ID.hashCode() + radius.hashCode() * 31 + overlayColor.hashCode() * 17
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val result = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, outWidth, outHeight)
        val rectF = RectF(rect)

        val shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.shader = shader

        canvas.drawRoundRect(rectF, radius, radius, paint)

        if (overlayColor != Color.TRANSPARENT) {
            val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            overlayPaint.color = overlayColor
            canvas.drawRoundRect(rectF, radius, radius, overlayPaint)
        }
        return result
    }
}
