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
import kotlin.math.min

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

    private const val MAX_PARALLEL_DOWNLOAD = 5 // 适当调低并发防止 IO 阻塞
    private val currentParallel = AtomicInteger(0)
    private const val MAX_RETRY = 3

    private val queueMutex = Mutex()
    private val client = OkHttpClient.Builder().retryOnConnectionFailure(true).build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    data class DownloadItem(
        val url: String,
        val targetDir: File,
        val listener: DownloadListener?,
        val priority: Int = 0,
        val md5: String? = null
    )

    fun downloadSingle(
        url: String,
        targetDir: File = MediaCacheManager.defaultDir,
        listener: DownloadListener? = null,
        md5: String? = null
    ) {
        if (url.isEmpty()) return

        // 1. 统一管理回调
        addDownloadCallback(url, listener)

        // 2. 检查本地是否已经有完整文件（缓存命中）
        val file = getTargetFile(url, targetDir)
        if (file.exists() && file.length() > 0) {
            Log.d(TAG, "[downloadSingle] Cache hit, notifying all: $url")
            notifyAllComplete(url, file)
            return
        }

        // 3. 检查是否已经在队列或下载中
        if (downloadQueue.any { it.url == url } || downloadingTasks.containsKey(url)) {
            Log.d(TAG, "[downloadSingle] Already processing: $url")
            return
        }

        val item = DownloadItem(url, targetDir, null, getPriority(url), md5)
        downloadQueue.offer(item)
        startQueueIfNeeded()
    }

    private fun startQueueIfNeeded() {
        scope.launch {
            queueMutex.withLock {
                if (currentParallel.get() < MAX_PARALLEL_DOWNLOAD) {
                    val nextItem = downloadQueue.poll() ?: return@withLock
                    if (pausedSet.contains(nextItem.url)) {
                        downloadQueue.offer(nextItem)
                        return@withLock
                    }
                    startDownload(nextItem)
                }
            }
        }
    }

    private fun startDownload(item: DownloadItem) {
        val file = getTargetFile(item.url, item.targetDir)

        // 双重检查文件
        if (file.exists() && file.length() > 0) {
            notifyAllComplete(item.url, file)
            startQueueIfNeeded()
            return
        }

        // 防止重复开启 Job
        if (downloadingTasks.containsKey(item.url)) return

        currentParallel.incrementAndGet()
        val job = scope.launch {
            try {
                Log.d(TAG, "[startDownload] Executing: ${item.url}")
                val headResp = client.newCall(Request.Builder().url(item.url).head().build()).execute()
                val contentLength = headResp.header("Content-Length")?.toLongOrNull() ?: -1
                val acceptRanges = headResp.header("Accept-Ranges")?.contains("bytes") == true
                headResp.close()

                if (contentLength > 0 && !hasEnoughSpace(item.targetDir, contentLength)) {
                    throw IOException("Not enough disk space")
                }

                val success = downloadWithRetry(item, file, contentLength, acceptRanges)
                if (success && file.exists()) {
                    notifyAllComplete(item.url, file)
                } else {
                    notifyAllFailed(item.url, IOException("Download failed or file deleted"))
                }
            } catch (e: Exception) {
                notifyAllFailed(item.url, e)
            } finally {
                downloadingTasks.remove(item.url)
                currentParallel.decrementAndGet()
                startQueueIfNeeded()
            }
        }
        downloadingTasks[item.url] = job
    }

    private suspend fun downloadWithRetry(item: DownloadItem, file: File, contentLength: Long, acceptRanges: Boolean): Boolean {
        var count = 0
        while (count < MAX_RETRY) {
            try {
                // 核心修复：内部增加对文件状态的精细控制
                downloadFileInternal(item.url, file, contentLength, acceptRanges)

                if (item.md5 != null) {
                    val fileMD5 = withContext(Dispatchers.Default) { calculateMD5(file) }
                    if (fileMD5 == null || !fileMD5.equals(item.md5, true)) {
                        file.delete()
                        throw IOException("MD5 mismatch")
                    }
                }
                return true
            } catch (e: Exception) {
                Log.e(TAG, "[Retry $count] Failed: ${item.url}, error: ${e.message}")
                count++
                if (pausedSet.contains(item.url)) return false
                delay(1000) // 重试间隔
            }
        }
        return false
    }

    private suspend fun downloadFileInternal(url: String, file: File, contentLength: Long, acceptRanges: Boolean) =
        withContext(Dispatchers.IO) {
            val tmpFile = File(file.absolutePath + ".part")

            // 修正：如果 .part 文件长度等于 contentLength 但文件并不完整（全是0），应删除重来
            // 由于 setLength 会填充 0，无法通过长度判断完整性，除非有特定的进度记录文件
            // 这里我们简化处理：如果不支持断点，或者文件明显异常，清空重下
            if (!acceptRanges || !tmpFile.exists()) {
                tmpFile.delete()
            }

            if (contentLength <= 0 || !acceptRanges) {
                downloadSingleThread(url, file, contentLength)
            } else {
                // 多线程逻辑：注意不要在还没写数据时就 rename
                performMultiThreadDownload(url, tmpFile, file, contentLength)
            }
        }

    private fun downloadSingleThread(url: String, file: File, contentLength: Long) {
        val tmpFile = File(file.absolutePath + ".part")
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
            val body = resp.body ?: throw IOException("Empty body")

            tmpFile.outputStream().use { output ->
                body.byteStream().copyTo(output)
            }
        }
        if (contentLength > 0 && tmpFile.length() != contentLength) {
            throw IOException("File incomplete")
        }
        tmpFile.renameTo(file)
    }

    private suspend fun performMultiThreadDownload(url: String, tmpFile: File, file: File, contentLength: Long) = coroutineScope {
        // 简化逻辑：对于 MP4 等资源，避免过度复杂的断点续传导致的 0 字节文件问题
        // 只有当 tmpFile 不存在时才创建
        if (!tmpFile.exists()) {
            RandomAccessFile(tmpFile, "rw").use { it.setLength(contentLength) }
        }

        val threadCount = if (contentLength < 5 * 1024 * 1024) 1 else 3
        val blockSize = contentLength / threadCount

        (0 until threadCount).map { i ->
            async {
                val start = i * blockSize
                val end = if (i == threadCount - 1) contentLength - 1 else (start + blockSize - 1)

                val req = Request.Builder()
                    .url(url)
                    .addHeader("Range", "bytes=$start-$end")
                    .build()

                client.newCall(req).execute().use { resp ->
                    val body = resp.body ?: throw IOException("Empty body")
                    RandomAccessFile(tmpFile, "rw").use { raf ->
                        raf.seek(start)
                        val source = body.source()
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (source.read(buffer).also { read = it } != -1) {
                            if (pausedSet.contains(url)) throw CancellationException()
                            raf.write(buffer, 0, read)
                        }
                    }
                }
            }
        }.awaitAll()

        if (tmpFile.length() == contentLength) {
            tmpFile.renameTo(file)
        } else {
            throw IOException("Multi-thread download incomplete")
        }
    }

    private fun notifyAllComplete(url: String, file: File) {
        val list = callbackMap.remove(url)
        mainHandler.post {
            list?.forEach { it.onComplete(file) }
        }
    }

    private fun notifyAllFailed(url: String, e: Throwable) {
        val list = callbackMap.remove(url)
        mainHandler.post {
            list?.forEach { it.onFailed(e) }
        }
    }

    private fun addDownloadCallback(url: String, listener: DownloadListener?) {
        if (listener == null) return
        val list = callbackMap.getOrPut(url) { CopyOnWriteArrayList() }
        if (!list.contains(listener)) list.add(listener)
    }

    private fun getPriority(url: String): Int = when {
        url.endsWith(".svga") -> 10
        url.endsWith(".mp4") -> 1
        else -> 3
    }

    private fun getTargetFile(url: String, targetDir: File): File = when {
        url.endsWith(".svga") -> MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
        url.endsWith(".mp4") -> MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, url)
        else -> File(targetDir, url.substringAfterLast("/"))
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
        } catch (e: Exception) { null }
    }

    private fun hasEnoughSpace(directory: File, requiredSize: Long): Boolean {
        val stat = StatFs(directory.path)
        return stat.availableBlocksLong * stat.blockSizeLong >= requiredSize
    }
}