package com.yuehai.util.util

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL

object WebPUtils {

    private const val TAG = "WebPUtils"

    /**
     * 判断本地 File 是否为 Animated WebP
     */
    fun isAnimatedWebP(file: File): Boolean {
        if (!file.exists() || !file.name.endsWith(".webp", true)) return false
        return try {
            FileInputStream(file).use {
                isAnimatedWebP(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "File check failed", e)
            false
        }
    }

    /**
     * 判断 Uri 是否为 Animated WebP（Android 9.0+ 优先用 ImageDecoder）
     */
    fun isAnimatedWebP(context: Context, uri: Uri): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val drawable = ImageDecoder.decodeDrawable(source)
                drawable is AnimatedImageDrawable
            } else {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { isAnimatedWebP(it) } ?: false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Uri check failed", e)
            false
        }
    }

    /**
     * 判断网络 URL 是否为 Animated WebP（下载到临时文件 + ImageDecoder）
     */
    fun isAnimatedWebPFromUrl(context: Context, url: String): Boolean {
        return try {
            val connection = URL(url).openConnection()
            connection.connect()
            val input = connection.getInputStream()

            val tempFile = File.createTempFile("temp_webp", ".webp", context.cacheDir)
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
            input.close()

            val uri = Uri.fromFile(tempFile)
            isAnimatedWebP(context, uri)
        } catch (e: Exception) {
            Log.e(TAG, "URL check failed", e)
            false
        }
    }

    /**
     * Fallback：读取 WebP 文件头判断是否包含动画标识（不适用于复杂结构）
     */
    fun isAnimatedWebP(inputStream: InputStream): Boolean {
        return try {
            val header = ByteArray(2048) // 扩大读取范围，提高识别率
            val len = inputStream.read(header, 0, header.size)
            if (len <= 0) return false
            val headerStr = String(header)
            headerStr.contains("ANIM") || headerStr.contains("ANMF")
        } catch (e: Exception) {
            Log.e(TAG, "Header check failed", e)
            false
        }
    }

    /**
     * 判断路径是否是 gif 动图（简单后缀判断）
     */
    fun isAnimatedGif(path: String): Boolean {
        return path.lowercase().endsWith(".gif")
    }

    /**
     * 综合判断网络图片是否为动图（支持 gif / webp）
     */
    fun isAnimatedImageFromUrl(context: Context, url: String): Boolean {
        return isAnimatedGif(url) || isAnimatedWebPFromUrl(context, url)
    }
}
