package com.yuehai.util.util.svga

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.opensource.svgaplayer.*
import com.tencent.qgame.animplayer.*
import com.tencent.qgame.animplayer.inter.*
import com.tencent.qgame.animplayer.mix.Resource
import com.yuehai.util.AppUtil
import com.yuehai.util.util.DownloadListener
import com.yuehai.util.util.OkDownloadExt
import com.yuehai.util.util.download.MediaCacheManager
import org.libpag.PAGFile
import org.libpag.PAGView
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

private const val TAG = "AnimPlayExt"

/* ================= SVGA Parser ================= */

val parser: SVGAParser by lazy {
    SVGAParser.shareParser().apply {
        init(AppUtil.appContext)
        Log.d(TAG, "SVGAParser init")
    }
}

private val svgaDecodeExecutor = Executors.newSingleThreadExecutor()

/* ================= Callback ================= */

interface PlayCallback {
    fun onPlayStart() {}
    fun onPlayComplete() {}
    fun onPlayFailed(error: String?) {}
}

/* ================= SVGA Core ================= */

private const val AVATAR_KEY = "key01"
private const val NAME_KEY = "key02"

private fun SVGAImageView.playCoreSafe(
    sourceKey: String,
    loop: Int,
    avatarUrl: String?,
    nickName: String?,
    callback: PlayCallback?,
    isLocalFile: Boolean = false
) {
    val playId = "$sourceKey#${System.nanoTime()}"
    tag = playId
    loops = loop
    visibility = View.VISIBLE

    callback?.onPlayStart()

    svgaDecodeExecutor.execute {
        try {
            val completion = object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    post {
                        if (tag != playId) return@post

                        val dynamic = SVGADynamicEntity().apply {
                            if (!nickName.isNullOrEmpty()) {
                                setDynamicText(
                                    nickName,
                                    TextPaint().apply {
                                        color = Color.WHITE
                                        textSize = 24f
                                        isAntiAlias = true
                                        typeface = Typeface.DEFAULT_BOLD
                                        textAlign = Paint.Align.CENTER
                                    },
                                    NAME_KEY
                                )
                            }
                        }

                        fun start() {
                            if (tag != playId) return
                            stopAnimation()
                            setVideoItem(videoItem, dynamic)
                            startAnimation()
                        }

                        if (!avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@playCoreSafe)
                                .asBitmap()
                                .load(avatarUrl)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        if (tag != playId) return
                                        dynamic.setDynamicImage(resource, AVATAR_KEY)
                                        start()
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        start()
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        start()
                                    }
                                })
                        } else {
                            start()
                        }
                    }
                }

                override fun onError() {
                    post {
                        if (tag == playId) {
                            callback?.onPlayFailed("SVGA decode error")
                        }
                    }
                }
            }

            when {
                sourceKey.startsWith("assets:") ->
                    parser.decodeFromAssets(sourceKey.removePrefix("assets:"), completion)

                isLocalFile ->
                    parser.decodeFromInputStream(
                        FileInputStream(sourceKey),
                        sourceKey,
                        completion,
                        true
                    )

                else ->
                    parser.decodeFromURL(URL(sourceKey), completion)
            }
        } catch (e: Exception) {
            post {
                if (tag == playId) {
                    callback?.onPlayFailed(e.message)
                }
            }
        }
    }

    this.callback = object : SVGACallback {
        override fun onFinished() {
            if (tag == playId) {
                callback?.onPlayComplete()
            }
        }

        override fun onPause() {}
        override fun onRepeat() {}
        override fun onStep(frame: Int, percentage: Double) {}
    }
}

/* ================= SVGA APIs（全部保留） ================= */

fun SVGAImageView.playSVGA(
    url: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA URL is empty")
        return
    }

    val cache = MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
    if (cache.exists()) {
        playCoreSafe(cache.absolutePath, loop, null, null, callback, true)
    } else {
        OkDownloadExt.downloadSingle(url, object : DownloadListener {
            override fun onComplete(file: File) {
                post {
                    playCoreSafe(file.absolutePath, loop, null, null, callback, true)
                }
            }

            override fun onFailed(error: Throwable?) {
                callback?.onPlayFailed(error?.message)
            }
        })
    }
}

fun SVGAImageView.playSVGAWithKey(
    url: String,
    avatarUrl: String,
    nickName: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA URL is empty")
        return
    }

    val cache = MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
    if (cache.exists()) {
        playCoreSafe(cache.absolutePath, loop, avatarUrl, nickName, callback, true)
    } else {
        OkDownloadExt.downloadSingle(url, object : DownloadListener {
            override fun onComplete(file: File) {
                post {
                    playCoreSafe(file.absolutePath, loop, avatarUrl, nickName, callback, true)
                }
            }

            override fun onFailed(error: Throwable?) {
                callback?.onPlayFailed(error?.message)
            }
        })
    }
}

fun SVGAImageView.playSVGAInputStream(
    file: File?,
    avatarUrl: String,
    nickName: String,
    loop: Int = 1,
    callback: PlayCallback? = null,
    useCache: Boolean = false
) {
    if (file == null) {
        callback?.onPlayFailed("SVGA file is null")
        return
    }
    playCoreSafe(file.absolutePath, loop, avatarUrl, nickName, callback, true)
}

fun SVGAImageView.playAssetsSVGA(
    assetName: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (assetName.isEmpty()) {
        callback?.onPlayFailed("SVGA asset name is empty")
        return
    }
    playCoreSafe("assets:$assetName", loop, null, null, callback)
}

fun SVGAImageView.playSVGAFromInternet(
    url: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA URL is empty")
        return
    }
    playCoreSafe(url, loop, null, null, callback)
}

fun SVGAImageView.playSVGAWithKeyFromInternet(
    url: String,
    loop: Int = 1,
    avatarUrl: String? = "",
    nickName: String? = null,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA URL is empty")
        return
    }
    playCoreSafe(url, loop, avatarUrl, nickName, callback)
}

/* ================= PAG ================= */

fun PAGView.playPGA(url: String, callback: PlayCallback? = null) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("PAG URL is empty")
        return
    }

    val cache = MediaCacheManager.getPagCacheFile(AppUtil.appContext, url)
    if (cache.exists()) {
        playPAGInternal(cache, callback)
    } else {
        OkDownloadExt.downloadSingle(url, object : DownloadListener {
            override fun onComplete(file: File) {
                post { playPAGInternal(file, callback) }
            }

            override fun onFailed(error: Throwable?) {
                callback?.onPlayFailed(error?.message)
            }
        })
    }
}

private fun PAGView.playPAGInternal(file: File, callback: PlayCallback?) {
    try {
        val pagFile = PAGFile.Load(file.absolutePath)
        composition = pagFile
        setRepeatCount(1)
        addListener(object : PAGView.PAGViewListener {
            override fun onAnimationStart(view: PAGView?) {
                callback?.onPlayStart()
            }

            override fun onAnimationEnd(view: PAGView?) {
                callback?.onPlayComplete()
            }

            override fun onAnimationCancel(view: PAGView?) {}
            override fun onAnimationRepeat(view: PAGView?) {}
            override fun onAnimationUpdate(view: PAGView?) {}
        })
        play()
    } catch (e: Exception) {
        callback?.onPlayFailed(e.message)
    }
}

/* ================= VAP (MP4) ================= */

fun AnimView.checkMp4Cache(
    giftMp4Url: String,
    loop: Int = 1,
    nickName: String? = "",
    avatarUrl: String? = "",
    avatarUrl2: String? = "",
    callback: PlayCallback? = null
) {
    if (TextUtils.isEmpty(giftMp4Url)) {
        callback?.onPlayFailed("MP4 URL is empty")
        return
    }

    val file = MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, giftMp4Url)
    if (file.exists()) {
        playMP4(file, loop, nickName, avatarUrl, avatarUrl2, callback)
    } else {
        OkDownloadExt.downloadSingle(giftMp4Url, object : DownloadListener {
            override fun onComplete(file: File) {
                post {
                    playMP4(file, loop, nickName, avatarUrl, avatarUrl2, callback)
                }
            }

            override fun onFailed(error: Throwable?) {
                callback?.onPlayFailed(error?.message)
            }
        })
    }
}

private fun AnimView.playMP4(
    file: File,
    loop: Int,
    nickName: String?,
    avatar: String?,
    avatarUrl2: String?,
    callback: PlayCallback?
) {
    stopPlay()
    setLoop(loop)

    setFetchResource(object : IFetchResource {
        override fun fetchImage(resource: Resource, result: (Bitmap?) -> Unit) {
            val url = when (resource.tag) {
                "tag1" -> avatar
                "tag2" -> avatarUrl2
                else -> null
            }

            if (url.isNullOrEmpty()) {
                result(null)
                return
            }

            Glide.with(this@playMP4)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) = result(resource)

                    override fun onLoadCleared(placeholder: Drawable?) = result(null)
                    override fun onLoadFailed(errorDrawable: Drawable?) = result(null)
                })
        }

        override fun fetchText(resource: Resource, result: (String?) -> Unit) {
            if (resource.tag == "tag2" || resource.tag == "tag3") {
                result(nickName ?: "")
            } else result(null)
        }

        override fun releaseResource(resources: List<Resource>) {}
    })

    setAnimListener(object : IAnimListener {
        override fun onVideoStart() {
            callback?.onPlayStart()
        }

        override fun onVideoComplete() {
            callback?.onPlayComplete()
        }

        override fun onFailed(errorType: Int, errorMsg: String?) {
            callback?.onPlayFailed("AnimView error($errorType): $errorMsg")
        }

        override fun onVideoDestroy() {}
        override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {}
    })

    startPlay(file)
}
