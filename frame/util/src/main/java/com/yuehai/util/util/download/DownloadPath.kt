package com.yuehai.util.util.download

import android.content.Context
import com.yuehai.util.AppUtil
import com.yuehai.util.util.EFFECT_DIR
import com.yuehai.util.util.ext.extractFileName
import java.io.File


object MediaCacheManager {
    // svga 缓存目录（/cache/svga）
    fun getSVGACacheDir(context: Context): File {
        val dir = File(context.cacheDir, "svga")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // 根据 URL 获取 svga 缓存文件
    fun getSVGACacheFile(context: Context, url: String): File {
        val fileName = extractFileName(url)
        return File(getSVGACacheDir(context), fileName)
    }


    fun getVapMp4CacheDir(context: Context): File {
        val dir = File(context.cacheDir, "vap")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }


    // 根据 URL 获取 vap mp4 缓存文件
    fun getVapMp4CacheFile(context: Context, url: String): File {
        val fileName = extractFileName(url)
        return File(getVapMp4CacheDir(context), fileName)
    }

    fun getGiftCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "gift")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // 根据 URL 获取 gift 缓存文件
    fun getGiftCacheFile(context: Context, url: String): File {
        val fileName = extractFileName(url)
        return File(getGiftCacheDir(context), fileName)
    }

    fun getEmotionCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "emotion")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // 根据 URL 获取 表情包 缓存文件
    fun getEmotionCacheFile(context: Context, url: String): File {
        val fileName = extractFileName(url)
        return File(getGiftCacheDir(context), fileName)
    }

    // 判断文件是否已缓存（支持 svga/mp4）
    fun isCached(file: File): Boolean {
        return file.exists() && file.isFile && file.length() > 0
    }

    // 清理缓存目录（可选）
    fun clearCacheDir(fileDir: File) {
        if (fileDir.exists() && fileDir.isDirectory) {
            fileDir.listFiles()?.forEach { it.delete() }
        }
    }

    val defaultDir by lazy {
        val apply =
            File(AppUtil.appContext.cacheDir.path + File.separator + "other", "download").apply {
                if (!exists()) mkdirs()
            }
        apply
    }

    fun getPagCacheFile(context: Context, url: String): File {
        val fileName = extractFileName(url)
        return File(getPagCacheDir(context), fileName)
    }

    fun getPagCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "pag")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    val giftDir by lazy {
        val apply =
            File(AppUtil.appContext.cacheDir.path + File.separator + "gift", "download").apply {
                if (!exists()) mkdirs()
            }
        apply
    }
    val emotionDir by lazy {
        val apply =
            File(AppUtil.appContext.cacheDir.path + File.separator + "emotion", "download").apply {
                if (!exists()) mkdirs()
            }
        apply
    }
}
