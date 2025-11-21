package com.yuehai.ui.widget.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class CropByRatioTransformation(private val ratio: Float) : BitmapTransformation() {
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("cropByRatio$ratio").toByteArray(Charsets.UTF_8))
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = (toTransform.width * ratio).toInt()
        return Bitmap.createBitmap(toTransform, 0, 0, width, toTransform.height)
    }
}
