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

    private const val MAX_PARALLEL_DOWNLOAD = 10
    private val currentParallel = AtomicInteger(0)
    private const val MAX_RETRY = 3
    private const val DEFAULT_BLOCK_SIZE = 1024L * 1024L // 1MB

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

    fun downloadSingle(
        url: String,
        targetDir: File = MediaCacheManager.defaultDir,
        listener: DownloadListener? = null,
        md5: String? = null
    ) {
        if (url.isEmpty()) return

        addDownloadCallback(url, listener)

        if (downloadQueue.any { it.url == url } || downloadingTasks.containsKey(url)) {
            Log.d(TAG, "[downloadSingle] Already in queue or downloading: $url")
            startQueueIfNeeded()
            return
        }

        val item = DownloadItem(url, targetDir, listener, getPriority(url), md5)
        downloadQueue.offer(item)
        Log.d(TAG, "[downloadSingle] Added to queue: $url")
        startQueueIfNeeded()
    }


    fun downloadMultiple(urls: List<String>, targetDir: File = MediaCacheManager.defaultDir) {
        urls.forEach { downloadSingle(it, targetDir) }
    }

    fun pause(url: String) {
        pausedSet.add(url)
        downloadingTasks[url]?.cancel()
        Log.d(TAG, "[pause] Paused: $url")
    }

    fun resume(url: String) {
        if (pausedSet.remove(url)) {
            val file = getTargetFile(url, MediaCacheManager.defaultDir)
            val tmpFile = File(file.absolutePath + ".part")
            if (tmpFile.exists()) {
                downloadQueue.offer(
                    DownloadItem(
                        url,
                        MediaCacheManager.defaultDir,
                        null,
                        getPriority(url)
                    )
                )
                Log.d(TAG, "[resume] Resumed: $url")
                startQueueIfNeeded()
            } else {
                downloadSingle(url, MediaCacheManager.defaultDir, null)
            }
        }
    }


    fun cancel(url: String) {
        pausedSet.remove(url)
        downloadingTasks[url]?.cancel()
        downloadingTasks.remove(url)
        callbackMap.remove(url)
        retryCountMap.remove(url)

        val iter = downloadQueue.iterator()
        while (iter.hasNext()) {
            if (iter.next().url == url) iter.remove()
        }

        val file = getTargetFile(url, MediaCacheManager.defaultDir)
        val tmpFile = File(file.absolutePath + ".part")
        tmpFile.delete()
        file.delete()
        Log.d(TAG, "[cancel] Cancelled and deleted: $url")
    }


    private fun addDownloadCallback(url: String, listener: DownloadListener?) {
        if (listener == null) return
        val list = callbackMap.getOrPut(url) { CopyOnWriteArrayList() }
        if (!list.contains(listener)) list.add(listener)
        Log.d(TAG, "[addDownloadCallback] $url callbacks=${list.size}")
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
                        downloadQueue.offer(nextItem)
                        startQueueIfNeeded()
                        return@withLock
                    }
                    Log.d(
                        TAG,
                        "[startQueueIfNeeded] Starting: ${nextItem.url}, Queue size=${downloadQueue.size}"
                    )
                    startDownload(nextItem)
                }
            }
        }
    }

    private fun startDownload(item: DownloadItem) {
        val file = getTargetFile(item.url, item.targetDir)

        if (file.exists() && file.length() > 0) {
            if (item.md5 != null) {
                scope.launch(Dispatchers.Default) {
                    val fileMD5 = calculateMD5(file)
                    if (fileMD5 != null && fileMD5.equals(item.md5, true)) {
                        mainHandler.post { item.listener?.onComplete(file) }
                        startQueueIfNeeded()
                        return@launch
                    } else {
                        Log.e(TAG, "[startDownload] MD5 mismatch, redownloading: ${item.url}")
                        file.delete()
                        launchDownload(item, file)
                    }
                }
            } else {
                mainHandler.post { item.listener?.onComplete(file) }
                startQueueIfNeeded()
            }
            return
        }
        launchDownload(item, file)
    }

    private fun launchDownload(item: DownloadItem, file: File) {
        currentParallel.incrementAndGet()
        Log.d(
            TAG,
            "[startDownload] Downloading: ${item.url}, currentParallel=${currentParallel.get()}"
        )

        val job = scope.launch {
            try {
                val headResp = client.newCall(Request.Builder().url(item.url).head().build()).execute()
                val contentLength = headResp.header("Content-Length")?.toLongOrNull() ?: -1
                val acceptRanges = headResp.header("Accept-Ranges")?.contains("bytes") == true
                headResp.close()

                if (contentLength > 0 && !hasEnoughSpace(item.targetDir, contentLength)) {
                    Log.e(TAG, "[launchDownload] Not enough disk space for ${item.url}")
                    throw IOException("Not enough disk space")
                }

                val success = downloadWithRetry(item, file, contentLength, acceptRanges)
                val callbacks = callbackMap[item.url]
                mainHandler.post {
                    if (success) callbacks?.forEach { it.onComplete(file) }
                    else callbacks?.forEach { it.onFailed(IOException("Download failed after retries")) }
                }
            } catch (e: Exception) {
                val callbacks = callbackMap[item.url]
                mainHandler.post { callbacks?.forEach { it.onFailed(e) } }
            } finally {
                downloadingTasks.remove(item.url)
                currentParallel.decrementAndGet()
                startQueueIfNeeded()
            }
        }

        downloadingTasks[item.url] = job
    }

    private suspend fun downloadWithRetry(item: DownloadItem, file: File, contentLength: Long, acceptRanges: Boolean): Boolean {
        var count = retryCountMap[item.url] ?: 0
        while (count < MAX_RETRY) {
            try {
                downloadFile(item.url, file, item.listener, contentLength, acceptRanges)
                if (item.md5 != null) {
                    val fileMD5 = withContext(Dispatchers.Default) { calculateMD5(file) }
                    if (fileMD5 == null || !fileMD5.equals(item.md5, true)) {
                        Log.e(TAG, "[downloadWithRetry] MD5 mismatch: ${item.url}")
                        file.delete()
                        throw IOException("MD5 mismatch")
                    }
                }
                retryCountMap.remove(item.url)
                return true
            } catch (e: Exception) {
                Log.e(TAG, "[downloadWithRetry] failed url=${item.url}, count=$count", e)
                count++
                retryCountMap[item.url] = count
                if (pausedSet.contains(item.url)) {
                    Log.d(TAG, "[downloadWithRetry] Task paused: ${item.url}")
                    return false
                }
            }
        }
        retryCountMap.remove(item.url)
        file.delete()
        return false
    }

    private suspend fun downloadFile(url: String, file: File, listener: DownloadListener?, contentLength: Long, acceptRanges: Boolean) =
        withContext(Dispatchers.IO) {
            if (contentLength <= 0 || !acceptRanges) {
                downloadSingleThread(url, file, listener, contentLength)
                return@withContext
            }

            val tmpFile = File(file.absolutePath + ".part")
            val currentFileLength = if (tmpFile.exists()) tmpFile.length() else 0L

            if (currentFileLength >= contentLength) {
                Log.d(TAG, "[downloadFile] File already complete: $url")
                tmpFile.renameTo(file)
                mainHandler.post { listener?.onProgress(200) }
                return@withContext
            }

            val threadCount = if (contentLength < 10L * 1024L * 1024L) 1 else 6
            val blockSize = contentLength / threadCount
            Log.d(
                TAG,
                "[downloadFile] URL=$url, blockSize=$blockSize, currentLength=$currentFileLength, threadCount=$threadCount"
            )

            if (currentFileLength == 0L) {
                RandomAccessFile(tmpFile, "rw").use { it.setLength(contentLength) }
            }

            val progressMap = LongArray(threadCount) { 0L }

            coroutineScope {
                (0 until threadCount).map { i ->
                    async {
                        val start = i * blockSize
                        val end =
                            if (i == threadCount - 1) contentLength - 1 else (start + blockSize - 1)

                        val blockDownloaded = if (currentFileLength > 0) {
                            min(end + 1, currentFileLength) - start
                        } else 0L

                        if (blockDownloaded >= (end - start + 1)) {
                            progressMap[i] = blockDownloaded
                            Log.d(TAG, "[downloadFile] Block $i already finished, skip.")
                            return@async
                        }

                        val rangeStart = start + blockDownloaded
                        Log.d(TAG, "[downloadFile] Block $i, start=$rangeStart, end=$end")
                        val req =
                            Request.Builder().url(url).addHeader("Range", "bytes=$rangeStart-$end")
                                .build()
                        client.newCall(req).execute().use { resp ->
                            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
                            val body = resp.body ?: throw IOException("Empty body")
                            body.byteStream().use { input ->
                                RandomAccessFile(tmpFile, "rw").use { accessFile ->
                                    accessFile.seek(rangeStart)
                                    val buf = ByteArray(16384)
                                    var len: Int
                                    var totalRead = blockDownloaded
                                    try {
                                        while (input.read(buf).also { len = it } != -1) {
                                            if (pausedSet.contains(url)) throw CancellationException()
                                            accessFile.write(buf, 0, len)
                                            totalRead += len
                                            progressMap[i] = totalRead - start
                                            val total = progressMap.sum()
                                            val progress =
                                                min(100, ((total * 100) / contentLength).toInt())
                                            mainHandler.post { listener?.onProgress(progress) }
                                        }
                                    } catch (e: IOException) {
                                        throw e
                                    }
                                    Log.d(
                                        TAG,
                                        "[downloadFile] Block $i finished, totalRead=$totalRead"
                                    )
                                }
                            }
                        }
                    }
                }.awaitAll()
            }

            if (tmpFile.length() != contentLength) {
                throw IOException("File incomplete")
            }

            tmpFile.renameTo(file)
            mainHandler.post { listener?.onProgress(100) }
        }


    private fun downloadSingleThread(url: String, file: File, listener: DownloadListener?, contentLength: Long) {
        val tmpFile = File(file.absolutePath + ".part")
        val currentFileLength = if (tmpFile.exists()) tmpFile.length() else 0L
        val reqBuilder = Request.Builder().url(url)
        if (currentFileLength > 0) {
            reqBuilder.addHeader("Range", "bytes=$currentFileLength-")
        }

        client.newCall(reqBuilder.build()).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
            val body = resp.body ?: throw IOException("Empty body")
            val totalLength = if (contentLength > 0) {
                contentLength
            } else if (resp.header("Content-Length") != null) {
                currentFileLength + (resp.header("Content-Length")?.toLongOrNull() ?: 0L)
            } else -1L

            var downloaded = currentFileLength
            body.byteStream().use { input ->
                RandomAccessFile(tmpFile, "rw").use { accessFile ->
                    accessFile.seek(currentFileLength)
                    val buf = ByteArray(8192)
                    var len: Int
                    try {
                        while (input.read(buf).also { len = it } != -1) {
                            if (pausedSet.contains(url)) throw CancellationException()
                            accessFile.write(buf, 0, len)
                            downloaded += len
                            val progress = if (totalLength > 0) min(
                                100,
                                ((downloaded * 100) / totalLength).toInt()
                            ) else 0
                            mainHandler.post { listener?.onProgress(progress) }
                        }
                    } catch (e: IOException) {
                        throw e
                    }
                }
            }
            if (totalLength != -1L && tmpFile.length() != totalLength) {
                throw IOException("File incomplete after download")
            }
        }
        tmpFile.renameTo(file)
        mainHandler.post { listener?.onProgress(100) }
    }

    private fun calculateMD5(file: File): String? {
        return try {
            val buffer = ByteArray(8192)
            val digest = MessageDigest.getInstance("MD5")
            file.inputStream().use { fis ->
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "[calculateMD5] Failed for ${file.name}", e)
            null
        }
    }

    private fun hasEnoughSpace(directory: File, requiredSize: Long): Boolean {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val stat = StatFs(directory.path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        return availableBytes >= requiredSize
    }
}