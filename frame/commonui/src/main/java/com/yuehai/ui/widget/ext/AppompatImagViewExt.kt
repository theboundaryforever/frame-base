package com.yuehai.ui.widget.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yuehai.ui.R
import com.yuehai.ui.widget.util.GlideUtils
import com.yuehai.ui.widget.widget.LevelViewInfo
import com.yuehai.util.AppUtil
import com.yuehai.util.util.ext.dp
import com.yuehai.util.util.ext.dpf
import java.io.File

fun AppCompatImageView.loadUrl(url: String, context: Context? = null) {
    if (url.endsWith(".webp")) {
        GlideUtils.loadImageWithNotPlace(this, url, this)
    } else {
        GlideUtils.loadImage(this, url, this)
    }
}

fun AppCompatImageView.loadUrlWithContext(url: String, context: Context) {
    GlideUtils.loadImageWithContext(context, url, this)
}

fun AppCompatImageView.loadWebpFrame(url: String) {
    if (url.endsWith(".webp")) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(this)

    } else {
        GlideUtils.loadImage(this, url, this)
    }
}

fun AppCompatImageView.loadFile(file: File, context: Context? = null) {
    GlideUtils.loadImageFile(this, file, this)
}

fun AppCompatImageView.loadPureUrl(url: String, context: Context? = null) {
    GlideUtils.loadImageWithNotPlace(this, url, this)
}

fun AppCompatImageView.loadUrl(url: String, context: Context? = null, placeId: Int) {
    GlideUtils.loadImage(this, url, this, placeId)
}

fun AppCompatImageView.loadUrlWithContext(url: String, context: Context, placeId: Int) {
    GlideUtils.loadImageWithContext(context, url, this, placeId)
}

fun AppCompatImageView.loadRes(resId: Int, context: Context? = null) {
    GlideUtils.loadResImage(this, this, resId)
}

fun AppCompatImageView.loadCircleUrl(url: String, context: Context? = null) {
    GlideUtils.loadCircleImage(this, url, this)
}

fun AppCompatImageView.loadCircleUrlWithContext(url: String, context: Context) {
    GlideUtils.loadCircleImage(context, url, this)
}

fun AppCompatImageView.loadGifFirstFrame(url: String, context: Context? = null) {
    GlideUtils.loadGifFirstFrame(this, url, this)
}

fun AppCompatImageView.loadRoundCornerUrl(
    url: String,
    context: Context? = null,
    radious: Int = 15.dp()
) {
    GlideUtils.loadRoundCornerImage(this, url, radious, this)
}

fun AppCompatImageView.loadRoundCornerUrl(
    url: String,
    context: Context? = null,
    placeId: Int,
    radious: Int = 15.dp()
) {
    GlideUtils.loadRoundCornerImage(this, url, radious, this, placeId)
}

fun AppCompatImageView.loadGifUrl(url: String, context: Context) {
    GlideUtils.loadGif(context, url, this)
}

val minHeight = 18.dpf()
val minWidth = 44.dpf()
fun loadImageWithOriginalSize(url: LevelViewInfo, imageView: AppCompatImageView) {
    // 先打上标记
    imageView.tag = url.levelUrl
    Glide.with(AppUtil.appContext)
        .asBitmap()
        .load(url.levelUrl)
        .placeholder(R.mipmap.placeholder_res_normal_ic)
        .dontAnimate()
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (imageView.tag == url.levelUrl) {
                    val width = resource.width
                    val height = resource.height

                    val layoutParams =
                        imageView.layoutParams ?: ViewGroup.LayoutParams(width, height)
                    val aspectRatio =
                        width.toFloat() / height
                    val realHeight = url.labelHeight

                    val realWidth = if (url.vipLevel) {
                        minWidth
                    } else {
                        (realHeight * aspectRatio).toInt()
                    }

                    layoutParams.width = realWidth.toInt()
                    layoutParams.height = realHeight.toInt()
                    imageView.layoutParams = layoutParams
                    imageView.requestLayout()

                    imageView.setImageBitmap(resource)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                if (imageView.tag == url.levelUrl) {
                    imageView.setImageDrawable(placeholder)
                }
            }
        })
}
