package com.yuehai.util

import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object GoogleNetworkChecker {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private const val primaryUrl = "https://www.google.com/generate_204"
    private const val backupUrl = "https://www.gstatic.com/generate_204"

    /**
     * 同步检测 Google 服务是否可达（优先主地址，失败尝试备用地址）
     */
    fun isGoogleAccessible(): Boolean {
        return tryCheckUrl(primaryUrl) || tryCheckUrl(backupUrl)
    }

    /**
     * 异步检测 Google 服务是否可达（主线程回调）
     * 失败或超时时，也会返回 true，避免阻塞登录。
     */
    fun checkAsync(callback: (Boolean) -> Unit) {
        executor.execute {
            val result = try {
                // 实际检测
                isGoogleAccessible()
            } catch (e: Exception) {
                false
            }

            // 检测失败也放行
            val finalResult = if (!result) {
                // 放行逻辑
                true
            } else {
                result
            }

            mainHandler.post {
                callback(finalResult)
            }
        }
    }

    /**
     * 尝试访问指定 URL，返回是否成功（HTTP 204）
     */
    private fun tryCheckUrl(urlStr: String): Boolean {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 2000
            conn.readTimeout = 2000
            conn.instanceFollowRedirects = false
            conn.useCaches = false
            conn.connect()
            val success = conn.responseCode == 204
            conn.disconnect()
            success
        } catch (e: Exception) {
            false
        }
    }
}
