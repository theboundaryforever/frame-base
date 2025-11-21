package com.yuehai.util.util.ext

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yuehai.util.util.isActivityInValid

fun View.loadBitmapFromUrl(
    url: String,
    onSuccess: (Bitmap) -> Unit,
    onFailure: (() -> Unit)? = null
) {
    if (isActivityInValid(this.context)) return

    val customTarget = object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            onSuccess(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            // 可选：资源被清除时处理
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            onFailure?.invoke()
        }
    }

    Glide.with(this.context)
        .asBitmap()
        .load(url)
        .into(customTarget)
}
