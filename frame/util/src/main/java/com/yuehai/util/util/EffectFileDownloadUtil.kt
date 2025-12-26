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
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * 强化版下载工具类
 * 修复点：
 * 1. 同路径并发下载锁 (fileLocks)
 * 2. 严格的 MD5 与文件长度校验
 * 3. 临时文件 (.part) 原子化重命名
 * 4. 完整的生命周期日志监控
 */
interface DownloadListener {
    fun onProgress(progress: Int) {}
    fun onComplete(file: File)
    fun onFailed(error: Throwable?)
}

object OkDownloadExt {

    private const val TAG = "OkDownloadExt"
    private val mainHandler = Handler(Looper.getMainLooper())

    private val downloadQueue = ConcurrentLinkedQueue<DownloadItem>()
    private val downloadingTasks = ConcurrentHashMap<String, Job>()
    private val callbackMap = ConcurrentHashMap<String, CopyOnWriteArrayList<DownloadListener>>()
    private val retryCountMap = ConcurrentHashMap<String, Int>()
    private val pausedSet: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    // 文件操作锁：防止多个 URL 同时写同一个 File 地址
    private val fileLocks = ConcurrentHashMap<String, Mutex>()

    private const val MAX_PARALLEL_DOWNLOAD = 5
    private val currentParallel = AtomicInteger(0)
    private const val MAX_RETRY = 3

    private val queueMutex = Mutex()
    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    data class DownloadItem(
        val url: String,
        val targetDir: File,
        val listener: DownloadListener?,
        val priority: Int = 0,
        val md5: String? = null
    )

    private fun getPriority(url: String): Int = when {
        url.endsWith(".svga") || url.endsWith(".gif") -> 10
        url.endsWith(".png") || url.endsWith(".jpg") -> 5
        url.endsWith(".mp4") -> 1
        else -> 3
    }

    /**
     * 发起单任务下载
     * 无论文件是否存在，最终都会通过 onComplete 回调
     */
    fun downloadSingle(
        url: String,
        targetDir: File = MediaCacheManager.defaultDir,
        listener: DownloadListener? = null,
        md5: String? = null
    ) {
        if (url.isEmpty()) return
        addDownloadCallback(url, listener)

        // 检查是否已经在队列或下载中
        if (downloadQueue.any { it.url == url } || downloadingTasks.containsKey(url)) {
            Log.d(TAG, "[Skip] Already in progress: $url")
            return
        }

        val item = DownloadItem(url, targetDir, listener, getPriority(url), md5)
        downloadQueue.offer(item)
        startQueueIfNeeded()
    }

    fun pause(url: String) {
        Log.i(TAG, "[User] Pause requested for: $url")
        pausedSet.add(url)
        downloadingTasks[url]?.cancel()
    }

    fun cancel(url: String) {
        Log.i(TAG, "[User] Cancel requested for: $url")
        pausedSet.remove(url)
        downloadingTasks[url]?.cancel()
        downloadingTasks.remove(url)
        callbackMap.remove(url)

        val file = getTargetFile(url, MediaCacheManager.defaultDir)
        val tmpFile = File(file.absolutePath + ".part")
        if (tmpFile.exists()) tmpFile.delete()
    }

    private fun addDownloadCallback(url: String, listener: DownloadListener?) {
        if (listener == null) return
        val list = callbackMap.getOrPut(url) { CopyOnWriteArrayList() }
        if (!list.contains(listener)) list.add(listener)
    }

    private fun getTargetFile(url: String, targetDir: File): File = when {
        url.endsWith(".svga") -> MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
        url.endsWith(".mp4") -> MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, url)
        else -> File(targetDir, url.substringAfterLast("/"))
    }

    private fun startQueueIfNeeded() {
        scope.launch {
            queueMutex.withLock {
                if (currentParallel.get() < MAX_PARALLEL_DOWNLOAD) {
                    val nextItem = downloadQueue.poll() ?: return@withLock
                    if (pausedSet.contains(nextItem.url)) {
                        startQueueIfNeeded()
                        return@withLock
                    }
                    startDownload(nextItem)
                }
            }
        }
    }

    private fun startDownload(item: DownloadItem) {
        val targetFile = getTargetFile(item.url, item.targetDir)

        val job = scope.launch {
            Log.d(TAG, "=====> [New Task] ${item.url}")

            // 核心：路径排他锁
            val fileLock = fileLocks.getOrPut(targetFile.absolutePath) { Mutex() }
            fileLock.withLock {
                try {
                    currentParallel.incrementAndGet()

                    // 1. 检查已存在的文件 (缓存逻辑)
                    if (targetFile.exists() && targetFile.length() > 0) {
                        Log.d(TAG, "[Check] File exists (${targetFile.length()}B). Validating...")
                        if (validateFile(targetFile, item.md5)) {
                            Log.i(TAG, "[Cache] Valid file found. Notify success.")
                            notifySuccess(item.url, targetFile)
                            return@withLock
                        } else {
                            Log.w(TAG, "[Check] File invalid. Deleting: ${targetFile.name}")
                            targetFile.delete()
                        }
                    }

                    // 2. 获取文件长度与是否支持断点
                    Log.d(TAG, "[Network] Fetching HEAD...")
                    val headResp = client.newCall(Request.Builder().url(item.url).head().build()).execute()
                    val contentLength = headResp.header("Content-Length")?.toLongOrNull() ?: -1L
                    val acceptRanges = headResp.header("Accept-Ranges")?.contains("bytes") == true
                    headResp.close()

                    if (contentLength > 0 && !hasEnoughSpace(item.targetDir, contentLength)) {
                        throw IOException("Disk space insufficient. Need $contentLength")
                    }

                    // 3. 带重试机制的下载
                    val success = downloadWithRetry(item, targetFile, contentLength, acceptRanges)
                    if (success) {
                        Log.i(TAG, "[Done] Download success: ${targetFile.name}")
                        notifySuccess(item.url, targetFile)
                    } else {
                        notifyFailed(item.url, IOException("Download failed after all retries"))
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "[Error] Task failed: ${e.message}")
                    notifyFailed(item.url, e)
                } finally {
                    downloadingTasks.remove(item.url)
                    currentParallel.decrementAndGet()
                    startQueueIfNeeded()
                }
            }
        }
        downloadingTasks[item.url] = job
    }

    private suspend fun downloadWithRetry(item: DownloadItem, file: File, contentLength: Long, acceptRanges: Boolean): Boolean {
        var count = 0
        val tmpFile = File(file.absolutePath + ".part")

        while (count < MAX_RETRY) {
            try {
                if (pausedSet.contains(item.url)) return false

                Log.d(TAG, "[Retry] Attempt ${count + 1}/$MAX_RETRY")
                performDownload(item, tmpFile, contentLength, acceptRanges)

                // 严格校验大小
                if (contentLength > 0 && tmpFile.length() != contentLength) {
                    throw IOException("Size mismatch! Expected $contentLength, but got ${tmpFile.length()}")
                }

                // 严格校验 MD5
                if (!validateFile(tmpFile, item.md5)) {
                    throw IOException("MD5 check failed")
                }

                // 原子重命名
                if (tmpFile.renameTo(file)) {
                    return true
                } else {
                    if (file.exists()) return true
                    throw IOException("Could not rename .part to final file")
                }

            } catch (e: Exception) {
                count++
                Log.e(TAG, "[Retry] Attempt $count failed: ${e.message}")
                if (count >= MAX_RETRY) {
                    tmpFile.delete()
                    return false
                }
                delay(1500L * count) // 递增重试延迟
            }
        }
        return false
    }

    private suspend fun performDownload(item: DownloadItem, tmpFile: File, contentLength: Long, acceptRanges: Boolean) = withContext(Dispatchers.IO) {
        val currentLen = if (tmpFile.exists()) tmpFile.length() else 0L

        if (!acceptRanges || contentLength <= 0) {
            Log.d(TAG, "[Download] Range not supported. Full download.")
            downloadFull(item.url, tmpFile)
            return@withContext
        }

        // 超过 20MB 使用多线程(3线程)，否则单线程下载
        val threadCount = if (contentLength > 20 * 1024 * 1024) 3 else 1
        val blockSize = contentLength / threadCount
        val progressMap = LongArray(threadCount) { 0L }

        coroutineScope {
            (0 until threadCount).map { i ->
                async {
                    val start = i * blockSize
                    val end = if (i == threadCount - 1) contentLength - 1 else (start + blockSize - 1)

                    // 如果临时文件已经写过该块，计算起始偏移
                    if (currentLen > end) {
                        progressMap[i] = end - start + 1
                        return@async
                    }

                    val rangeStart = start + (if (currentLen > start) currentLen - start else 0L)
                    if (rangeStart > end) return@async

                    Log.d(TAG, "[Thread-$i] Downloading range: $rangeStart-$end")
                    val request = Request.Builder()
                        .url(item.url)
                        .addHeader("Range", "bytes=$rangeStart-$end")
                        .build()

                    client.newCall(request).execute().use { resp ->
                        // 校验：请求 Range 必须返回 206
                        if (resp.code != 206 && rangeStart > 0) throw IOException("Server ignored Range request (Code ${resp.code})")

                        val body = resp.body ?: throw IOException("Empty body")
                        RandomAccessFile(tmpFile, "rw").use { raf ->
                            raf.seek(rangeStart)
                            body.byteStream().use { input ->
                                val buffer = ByteArray(8192)
                                var len: Int
                                while (input.read(buffer).also { len = it } != -1) {
                                    if (pausedSet.contains(item.url)) throw CancellationException()
                                    raf.write(buffer, 0, len)
                                    progressMap[i] += len.toLong()

                                    val totalDownloaded = progressMap.sum()
                                    val progress = (totalDownloaded * 100 / contentLength).toInt()
                                    mainHandler.post { callbackMap[item.url]?.forEach { it.onProgress(progress) } }
                                }
                            }
                        }
                    }
                }
            }.awaitAll()
        }
    }

    private fun downloadFull(url: String, tmpFile: File) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
            val body = resp.body ?: throw IOException("Empty body")
            tmpFile.delete()
            body.byteStream().use { input ->
                tmpFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun validateFile(file: File, expectedMd5: String?): Boolean {
        if (!file.exists()) return false
        if (expectedMd5 == null) {
            val valid = file.length() > 0
            Log.d(TAG, "[Validate] No MD5, checking length: $valid")
            return valid
        }
        val actualMd5 = calculateMD5(file)
        val match = actualMd5.equals(expectedMd5, true)
        Log.d(TAG, "[Validate] MD5 Result: $match (Actual: $actualMd5)")
        return match
    }

    private fun calculateMD5(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            file.inputStream().use { fis ->
                val buffer = ByteArray(8192)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun notifySuccess(url: String, file: File) {
        mainHandler.post {
            Log.i(TAG, "[Callback] Success: $url")
            callbackMap[url]?.forEach { it.onComplete(file) }
            callbackMap.remove(url)
        }
    }

    private fun notifyFailed(url: String, e: Throwable) {
        mainHandler.post {
            Log.e(TAG, "[Callback] Failed: $url -> ${e.message}")
            callbackMap[url]?.forEach { it.onFailed(e) }
            callbackMap.remove(url)
        }
    }

    private fun hasEnoughSpace(directory: File, requiredSize: Long): Boolean {
        return try {
            if (!directory.exists()) directory.mkdirs()
            val stat = StatFs(directory.path)
            stat.availableBlocksLong * stat.blockSizeLong > requiredSize
        } catch (e: Exception) { true }
    }
}