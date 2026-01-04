package com.yuehai.util.util

import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.util.Log
import com.yuehai.util.AppUtil
import com.yuehai.util.util.download.MediaCacheManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

interface DownloadListener {
    fun onProgress(progress: Int) {}
    fun onComplete(file: File)
    fun onFailed(error: Throwable?)
}

object OkDownloadExt {

    private const val TAG = "OkDownloadExt"

    private const val MAX_PARALLEL_DOWNLOAD = 10
    private const val MAX_RETRY = 3

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 下载队列（真正支持 priority） */
    private val downloadQueue =
        PriorityQueue<DownloadItem>(compareByDescending { it.priority })

    /** url -> Job（占位锁 + 真实任务） */
    private val downloadingTasks = ConcurrentHashMap<String, Job>()

    /** url -> callbacks */
    private val callbackMap =
        ConcurrentHashMap<String, CopyOnWriteArrayList<DownloadListener>>()

    private val retryCountMap = ConcurrentHashMap<String, Int>()
    private val pausedSet = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    private val queueMutex = Mutex()
    private val currentParallel = AtomicInteger(0)

    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    private val PLACEHOLDER_JOB = Job()

    data class DownloadItem(
        val url: String,
        val targetDir: File,
        val priority: Int,
        val md5: String?
    )

    // ---------------- API ----------------

    fun downloadSingle(
        url: String,
        targetDir: File = MediaCacheManager.defaultDir,
        listener: DownloadListener? = null,
        md5: String? = null
    ) {
        if (url.isBlank()) return

        addCallback(url, listener)

        if (downloadingTasks.containsKey(url)) {
            Log.d(TAG, "[enqueue] already downloading: $url")
            return
        }

        synchronized(downloadQueue) {
            if (downloadQueue.any { it.url == url }) {
                Log.d(TAG, "[enqueue] already queued: $url")
                return
            }
            downloadQueue.offer(
                DownloadItem(url, targetDir, getPriority(url), md5)
            )
        }

        Log.d(TAG, "[enqueue] $url")
        tryStartNext()
    }

    fun cancel(url: String) {
        pausedSet.remove(url)
        downloadingTasks.remove(url)?.cancel()
        callbackMap.remove(url)
        retryCountMap.remove(url)

        synchronized(downloadQueue) {
            downloadQueue.removeAll { it.url == url }
        }

        getTargetFile(url).delete()
        File(getTargetFile(url).absolutePath + ".part").delete()

        Log.d(TAG, "[cancel] $url")
    }

    // ---------------- Core ----------------

    private fun tryStartNext() {
        scope.launch {
            queueMutex.withLock {
                if (currentParallel.get() >= MAX_PARALLEL_DOWNLOAD) return@withLock

                val item = synchronized(downloadQueue) {
                    downloadQueue.poll()
                } ?: return@withLock

                if (pausedSet.contains(item.url)) {
                    Log.d(TAG, "[skip paused] ${item.url}")
                    synchronized(downloadQueue) { downloadQueue.offer(item) }
                    return@withLock
                }

                if (downloadingTasks.putIfAbsent(item.url, PLACEHOLDER_JOB) != null) {
                    Log.w(TAG, "[duplicate blocked] ${item.url}")
                    return@withLock
                }

                startDownload(item)
            }
        }
    }

    private fun startDownload(item: DownloadItem) {
        val file = getTargetFile(item.url)
        currentParallel.incrementAndGet()

        Log.d(
            TAG,
            "[start] ${item.url}, parallel=${currentParallel.get()}"
        )

        val job = scope.launch {
            try {
                val success = downloadWithRetry(item, file)
                dispatchResult(item.url, success, file)
            } catch (e: Exception) {
                dispatchError(item.url, e)
            } finally {
                cleanup(item.url)
            }
        }

        downloadingTasks[item.url] = job
    }

    private suspend fun downloadWithRetry(item: DownloadItem, file: File): Boolean {
        var retry = retryCountMap[item.url] ?: 0
        while (retry < MAX_RETRY) {
            try {
                downloadFile(item.url, file)
                retryCountMap.remove(item.url)
                return true
            } catch (e: Exception) {
                retry++
                retryCountMap[item.url] = retry
                Log.e(TAG, "[retry $retry] ${item.url}", e)
                if (pausedSet.contains(item.url)) return false
            }
        }
        return false
    }

    // ---------------- Download ----------------

    private fun downloadFile(url: String, file: File) {
        val tmp = File(file.absolutePath + ".part")
        val req = Request.Builder().url(url).build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
            val body = resp.body ?: throw IOException("Empty body")
            body.byteStream().use { input ->
                RandomAccessFile(tmp, "rw").use { raf ->
                    val buf = ByteArray(8192)
                    var len: Int
                    while (input.read(buf).also { len = it } != -1) {
                        if (pausedSet.contains(url)) throw CancellationException()
                        raf.write(buf, 0, len)
                    }
                }
            }
        }
        tmp.renameTo(file)
    }

    // ---------------- Utils ----------------

    private fun cleanup(url: String) {
        downloadingTasks.remove(url)
        callbackMap.remove(url)
        retryCountMap.remove(url)
        currentParallel.decrementAndGet()

        Log.d(TAG, "[finish] $url")
        tryStartNext()
    }

    private fun dispatchResult(url: String, success: Boolean, file: File) {
        val callbacks = callbackMap[url]
        mainHandler.post {
            callbacks?.forEach {
                if (success) it.onComplete(file)
                else it.onFailed(IOException("Download failed"))
            }
        }
    }

    private fun dispatchError(url: String, e: Throwable) {
        val callbacks = callbackMap[url]
        mainHandler.post {
            callbacks?.forEach { it.onFailed(e) }
        }
    }

    private fun addCallback(url: String, listener: DownloadListener?) {
        if (listener == null) return
        callbackMap.getOrPut(url) { CopyOnWriteArrayList() }.add(listener)
    }

    private fun getTargetFile(url: String): File =
        when {
            url.endsWith(".svga") -> MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
            url.endsWith(".mp4") -> MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, url)
            else -> File(MediaCacheManager.defaultDir, url.substringAfterLast("/"))
        }

    private fun getPriority(url: String): Int = when {
        url.endsWith(".svga") || url.endsWith(".gif") -> 10
        url.endsWith(".png") || url.endsWith(".jpg") -> 5
        url.endsWith(".mp4") -> 1
        else -> 3
    }
}
