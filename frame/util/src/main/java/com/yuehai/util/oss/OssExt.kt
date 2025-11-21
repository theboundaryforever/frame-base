package com.yuehai.util.oss

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alibaba.sdk.android.oss.OSS
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.yuehai.data.collection.path.Constants
import com.yuehai.util.util.SignatureNewUtil
import com.yuehai.util.util.SnowFlake
import java.util.*

/**
 * 单张图片异步上传
 */
fun OSSClient.uploadImageAsync(
    localPath: String,
    ossBucket: String = Constants.ossbucket,
    onSuccess: (ossUrl: String) -> Unit,
    onFinish: () -> Unit,
    onError: (Exception?) -> Unit
) {
    val random = Random().nextInt(1000)
    val snowFlake = SnowFlake(1, 1)
    val picName = "${snowFlake.nextId()}$random"
    val md5PicName = SignatureNewUtil.md5(picName)
    val objectKey = "img/$md5PicName.png"
    val request = PutObjectRequest(ossBucket, objectKey, localPath)

    Log.d("OSS_UPLOAD", "Local path: $localPath")
    Log.d("OSS_UPLOAD", "OSS object key: $objectKey")

    this.asyncPutObject(request, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
        override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
            val url = "https://cdn.yoppo.net/$objectKey"
            Log.d("OSS_UPLOAD", "Success: $url")
            onSuccess(url)
            onFinish()
        }

        override fun onFailure(
            request: PutObjectRequest?,
            clientException: ClientException?,
            serviceException: ServiceException?
        ) {
            Log.e("OSS_UPLOAD", "Client error: ${clientException?.message}")
            Log.e("OSS_UPLOAD", "Service error: ${serviceException?.rawMessage}")
            onError(clientException ?: serviceException)
            onFinish()
        }
    })
}

/**
 * 多张图片有序上传，带重试机制
 */
fun OSSClient.uploadImagesAsyncOrderedWithRetry(
    localPaths: List<String>,
    ossBucket: String = Constants.ossbucket,
    maxRetry: Int = 2,
    onEachSuccess: (ossUrl: String, index: Int) -> Unit = { _, _ -> },
    onAllSuccess: (ossUrls: List<String>) -> Unit = {},
    onAllCompleted: (successUrls: List<String>, failedPaths: List<String>) -> Unit = { _, _ -> },
    onError: (e: Exception?, index: Int, attempt: Int) -> Unit = { _, _, _ -> },
    onFinish: () -> Unit = {}
) {
    if (localPaths.isEmpty()) {
        onAllSuccess(emptyList())
        onAllCompleted(emptyList(), emptyList())
        onFinish()
        return
    }

    val ossUrls = MutableList<String?>(localPaths.size) { null }
    val failedPaths = mutableListOf<String>()
    var completedCount = 0

    fun checkAllCompleted() {
        synchronized(this) {
            if (completedCount == localPaths.size) {
                val successList = ossUrls.filterNotNull()
                onAllSuccess(successList)
                onAllCompleted(successList, failedPaths)
                onFinish()
            }
        }
    }

    localPaths.forEachIndexed { index, path ->

        fun attemptUpload(attempt: Int) {
            Log.d("OSS_UPLOAD", "上传第$attempt 次尝试: $path")
            this.uploadImageAsync(
                localPath = path,
                ossBucket = ossBucket,
                onSuccess = { url ->
                    ossUrls[index] = url
                    onEachSuccess(url, index)

                    synchronized(this) { completedCount++ }
                    checkAllCompleted()
                },
                onError = { e ->
                    if (attempt <= maxRetry) {
                        // 延迟 1 秒后重试
                        Handler(Looper.getMainLooper()).postDelayed({
                            attemptUpload(attempt + 1)
                        }, 1000L)
                    } else {
                        failedPaths.add(path)
                        onError(e, index, attempt)
                        synchronized(this) { completedCount++ }
                        checkAllCompleted()
                    }
                },
                onFinish = {}
            )
        }

        attemptUpload(1) // 第一次尝试
    }
}
