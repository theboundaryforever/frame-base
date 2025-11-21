package com.yuehai.util.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private const val TAG = "FileUtilFunctions"

/**
 * 将 Uri 指向的内容复制到应用缓存目录，并返回新的 File 对象。
 * 修复了扩展名获取不准确的问题。
 *
 * @param context Context 实例
 * @param uri 待处理的 Uri (通常是 content:// 类型)
 * @return 复制到缓存目录的 File 对象，如果失败则返回 null
 */
fun getFileFromUri(context: Context, uri: Uri): File? {
    // 调用经过增强的函数来获取扩展名
    val extension = getExtensionFromUri(context, uri) ?: "temp" // 默认为 temp
    val fileName = "${System.currentTimeMillis()}.$extension"
    val file = File(context.cacheDir, fileName)

    var inputStream: InputStream? = null
    var outputStream: FileOutputStream? = null

    try {
        inputStream = context.contentResolver.openInputStream(uri)
        outputStream = FileOutputStream(file)

        // 使用 Kotlin 的标准库函数 copyTo 进行高效复制
        inputStream?.copyTo(outputStream)

        return file
    } catch (e: Exception) {
        Log.e(TAG, "Failed to copy file from Uri: $uri", e)
        // 如果复制失败，删除可能残留的不完整文件
        file.delete()
        return null
    } finally {
        // 确保流被关闭
        try { inputStream?.close() } catch (ignored: Exception) {}
        try { outputStream?.close() } catch (ignored: Exception) {}
    }
}

/**
 * 尝试从 Uri 中获取文件扩展名。
 * 采用多重检查机制：1. MIME 类型 -> 2. ContentResolver 查询 (最可靠的 Fallback) -> 3. Uri 路径 (最不可靠)
 *
 * @param context Context 实例
 * @param uri 待处理的 Uri
 * @return 文件的扩展名字符串 (例如 "jpg", "png", "mp4")，如果无法获取则返回 null
 */
fun getExtensionFromUri(context: Context, uri: Uri): String? {
    val contentResolver = context.contentResolver

    // --- 尝试 1: 从 MIME 类型获取 (最高优先级) ---
    val type = contentResolver.getType(uri)
    if (type != null) {
        val extensionFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
        if (extensionFromMime != null) {
            return extensionFromMime.lowercase()
        }
    }

    // --- 尝试 2: 从 ContentResolver 查询显示名称 (最可靠的 Fallback) ---
    try {
        // 查询 DISPLAY_NAME
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    val fileName = cursor.getString(nameIndex)
                    val dotIndex = fileName.lastIndexOf('.')
                    // 确保有小数点且后面有内容
                    if (dotIndex != -1 && dotIndex < fileName.length - 1) {
                        return fileName.substring(dotIndex + 1).lowercase()
                    }
                }
            }
        }
    } catch (e: Exception) {
        Log.w(TAG, "Failed to query display name for Uri: $uri", e)
    }

    // --- 尝试 3: 从 Uri path 获取 (最后的兜底) ---
    val path = uri.path
    if (path != null) {
        val dotIndex = path.lastIndexOf('.')
        if (dotIndex != -1 && dotIndex < path.length - 1) {
            return path.substring(dotIndex + 1).lowercase()
        }
    }

    // 所有方法都失败
    return null
}