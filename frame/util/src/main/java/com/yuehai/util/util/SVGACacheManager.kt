package com.yuehai.util.util

import com.yuehai.util.AppUtil
import java.io.File

object SVGACacheManager {

    val defaultDir: File
        get() = File(AppUtil.appContext.cacheDir, "svga").apply { if (!exists()) mkdirs() }

    fun getSVGACacheFile(context: android.content.Context, url: String): File {
        val fileName = url.substringAfterLast("/")
        return File(defaultDir, fileName)
    }

    fun saveSVGAFile(file: File, data: ByteArray) {
        try {
            file.outputStream().use { it.write(data) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
