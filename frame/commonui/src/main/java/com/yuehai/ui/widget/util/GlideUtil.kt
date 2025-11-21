package com.yuehai.ui.widget.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yuehai.ui.R
import java.io.File

object GlideUtils {

    fun loadImageWithNotPlace(context: View, url: String?, imageView: ImageView) {

        Glide.with(context)
            .load(url)
            .into(imageView)
    }

    fun loadImage(context: View, url: String?, imageView: ImageView, resId: Int = 0) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_normal_ic
        } else {
            resId
        }

        Glide.with(context)
            .load(url)
            .placeholder(placeId)
            .error(placeId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }
    fun loadImageWithContext(context: Context, url: String?, imageView: ImageView, resId: Int = 0) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_normal_ic
        } else {
            resId
        }

        Glide.with(context)
            .load(url)
            .placeholder(placeId)
            .error(placeId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun loadImageFile(context: View, file: File?, imageView: ImageView, resId: Int = 0) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_normal_ic
        } else {
            resId
        }

        Glide.with(context)
            .load(file)
            .placeholder(placeId)
            .error(placeId)
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 避免 Glide 缓存干扰
            .skipMemoryCache(true)
            .into(imageView)
    }

    fun loadResImage(context: View, imageView: ImageView, resId: Int = 0) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_normal_ic
        } else {
            resId
        }

        Glide.with(context)
            .load(resId)
            .placeholder(placeId)
            .error(placeId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    /**
     * 加载圆形图片
     */
    fun loadCircleImage(context: View, url: String, imageView: ImageView, resId: Int = 0) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_circle_ic
        } else {
            resId
        }
        Glide.with(context)
            .load(url)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(placeId)
                    .error(placeId)
            )
            .into(imageView)
    }
    fun loadCircleImage(context: Context, url: String, imageView: ImageView, resId: Int = 0) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_circle_ic
        } else {
            resId
        }
        Glide.with(context)
            .load(url)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(placeId)
                    .error(placeId)
            )
            .into(imageView)
    }

    fun loadGifFirstFrame(
        context: View,
        url: String,
        imageView: ImageView,
        resId: Int = 0
    ) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_normal_ic
        } else {
            resId
        }
        Glide.with(context)
            .asBitmap()
            .load(url)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(placeId)
                    .error(resId)
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                    imageView.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    imageView.setImageDrawable(placeholder) // 防止被清除后是空白
                }
            })

    }

    fun loadRoundCornerImage(
        context: View,
        url: String,
        radius: Int,
        imageView: ImageView,
        resId: Int = 0,
    ) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_normal_ic
        } else {
            resId
        }
        val requestOptions = RequestOptions()
            .transform(CenterCrop(),RoundedCorners(radius))
            .placeholder(placeId)
            .error(placeId)

        Glide.with(context)
            .load(url)
            .apply(requestOptions)
            .placeholder(placeId) // 建议你预先做成圆角
            .into(imageView)

    }

    fun loadCircleImage(
        context: View,
        url: String,
        resId: Int = 0,
        size: Int,
        imageView: ImageView
    ) {
        Glide.with(context)
            .load(url)
            .override(size)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(resId)
                    .error(resId)
            )
            .into(imageView)
    }

    /**
     * 加载圆形图片
     */
    fun loadCircleResImage(context: Context, resId: Int, imageView: ImageView) {
        Glide.with(context)
            .load(resId)
            .apply(
                RequestOptions()
                    .circleCrop()
                    .placeholder(resId)
                    .error(resId)
            )
            .into(imageView)
    }


    /**
     * 加载圆角图片
     */
    @SuppressLint("CheckResult")
    fun loadRoundedImage(
        context: Context,
        url: String,
        imageView: ImageView,
        radius: Int,
        resId: Int = 0
    ) {
        val placeId = if (resId == 0) {
            R.mipmap.placeholder_res_normal_ic
        } else {
            resId
        }
        val requestOptions = RequestOptions()
            .transform(RoundedCorners(radius))
            .placeholder(placeId)
            .error(placeId)
        Glide.with(context)
            .load(url)
            .apply(requestOptions)
            .error(placeId)
            .into(imageView)
    }

    /**
     * 加载 GIF
     */
    fun loadGif(context: Context, gifUrl: String, imageView: ImageView) {
        Glide.with(context)
            .asGif()
            .load(gifUrl)
            .load(gifUrl)
            .diskCacheStrategy(DiskCacheStrategy.DATA) // 缓存原始资源（如 .gif 文件）
            .into(imageView)
    }

    /**
     * 清理缓存
     */
    fun clearCache(context: Context) {
        Glide.get(context).clearMemory() // 清除内存缓存（主线程）
        Thread { Glide.get(context).clearDiskCache() }.start() // 清除磁盘缓存（子线程）
    }

    fun load(context: Context, url: String, callback: (Drawable?) -> Unit) {
        Glide.with(context)
            .load(url)
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    callback(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 这里可选处理占位图释放
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    callback(null)
                }
            })
    }
}
