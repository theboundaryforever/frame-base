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
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
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
    private const val SMALL_FILE_THRESHOLD = 200 * 1024L
    private const val PROGRESS_SAVE_BYTES = 512 * 1024L
    private const val PROGRESS_SAVE_INTERVAL = 500L

    private val SAFE_EXTENSIONS = setOf("svga", "mp4", "gif", "png", "jpg", "jpeg", "webp")

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val globalMutex = Mutex()

    private val downloadQueue = mutableListOf<DownloadItem>()
    private val waitingSet = mutableSetOf<String>()
    private val downloadingTasks = mutableMapOf<String, Job>()
    private var runningCount = 0

    private val retryCountMap = ConcurrentHashMap<String, Int>()
    private val pausedSet = Collections.newSetFromMap<String>(ConcurrentHashMap())
    private val callbackMap = ConcurrentHashMap<String, MutableList<WeakReference<DownloadListener>>>()

    private val client = OkHttpClient.Builder().retryOnConnectionFailure(true).build()

    data class DownloadItem(
        val url: String,
        val targetDir: File,
        val listener: DownloadListener?,
        val priority: Int = 0,
        val md5: String? = null
    )

    /* ===================== API ===================== */

    fun downloadSingle(url: String, targetDir: File = MediaCacheManager.defaultDir, listener: DownloadListener? = null, md5: String? = null) {
        if (url.isEmpty()) return
        Log.d(TAG, "add task: $url, priority: ${getPriority(url)}")
        addCallback(url, listener)

        scope.launch {
            globalMutex.withLock {
                if (waitingSet.contains(url) || downloadingTasks.containsKey(url)) {
                    Log.i(TAG, "task already exists in queue/running: $url")
                    startNextLocked()
                    return@withLock
                }
                waitingSet.add(url)
                downloadQueue.add(DownloadItem(url, targetDir, listener, getPriority(url), md5))
                downloadQueue.sortByDescending { it.priority }
                startNextLocked()
            }
        }
    }

    fun pause(url: String) {
        Log.i(TAG, "pause task: $url")
        pausedSet.add(url)
        scope.launch {
            globalMutex.withLock {
                if (waitingSet.remove(url)) {
                    downloadQueue.removeAll { it.url == url }
                }
                stopRunningTaskLocked(url)
            }
        }
    }

    fun resume(url: String) {
        Log.i(TAG, "resume task: $url")
        if (pausedSet.remove(url)) downloadSingle(url)
    }

    fun cancel(url: String) {
        Log.w(TAG, "cancel task: $url")
        pausedSet.remove(url)
        retryCountMap.remove(url)
        callbackMap.remove(url)
        scope.launch {
            globalMutex.withLock {
                if (waitingSet.remove(url)) {
                    downloadQueue.removeAll { it.url == url }
                }
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
        Log.v(TAG, "check schedule: running=$runningCount, waiting=${downloadQueue.size}")
        while (runningCount < MAX_PARALLEL_DOWNLOAD && downloadQueue.isNotEmpty()) {
            val item = downloadQueue.removeAt(0)
            waitingSet.remove(item.url)

            if (pausedSet.contains(item.url)) {
                Log.d(TAG, "skip paused task in queue: ${item.url}")
                continue
            }
            launchTaskLocked(item)
        }
    }

    private fun launchTaskLocked(item: DownloadItem) {
        runningCount++
        Log.i(TAG, "🚀 launch task: ${item.url} (runningCount=$runningCount)")
        val job = scope.launch {
            try {
                yield()
                val file = getTargetFile(item.url, item.targetDir)
                if (file.exists() && file.length() > 0) {
                    if (item.md5 == null || calculateMD5(file).equals(item.md5, true)) {
                        Log.d(TAG, "file already exists and valid: ${item.url}")
                        dispatchComplete(item.url, file)
                        return@launch
                    } else {
                        Log.w(TAG, "MD5 mismatch for existing file, re-downloading: ${item.url}")
                        file.delete()
                    }
                }

                if (downloadWithRetry(item, file)) {
                    dispatchComplete(item.url, file)
                } else {
                    Log.e(TAG, "task failed after all retries: ${item.url}")
                    dispatchFail(item.url, IOException("Max retries reached"))
                }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "task coroutine cancelled: ${item.url}")
                } else {
                    Log.e(TAG, "task exception: ${item.url}", e)
                    dispatchFail(item.url, e)
                }
            } finally {
                globalMutex.withLock {
                    if (downloadingTasks.remove(item.url) != null) {
                        runningCount = max(0, runningCount - 1)
                        Log.d(TAG, "task finished and slot released: ${item.url} (runningCount=$runningCount)")
                    }
                    startNextLocked()
                }
            }
        }
        downloadingTasks[item.url] = job
    }

    private fun stopRunningTaskLocked(url: String) {
        val job = downloadingTasks[url]
        if (job != null) {
            job.cancel()
            downloadingTasks.remove(url)
            runningCount = max(0, runningCount - 1)
            Log.d(TAG, "force stopped running task: $url (runningCount=$runningCount)")
            startNextLocked()
        }
    }

    /* ===================== 网络与IO ===================== */

    private suspend fun downloadWithRetry(item: DownloadItem, file: File): Boolean {
        var retry = retryCountMap[item.url] ?: 0
        while (retry < MAX_RETRY) {
            try {
                processDownload(item.url, file)
                if (item.md5 != null && !calculateMD5(file).equals(item.md5, true)) {
                    file.delete()
                    throw IOException("MD5 mismatch after download")
                }
                retryCountMap.remove(item.url)
                return true
            } catch (e: Exception) {
                if (e is CancellationException || pausedSet.contains(item.url)) {
                    Log.d(TAG, "stop retry due to pause/cancel: ${item.url}")
                    return false
                }
                retry++
                retryCountMap[item.url] = retry
                Log.w(TAG, "retry task ($retry/$MAX_RETRY): ${item.url} cause: ${e.message}")
                delay(1000L * retry)
            }
        }
        return false
    }

    private suspend fun processDownload(url: String, file: File) = withContext(Dispatchers.IO) {
        val tmpFile = File(file.absolutePath + ".part")
        val cfgFile = File(file.absolutePath + ".cfg")

        var startBytes = 0L
        if (cfgFile.exists() && tmpFile.exists()) {
            startBytes = cfgFile.readText().toLongOrNull() ?: 0L
            if (startBytes > tmpFile.length()) startBytes = tmpFile.length()
            Log.d(TAG, "resume download from: $startBytes bytes, url: $url")
        } else {
            tmpFile.delete()
            cfgFile.delete()
        }

        val request = Request.Builder().url(url)
            .apply { if (startBytes > 0) addHeader("Range", "bytes=$startBytes-") }.build()

        client.newCall(request).execute().use { resp ->
            val isPartial = resp.code == 206
            if (!resp.isSuccessful && !isPartial) throw IOException("HTTP ${resp.code}")
            val body = resp.body ?: throw IOException("Empty body")
            val contentLen = body.contentLength()
            val totalLength = if (isPartial) startBytes + contentLen else contentLen

            Log.i(TAG, "connection success: $url, totalSize: $totalLength, isPartial: $isPartial")

            if (contentLen > 0 && !ensureSpace(file.parentFile!!, contentLen)) {
                throw IOException("No space left on device after eviction")
            }

            var lastDisplayProgress = if (totalLength > 0) (startBytes * 100 / totalLength).toInt() else 0
            RandomAccessFile(tmpFile, "rw").use { raf ->
                val offset = if (isPartial) startBytes else 0L
                raf.seek(offset)
                val buf = ByteArray(16 * 1024)
                var downloaded = offset
                var lastSaveTime = System.currentTimeMillis()
                var lastSaveBytes = downloaded

                body.byteStream().use { input ->
                    var len: Int
                    while (input.read(buf).also { len = it } != -1) {
                        if (pausedSet.contains(url)) throw CancellationException()
                        raf.write(buf, 0, len)
                        downloaded += len

                        val now = System.currentTimeMillis()
                        if (now - lastSaveTime >= 200L) { // 稍微降低日志频率
                            val cur = if (totalLength > 0) (downloaded * 100 / totalLength).toInt() else 0
                            lastDisplayProgress = max(lastDisplayProgress, cur)
                            dispatchProgress(url, lastDisplayProgress)
                        }
                        if (totalLength > SMALL_FILE_THRESHOLD && (downloaded - lastSaveBytes >= PROGRESS_SAVE_BYTES || now - lastSaveTime >= PROGRESS_SAVE_INTERVAL)) {
                            cfgFile.writeText(downloaded.toString())
                            lastSaveBytes = downloaded
                            lastSaveTime = now
                        }
                    }
                }
            }
            if (!tmpFile.renameTo(file)) throw IOException("Rename error")
            cfgFile.delete()
            Log.i(TAG, "download success: $url -> ${file.absolutePath}")
        }
    }

    /* ===================== 磁盘清理逻辑 ===================== */

    private suspend fun ensureSpace(dir: File, requiredSize: Long): Boolean {
        val available = getAvailableSpace(dir)
        if (available >= requiredSize) return true

        Log.w(TAG, "low space: required $requiredSize, available $available. triggering eviction...")
        evictOldCaches(dir, requiredSize)

        val finalAvailable = getAvailableSpace(dir)
        val success = finalAvailable >= requiredSize
        if (!success) Log.e(TAG, "still no space after eviction! available: $finalAvailable")
        return success
    }

    private suspend fun evictOldCaches(dir: File, neededBytes: Long) = withContext(Dispatchers.IO) {
        try {
            val files = dir.listFiles { f ->
                f.isFile && !f.name.endsWith(".part") && !f.name.endsWith(".cfg") &&
                        SAFE_EXTENSIONS.contains(f.extension.lowercase())
            } ?: return@withContext

            yield()
            val sortedFiles = files.sortedBy { it.lastModified() }
            Log.d(TAG, "found ${sortedFiles.size} candidate files for eviction")

            var freedTotal = 0L
            for (file in sortedFiles) {
                ensureActive()
                val size = file.length()
                if (file.delete()) {
                    freedTotal += size
                    Log.v(TAG, "deleted old cache file: ${file.name}, freed: $size")
                    if (getAvailableSpace(dir) >= neededBytes) break
                }
                yield()
            }
            Log.i(TAG, "eviction finished. total freed: $freedTotal bytes")
        } catch (e: Exception) {
            if (e !is CancellationException) Log.e(TAG, "eviction error", e)
        }
    }

    private fun getAvailableSpace(dir: File): Long {
        return try {
            val stat = StatFs(dir.path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) { 0L }
    }

    /* ===================== 工具方法 ===================== */

    private fun addCallback(url: String, listener: DownloadListener?) {
        if (listener == null) return
        val list = callbackMap.getOrPut(url) { Collections.synchronizedList(mutableListOf()) }
        list.add(WeakReference(listener))
    }

    private fun dispatchProgress(url: String, progress: Int) {
        mainHandler.post {
            callbackMap[url]?.iterator()?.let { iter ->
                while (iter.hasNext()) {
                    val l = iter.next().get()
                    if (l == null) iter.remove() else l.onProgress(progress)
                }
            }
        }
    }

    private fun dispatchComplete(url: String, file: File) {
        mainHandler.post {
            callbackMap[url]?.forEach { it.get()?.onComplete(file) }
            callbackMap.remove(url)
        }
    }

    private fun dispatchFail(url: String, e: Throwable?) {
        mainHandler.post {
            callbackMap[url]?.forEach { it.get()?.onFailed(e) }
            callbackMap.remove(url)
        }
    }

    private fun calculateMD5(file: File): String? = try {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buf = ByteArray(8192)
            var len: Int
            while (input.read(buf).also { len = it } != -1) md.update(buf, 0, len)
        }
        md.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) { null }

    private fun getTargetFile(url: String, targetDir: File): File = when {
        url.endsWith(".svga") -> MediaCacheManager.getSVGACacheFile(AppUtil.appContext, url)
        url.endsWith(".mp4") -> MediaCacheManager.getVapMp4CacheFile(AppUtil.appContext, url)
        else -> File(targetDir, url.substringAfterLast("/"))
    }

    private fun getPriority(url: String): Int = when {
        url.endsWith(".svga") || url.endsWith(".gif") -> 10
        url.endsWith(".png") || url.endsWith(".jpg") -> 5
        url.endsWith(".mp4") -> 1
        else -> 3
    }
}