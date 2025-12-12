package com.yuehai.util.util.svga

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import android.util.LruCache
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.opensource.svgaplayer.*
import com.opensource.svgaplayer.SVGAParser.ParseCompletion
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.tencent.qgame.animplayer.inter.IFetchResource
import com.tencent.qgame.animplayer.mix.Resource
import com.yuehai.util.AppUtil
import com.yuehai.util.util.DownloadListener
import com.yuehai.util.util.OkDownloadExt
import com.yuehai.util.util.download.MediaCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.libpag.PAGFile
import org.libpag.PAGView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "AnimPlayExt"

val parser: SVGAParser by lazy {
    SVGAParser.shareParser().apply {
        init(AppUtil.appContext)
        Log.d(TAG, "SVGAParser initialized")
    }
}

private val svgaDecodeExecutor: ExecutorService = Executors.newSingleThreadExecutor()

private const val MAX_SVGA_CACHE_COUNT = 3

private val svgaCache: LruCache<String, SVGAVideoEntity> =
    object : LruCache<String, SVGAVideoEntity>(MAX_SVGA_CACHE_COUNT) {}

private val svgaLoading = ConcurrentHashMap<String, MutableList<(SVGAVideoEntity?) -> Unit>>()

private fun getCachedSVGA(key: String) = svgaCache.get(key)
private fun cacheSVGA(key: String, video: SVGAVideoEntity) {
    svgaCache.put(key, video)
}

private fun loadSVGA(
    key: String,
    decode: (SVGAParser.ParseCompletion) -> Unit,
    onLoaded: (SVGAVideoEntity?) -> Unit
) {
    getCachedSVGA(key)?.let { onLoaded(it); return }

    synchronized(svgaLoading) {
        if (svgaLoading.containsKey(key)) {
            svgaLoading[key]?.add(onLoaded)
            return
        }
        svgaLoading[key] = mutableListOf(onLoaded)
    }

    svgaDecodeExecutor.submit {
        decode(object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                cacheSVGA(key, videoItem)
                svgaLoading.remove(key)?.forEach { it(videoItem) }
            }

            override fun onError() {
                svgaLoading.remove(key)?.forEach { it(null) }
            }
        })
    }
}

interface PlayCallback {
    fun onPlayStart() {}
    fun onPlayComplete() {}
    fun onPlayFailed(error: String?) {}
}

private const val avatarKey = "key01"
private const val nameKey = "key02"

private fun SVGAImageView.playCore(
    key: String,
    loop: Int,
    avatarUrl: String?,
    nickName: String?,
    callback: PlayCallback?,
    isLocalFile: Boolean = false
) {
    this.loops = loop
    this.visibility = View.VISIBLE

    this.callback = object : SVGACallback {
        override fun onFinished() {
            callback?.onPlayComplete()
        }

        override fun onPause() {}
        override fun onRepeat() {}
        override fun onStep(frame: Int, percentage: Double) {}
    }

    this.tag = key

    loadSVGA(key, { completion ->
        when {
            key.startsWith("assets:") -> {
                parser.decodeFromAssets(key.substringAfter("assets:"), completion)
            }

            isLocalFile -> {
                try {
                    parser.decodeFromInputStream(FileInputStream(key), key, completion, true)
                } catch (e: FileNotFoundException) {
                    completion.onError()
                }
            }

            else -> {
                try {
                    parser.decodeFromURL(URL(key), completion)
                } catch (e: Exception) {
                    completion.onError()
                }
            }
        }
    }) { videoItem ->
        if (videoItem == null) {
            this.tag = null
            callback?.onPlayFailed("SVGA decode failed for key: $key")
            return@loadSVGA
        }

        post {
            if (this.tag != key) {
                Log.w(TAG, "SVGA load complete, but View tag changed. Skipping animation start.")
                return@post
            }

            stopAnimation()

            val dynamicEntity = SVGADynamicEntity().apply {
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
                        nameKey
                    )
                }
            }

            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .asBitmap()
                    .load(avatarUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            if (this@playCore.tag != key) return

                            dynamicEntity.setDynamicImage(resource, avatarKey)
                            setVideoItem(videoItem, dynamicEntity)
                            startAnimation()
                            callback?.onPlayStart()
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            if (this@playCore.tag != key) return
                            setVideoItem(videoItem, dynamicEntity)
                            startAnimation()
                            callback?.onPlayStart()
                        }

                        override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                            if (this@playCore.tag != key) return
                            setVideoItem(videoItem, dynamicEntity)
                            startAnimation()
                            callback?.onPlayStart()
                        }
                    })
            } else {
                setVideoItem(videoItem, dynamicEntity)
                startAnimation()
                callback?.onPlayStart()
            }
        }
    }
}

fun SVGAImageView.playSVGA(
    url: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA URL is empty"); return
    }
    val cacheFile = MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
    if (cacheFile.exists() && cacheFile.length() > 0) {
        playCore(cacheFile.absolutePath, loop, null, null, callback, isLocalFile = true)
    } else {
        OkDownloadExt.downloadSingle(url, listener = object : DownloadListener {
            override fun onComplete(file: File) {
                this@playSVGA.post {
                    playCore(file.absolutePath, loop, null, null, callback, isLocalFile = true)
                }
            }

            override fun onFailed(error: Throwable?) {
                this@playSVGA.post {
                    callback?.onPlayFailed(error?.message)
                }
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
        callback?.onPlayFailed("SVGA URL is empty"); return
    }
    val cacheFile = MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
    if (cacheFile.exists() && cacheFile.length() > 0) {
        playCore(cacheFile.absolutePath, loop, avatarUrl, nickName, callback, isLocalFile = true)
    } else {
        OkDownloadExt.downloadSingle(url, listener = object : DownloadListener {
            override fun onComplete(file: File) {
                this@playSVGAWithKey.post {
                    playCore(file.absolutePath, loop, avatarUrl, nickName, callback, isLocalFile = true)
                }
            }

            override fun onFailed(error: Throwable?) {
                this@playSVGAWithKey.post {
                    callback?.onPlayFailed(error?.message)
                }
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
    useCache: Boolean = false,
) {
    if (file == null) {
        callback?.onPlayFailed("SVGA file is null"); return
    }
    playCore(file.absolutePath, loop, avatarUrl, nickName, callback, isLocalFile = true)
}

fun SVGAImageView.playAssetsSVGA(
    assetName: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (assetName.isEmpty()) {
        callback?.onPlayFailed("SVGA asset name is empty"); return
    }
    playCore("assets:$assetName", loop, null, null, callback)
}

fun SVGAImageView.playSVGAFromInternet(
    url: String,
    loop: Int = 1,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA URL is empty"); return
    }
    playCore(url, loop, null, null, callback)
}

fun SVGAImageView.playSVGAWithKeyFromInternet(
    url: String,
    loop: Int = 1,
    avatarUrl: String? = "",
    nickName: String? = null,
    callback: PlayCallback? = null
) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("SVGA URL is empty"); return
    }
    playCore(url, loop, avatarUrl, nickName, callback)
}

fun PAGView.playPGA(url: String, callback: PlayCallback? = null) {
    if (url.isEmpty()) {
        callback?.onPlayFailed("PAG URL is empty")
        return
    }
    val cacheFile = MediaCacheManager.getPagCacheFile(AppUtil.appContext, url)
    if (cacheFile.exists()) {
        this.post {
            playPAGInternal(cacheFile, callback)
        }
    } else {
        OkDownloadExt.downloadSingle(url, listener = object : DownloadListener {
            override fun onComplete(file: File) {
                this@playPGA.post {
                    playPAGInternal(file, callback)
                }
            }

            override fun onFailed(error: Throwable?) {
                this@playPGA.post {
                    callback?.onPlayFailed(error?.message)
                }
            }
        })
    }
}

private fun PAGView.playPAGInternal(file: File, callback: PlayCallback?) {
    try {
        val pagFile = PAGFile.Load(file.absolutePath)
        this.composition = pagFile
        this.setRepeatCount(Integer.MAX_VALUE)
        this.addListener(object : PAGView.PAGViewListener {
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
        this.play()
    } catch (e: Exception) {
        callback?.onPlayFailed(e.message)
    }
}


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
    val targetFile = MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, giftMp4Url)
    if (targetFile.exists() && targetFile.length() > 0) {
        this.post {
            playMP4(targetFile, loop, nickName, avatarUrl, avatarUrl2, callback)
        }
    } else {
        startDownLoadFile(giftMp4Url, targetFile, loop, nickName, avatarUrl, avatarUrl2, callback)
    }
}


private fun AnimView.startDownLoadFile(
    url: String,
    targetFile: File,
    loop: Int,
    nickName: String?,
    avatarUrl: String?,
    avatarUrl2: String?,
    callback: PlayCallback?
) {
    OkDownloadExt.downloadSingle(url, listener = object : DownloadListener {
        override fun onComplete(file: File) {
            this@startDownLoadFile.post {
                playMP4(file, loop, nickName, avatarUrl, avatarUrl2, callback)
            }
        }

        override fun onFailed(error: Throwable?) {
            this@startDownLoadFile.post {
                callback?.onPlayFailed(error?.message)
            }
        }
    })
}

private fun AnimView.playMP4(
    file: File,
    loop: Int,
    nickName: String?,
    avatar: String?,
    avatarUrl2: String?,
    callback: PlayCallback?
) {
    setLoop(loop)
    setFetchResource(object : IFetchResource {
        override fun fetchImage(resource: Resource, result: (Bitmap?) -> Unit) {
            if (resource.tag == "tag1" && !avatar.isNullOrEmpty()) {
                Glide.with(context)
                    .asBitmap()
                    .load(avatar)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            result(resource)
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            result(null)
                        }

                        override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                            result(null)
                        }
                    })
            } else if (resource.tag == "tag2" && !avatarUrl2.isNullOrEmpty()) {
                Glide.with(context)
                    .asBitmap()
                    .load(avatar)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            result(resource)
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            result(null)
                        }

                        override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                            result(null)
                        }
                    })
            } else {
                result(null)
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