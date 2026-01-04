package com.yuehai.util.util.svga

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import android.util.LruCache
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
import java.io.*
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

private const val TAG = "AnimPlayExt"

private val mainHandler = Handler(Looper.getMainLooper())

/* ---------------- SVGA Parser ---------------- */

val parser: SVGAParser by lazy {
    SVGAParser.shareParser().apply {
        init(AppUtil.appContext)
        Log.d(TAG, "SVGAParser init")
    }
}

private val decodeExecutor = Executors.newSingleThreadExecutor()

/* ---------------- 安全缓存：只缓存原始字节 ---------------- */

private const val MAX_CACHE_COUNT = 3
private val svgaBinaryCache = object : LruCache<String, ByteArray>(MAX_CACHE_COUNT) {}

private val loadingMap =
    ConcurrentHashMap<String, MutableList<(ByteArray?) -> Unit>>()

/* ---------------- 播放回调 ---------------- */

interface PlayCallback {
    fun onPlayStart() {}
    fun onPlayComplete() {}
    fun onPlayFailed(error: String?) {}
}

private const val AVATAR_KEY = "key01"
private const val NAME_KEY = "key02"

/* ---------------- 核心加载逻辑 ---------------- */

private fun loadSVGABytes(
    key: String,
    loader: () -> ByteArray?,
    onResult: (ByteArray?) -> Unit
) {
    svgaBinaryCache.get(key)?.let {
        Log.d(TAG, "SVGA hit cache: $key")
        onResult(it)
        return
    }

    synchronized(loadingMap) {
        if (loadingMap.containsKey(key)) {
            loadingMap[key]?.add(onResult)
            return
        }
        loadingMap[key] = mutableListOf(onResult)
    }

    decodeExecutor.execute {
        val data = try {
            loader()
        } catch (e: Exception) {
            null
        }

        if (data != null) {
            svgaBinaryCache.put(key, data)
        }

        mainHandler.post {
            loadingMap.remove(key)?.forEach {
                it(data)
            }
        }
    }
}

/* ---------------- SVGA 播放核心 ---------------- */

private fun SVGAImageView.playCore(
    key: String,
    avatarUrl: String?,
    nickName: String?,
    loop: Int,
    callback: PlayCallback?
) {
    val playId = "$key#${System.nanoTime()}"
    tag = playId
    loops = loop
    visibility = View.VISIBLE

    Log.d(TAG, "SVGA play start key=$key playId=$playId")

    callback?.onPlayStart()

    loadSVGABytes(key, {
        FileInputStream(key).use { it.readBytes() }
    }) { bytes ->
        if (bytes == null) {
            callback?.onPlayFailed("SVGA load failed")
            return@loadSVGABytes
        }

        parser.decodeFromInputStream(
            ByteArrayInputStream(bytes),
            key,
            object : SVGAParser.ParseCompletion {
                override fun onComplete(videoItem: SVGAVideoEntity) {
                    if (tag != playId) {
                        Log.w(TAG, "SVGA abandon playId=$playId")
                        return
                    }

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
                        Glide.with(this@playCore)
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

                override fun onError() {
                    callback?.onPlayFailed("SVGA decode error")
                }
            },
            true
        )
    }

    callback?.let {
        this.callback = object : SVGACallback {
            override fun onFinished() {
                Log.d(TAG, "SVGA play complete playId=$playId")
                it.onPlayComplete()
            }

            override fun onPause() {}
            override fun onRepeat() {}
            override fun onStep(frame: Int, percentage: Double) {}
        }
    }
}

/* ---------------- 对外 API（完全不变） ---------------- */

fun SVGAImageView.playSVGAWithKey(
    url: String,
    avatarUrl: String,
    nickName: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA url empty")
        return
    }

    val cache = MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
    if (cache.exists()) {
        playCore(cache.absolutePath, avatarUrl, nickName, loop, callback)
    } else {
        OkDownloadExt.downloadSingle(url, listener = object : DownloadListener {
            override fun onComplete(file: File) {
                post {
                    playCore(file.absolutePath, avatarUrl, nickName, loop, callback)
                }
            }

            override fun onFailed(error: Throwable?) {
                callback?.onPlayFailed(error?.message)
            }
        })
    }
}

/* ---------------- PAG ---------------- */

fun PAGView.playPGA(url: String, callback: PlayCallback? = null) {
    val cache = MediaCacheManager.getPagCacheFile(AppUtil.appContext, url)
    val file = if (cache.exists()) cache else return

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

/* ---------------- MP4 (VAP) 保持你原实现 ---------------- */

fun AnimView.checkMp4Cache(
    giftMp4Url: String,
    loop: Int = 1,
    nickName: String? = "",
    avatarUrl: String? = "",
    avatarUrl2: String? = "",
    callback: PlayCallback? = null
) {
    if (TextUtils.isEmpty(giftMp4Url)) {
        callback?.onPlayFailed("MP4 url empty")
        return
    }

    val target = MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, giftMp4Url)
    if (target.exists()) {
        playMP4(target, loop, nickName, avatarUrl, avatarUrl2, callback)
    } else {
        OkDownloadExt.downloadSingle(giftMp4Url, listener = object : DownloadListener {
            override fun onComplete(file: File) {
                playMP4(file, loop, nickName, avatarUrl, avatarUrl2, callback)
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
    Log.d(TAG, "VAP playMP4 start file=${file.absolutePath}")

    stopPlay()

    setLoop(loop)

    setFetchResource(object : IFetchResource {

        override fun fetchImage(resource: Resource, result: (Bitmap?) -> Unit) {
            when (resource.tag) {
                "tag1" -> {
                    if (!avatar.isNullOrEmpty()) {
                        Glide.with(this@playMP4)
                            .asBitmap()
                            .load(avatar)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    result(resource)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    result(null)
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    result(null)
                                }
                            })
                    } else {
                        result(null)
                    }
                }

                "tag2" -> {
                    if (!avatarUrl2.isNullOrEmpty()) {
                        Glide.with(this@playMP4)
                            .asBitmap()
                            .load(avatarUrl2)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    result(resource)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    result(null)
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    result(null)
                                }
                            })
                    } else {
                        result(null)
                    }
                }

                else -> result(null)
            }
        }

        override fun fetchText(resource: Resource, result: (String?) -> Unit) {
            if (resource.tag == "tag2" || resource.tag == "tag3") {
                result(nickName ?: "")
            } else {
                result(null)
            }
        }

        override fun releaseResource(resources: List<Resource>) {}
    })

    setAnimListener(object : IAnimListener {

        override fun onVideoStart() {
            Log.d(TAG, "VAP onVideoStart")
            callback?.onPlayStart()
        }

        override fun onVideoComplete() {
            Log.d(TAG, "VAP onVideoComplete")
            callback?.onPlayComplete()
        }

        override fun onFailed(errorType: Int, errorMsg: String?) {
            Log.e(TAG, "VAP error type=$errorType msg=$errorMsg")
            callback?.onPlayFailed("AnimView error($errorType): $errorMsg")
        }

        override fun onVideoDestroy() {}
        override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {}
    })

    startPlay(file)
}

