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
import java.io.*
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.max

interface DownloadListener {
    fun onProgress(progress: Int) {}
    fun onComplete(file: File)
    fun onFailed(error: Throwable?)
}

object OkDownloadExt {

    private const val TAG = "OkDownloadExt"

    private const val MAX_PARALLEL_DOWNLOAD = 10
    private const val MAX_RETRY = 3

    private const val SMALL_FILE_THRESHOLD = 200 * 1024L
    private const val PROGRESS_SAVE_BYTES = 1024 * 1024L
    private const val PROGRESS_SAVE_INTERVAL = 1000L

    private val SAFE_EXTENSIONS =
        setOf("svga", "mp4", "gif", "png", "jpg", "jpeg", "webp")

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val globalMutex = Mutex()

    private val downloadQueue = mutableListOf<DownloadItem>()
    private val waitingSet = mutableSetOf<String>()
    private val downloadingTasks = mutableMapOf<String, Job>()

    private val pausedItems = ConcurrentHashMap<String, DownloadItem>()
    private val pausedSet = Collections.newSetFromMap<String>(ConcurrentHashMap())

    private val retryCountMap = ConcurrentHashMap<String, Int>()
    private val callbackMap =
        ConcurrentHashMap<String, MutableList<WeakReference<DownloadListener>>>()

    @Volatile
    private var runningCount = 0

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    data class DownloadItem(
        val url: String,
        val targetDir: File,
        val priority: Int,
        val md5: String?
    )

    /* ===================== 核心 API (完全未改动) ===================== */

    fun downloadSingle(
        url: String,
        targetDir: File = MediaCacheManager.defaultDir,
        listener: DownloadListener? = null,
        md5: String? = null
    ) {
        if (url.isBlank()) return
        addCallback(url, listener)
        scope.launch {
            globalMutex.withLock {
                if (waitingSet.contains(url) || downloadingTasks.containsKey(url)) {
                    startNextLocked()
                    return@withLock
                }
                val item = DownloadItem(url, targetDir, getPriority(url), md5)
                waitingSet.add(url)
                downloadQueue.add(item)
                downloadQueue.sortByDescending { it.priority }
                startNextLocked()
            }
        }
    }

    fun pause(url: String) {
        pausedSet.add(url)
        scope.launch {
            globalMutex.withLock {
                downloadQueue.find { it.url == url }?.let { pausedItems[url] = it }
                waitingSet.remove(url)
                downloadQueue.removeAll { it.url == url }
                stopRunningTaskLocked(url)
            }
        }
    }

    fun resume(url: String) {
        val item = pausedItems.remove(url) ?: return
        pausedSet.remove(url)
        downloadSingle(item.url, item.targetDir, null, item.md5)
    }

    fun cancel(url: String) {
        pausedSet.remove(url)
        pausedItems.remove(url)
        retryCountMap.remove(url)
        callbackMap.remove(url)
        scope.launch {
            globalMutex.withLock {
                waitingSet.remove(url)
                downloadQueue.removeAll { it.url == url }
                stopRunningTaskLocked(url)
            }
        }
        val file = getTargetFile(url, MediaCacheManager.defaultDir)
        File(file.absolutePath + ".part").delete()
        File(file.absolutePath + ".cfg").delete()
        file.delete()
    }

    /* ===================== 调度内核 ===================== */

    private fun startNextLocked() {
        while (runningCount < MAX_PARALLEL_DOWNLOAD && downloadQueue.isNotEmpty()) {
            val item = downloadQueue.removeAt(0)
            waitingSet.remove(item.url)
            if (pausedSet.contains(item.url)) continue
            launchTaskLocked(item)
        }
    }

    private fun launchTaskLocked(item: DownloadItem) {
        runningCount++
        val job = scope.launch {
            try {
                val file = getTargetFile(item.url, item.targetDir)
                if (file.exists() && file.length() > 0) {
                    val valid = item.md5 == null || calculateMD5(file) == item.md5
                    if (valid) {
                        dispatchComplete(item.url, file)
                        return@launch
                    } else {
                        file.delete()
                    }
                }
                if (downloadWithRetry(item, file)) {
                    dispatchComplete(item.url, file)
                } else {
                    dispatchFail(item.url, IOException("Max retries or manual stop"))
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "⛔ cancelled: ${item.url}")
            } catch (e: Exception) {
                dispatchFail(item.url, e)
            } finally {
                globalMutex.withLock {
                    downloadingTasks.remove(item.url)
                    runningCount = max(0, runningCount - 1)
                    startNextLocked()
                }
            }
        }
        downloadingTasks[item.url] = job
    }

    private fun stopRunningTaskLocked(url: String) {
        downloadingTasks.remove(url)?.cancel()
    }

    /* ===================== 下载实现 (核心修复点) ===================== */

    private suspend fun downloadWithRetry(item: DownloadItem, file: File): Boolean {
        var retry = retryCountMap[item.url] ?: 0
        while (retry < MAX_RETRY) {
            try {
                processDownload(item, file)
                retryCountMap.remove(item.url)
                return true
            } catch (e: CancellationException) {
                return false
            } catch (e: Exception) {
                retry++
                retryCountMap[item.url] = retry
                delay(1000L * retry)
            }
        }
        return false
    }

    private suspend fun processDownload(item: DownloadItem, file: File) =
        withContext(Dispatchers.IO) {
            val tmp = File(file.absolutePath + ".part")
            val cfg = File(file.absolutePath + ".cfg")

            var startBytes = 0L
            if (tmp.exists() && cfg.exists()) {
                val savedBytes = cfg.readText().toLongOrNull() ?: 0L
                if (savedBytes > 0 && savedBytes == tmp.length()) {
                    startBytes = savedBytes
                } else {
                    tmp.delete(); cfg.delete()
                }
            } else {
                tmp.delete(); cfg.delete()
            }

            val request = Request.Builder()
                .url(item.url)
                .apply { if (startBytes > 0) addHeader("Range", "bytes=$startBytes-") }
                .build()

            client.newCall(request).execute().use { resp ->
                // 修复点 1：处理非 20x 的错误码
                if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")

                val body = resp.body ?: throw IOException("Empty body")
                val contentLen = body.contentLength()

                // 修复点 2：判断是否支持断点续传 (206 表示支持，200 表示不支持或无视 Range)
                val isPartial = resp.code == 206
                val actualStartBytes = if (isPartial) startBytes else 0L
                val totalLen = if (isPartial) startBytes + contentLen else contentLen

                if (!ensureSpace(file.parentFile!!, if (contentLen > 0) contentLen else SMALL_FILE_THRESHOLD)) {
                    throw IOException("Insufficient storage space")
                }

                RandomAccessFile(tmp, "rw").use { raf ->
                    // 如果服务器不支持 Range (返回200)，则清空文件从头写
                    if (!isPartial) raf.setLength(0)

                    raf.seek(actualStartBytes)

                    val buf = ByteArray(16 * 1024)
                    var downloaded = actualStartBytes
                    var lastProgress = -1
                    var lastSaveTime = System.currentTimeMillis()

                    body.byteStream().use { input ->
                        var len: Int
                        while (input.read(buf).also { len = it } != -1) {
                            ensureActive()
                            if (pausedSet.contains(item.url)) throw CancellationException()

                            raf.write(buf, 0, len)
                            downloaded += len

                            if (totalLen > 0) {
                                val p = ((downloaded * 100) / totalLen).toInt()
                                if (p > lastProgress) {
                                    lastProgress = p
                                    dispatchProgress(item.url, p)
                                }
                            }

                            val now = System.currentTimeMillis()
                            if (downloaded % PROGRESS_SAVE_BYTES == 0L || now - lastSaveTime > PROGRESS_SAVE_INTERVAL) {
                                cfg.writeText(downloaded.toString())
                                lastSaveTime = now
                            }
                        }
                    }
                    raf.fd.sync() // 强制落盘
                }

                if (item.md5 != null) {
                    val actualMd5 = calculateMD5(tmp)
                    if (!actualMd5.equals(item.md5, true)) {
                        tmp.delete(); cfg.delete()
                        throw IOException("MD5 Verify Failed")
                    }
                }

                if (file.exists()) file.delete()
                if (!tmp.renameTo(file)) {
                    tmp.copyTo(file, overwrite = true)
                    tmp.delete()
                }
                cfg.delete()
            }
        }

    /* ===================== 工具 (保持一致) ===================== */

    private fun addCallback(url: String, listener: DownloadListener?) {
        if (listener == null) return
        val list = callbackMap.getOrPut(url) { Collections.synchronizedList(mutableListOf()) }
        list.add(WeakReference(listener))
    }

    private fun dispatchProgress(url: String, progress: Int) {
        val callbacks = callbackMap[url] ?: return
        mainHandler.post {
            val it = callbacks.iterator()
            while (it.hasNext()) {
                val l = it.next().get()
                if (l == null) it.remove() else l.onProgress(progress)
            }
        }
    }

    private fun dispatchComplete(url: String, file: File) {
        mainHandler.post { callbackMap.remove(url)?.forEach { it.get()?.onComplete(file) } }
    }

    private fun dispatchFail(url: String, e: Throwable?) {
        mainHandler.post { callbackMap.remove(url)?.forEach { it.get()?.onFailed(e) } }
    }

    private fun calculateMD5(file: File): String? = try {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use {
            val buf = ByteArray(8192)
            var len: Int
            while (it.read(buf).also { len = it } != -1) { md.update(buf, 0, len) }
        }
        md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) { null }

    private fun ensureSpace(dir: File, need: Long): Boolean {
        if (getAvailableSpace(dir) >= need) return true
        evictOldCaches(dir, need)
        return getAvailableSpace(dir) >= need
    }

    private fun evictOldCaches(dir: File, need: Long) {
        val files = dir.listFiles { f ->
            f.isFile && SAFE_EXTENSIONS.contains(f.extension.lowercase()) &&
                    !f.name.endsWith(".part") && !f.name.endsWith(".cfg")
        } ?: return
        for (f in files.sortedBy { it.lastModified() }) {
            f.delete()
            if (getAvailableSpace(dir) >= need) break
        }
    }

    private fun getAvailableSpace(dir: File): Long = try {
        val stat = StatFs(dir.path)
        stat.availableBlocksLong * stat.blockSizeLong
    } catch (e: Exception) { 0L }

    private fun getTargetFile(url: String, dir: File): File = when {
        url.endsWith(".svga") -> MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
        url.endsWith(".mp4") -> MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, url)
        else -> File(dir, url.substringAfterLast("/"))
    }

    private fun getPriority(url: String): Int = when {
        url.endsWith(".svga") || url.endsWith(".gif") -> 10
        url.endsWith(".png") || url.endsWith(".jpg") -> 5
        url.endsWith(".mp4") -> 1
        else -> 3
    }
}