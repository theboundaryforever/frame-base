package com.yuehai.util.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Pair


import java.io.*
import java.io.IOException
import java.net.URLDecoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

fun isExternalStorageAvailable(): Boolean {
    return isExternalStorageReadable() && isExternalStorageWritable()
}

/* Checks if external storage is available for read and write */
fun isExternalStorageWritable(): Boolean {
    val state = Environment.getExternalStorageState()
    return Environment.MEDIA_MOUNTED == state
}

/* Checks if external storage is available to at least read */
fun isExternalStorageReadable(): Boolean {
    val state = Environment.getExternalStorageState()
    return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
}


/**
 * Return the file by path. If path is null or is blank, return null
 *
 * @param filePath The path of file.
 * @return the file
 */
fun getFileByPath(filePath: String?): File? {
    return if (filePath.isNullOrEmpty()) null else File(filePath)
}

/**
 * Return whether the file exists.
 *
 * @param filePath The path of file.
 * @return `true`: yes<br></br>`false`: no
 */
fun isFileExists(filePath: String?): Boolean {
    return isFileExists(getFileByPath(filePath))
}

/**
 * Return whether the file exists.
 *
 * @param file The file.
 * @return `true`: yes<br></br>`false`: no
 */
fun isFileExists(file: File?): Boolean {
    return file != null && file.exists()
}

/**
 * Create a directory if it doesn't exist, otherwise do nothing.
 *
 * @param dirPath The path of directory.
 * @return `true`: exists or creates successfully<br></br>`false`: otherwise
 */
fun createOrExistsDir(dirPath: String?): Boolean {
    return createOrExistsDir(getFileByPath(dirPath))
}

/**
 * Create a directory if it doesn't exist, otherwise do nothing.
 *
 * @param file The file.
 * @return `true`: exists or creates successfully<br></br>`false`: otherwise
 */
fun createOrExistsDir(file: File?): Boolean {
    return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
}

/**
 * Create a file if it doesn't exist, otherwise do nothing.
 *
 * @param filePath The path of file.
 * @return `true`: exists or creates successfully<br></br>`false`: otherwise
 */
fun createOrExistsFile(filePath: String?): Boolean {
    return createOrExistsFile(getFileByPath(filePath))
}

/**
 * Create a file if it doesn't exist, otherwise do nothing.
 *
 * @param file The file.
 * @return `true`: exists or creates successfully<br></br>`false`: otherwise
 */
fun createOrExistsFile(file: File?): Boolean {
    if (file == null) return false
    if (file.exists()) return file.isFile
    return if (!createOrExistsDir(file.parentFile)) false else try {
        file.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}


/**
 * Create a file if it doesn't exist, otherwise delete old file before creating.
 *
 * @param filePath The path of file.
 * @return `true`: success<br></br>`false`: fail
 */
fun createFileByDeleteOldFile(filePath: String?): Boolean {
    return createFileByDeleteOldFile(getFileByPath(filePath))
}

/**
 * Create a file if it doesn't exist, otherwise delete old file before creating.
 * return false if there is a same name dir exist.
 *
 * @param file The file.
 * @return `true`: success<br></br>`false`: fail
 */
fun createFileByDeleteOldFile(file: File?): Boolean {
    if (file == null) return false
    if (file.exists()) {
        if (!file.isFile) return false else if (!file.delete()) return false
    }
    // file exists and unsuccessfully delete then return false
    return if (!createOrExistsDir(file.parentFile)) false else try {
        file.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

/**
 * Return the file's path of directory.
 *
 * @param file The file.
 * @return the file's path of directory
 */
fun getDirName(file: File?): String? {
    return if (file == null) "" else getDirName(file.absolutePath)
}

/**
 * Return the file's path of directory.
 *
 * @param filePath The path of file.
 * @return the file's path of directory
 */
fun getDirName(filePath: String): String? {
    if (TextUtils.isEmpty(filePath)) return ""
    val lastSep = filePath.lastIndexOf(File.separator)
    return if (lastSep == -1) "" else filePath.substring(0, lastSep + 1)
}

/**
 * Return the name of file.
 *
 * @param file The file.
 * @return the name of file
 */
fun getFileName(file: File?): String? {
    return if (file == null) "" else getFileName(file.absolutePath)
}

/**
 * Return the name of file.
 *
 * @param filePath The path of file.
 * @return the name of file
 */
fun getFileName(filePath: String): String? {
    if (TextUtils.isEmpty(filePath)) return ""
    val lastSep = filePath.lastIndexOf(File.separator)
    return if (lastSep == -1) filePath else filePath.substring(lastSep + 1)
}

/**
 * Return the name of file without extension.
 *
 * @param file The file.
 * @return the name of file without extension
 */
fun getFileNameNoSuffix(file: File?): String? {
    return if (file == null) "" else getFileNameNoSuffix(file.path)
}

/**
 * Return the name of file without extension.
 *
 * @param filePath The path of file.
 * @return the name of file without extension
 */
fun getFileNameNoSuffix(filePath: String): String? {
    if (TextUtils.isEmpty(filePath)) return ""
    val lastPoi = filePath.lastIndexOf('.')
    val lastSep = filePath.lastIndexOf(File.separator)
    if (lastSep == -1) {
        return if (lastPoi == -1) filePath else filePath.substring(0, lastPoi)
    }
    return if (lastPoi == -1 || lastSep > lastPoi) {
        filePath.substring(lastSep + 1)
    } else filePath.substring(lastSep + 1, lastPoi)
}

/**
 * Return the extension of file.
 *
 * @param file The file.
 * @return the extension of file
 */
fun getFileSuffix(file: File?): String? {
    return if (file == null) "" else getFileSuffix(file.path)
}

/**
 * Return the extension of file.
 *
 * @param filePath The path of file.
 * @return the extension of file
 */
fun getFileSuffix(filePath: String): String? {
    if (TextUtils.isEmpty(filePath)) return ""
    val lastPoi = filePath.lastIndexOf('.')
    val lastSep = filePath.lastIndexOf(File.separator)
    return if (lastPoi == -1 || lastSep >= lastPoi) "" else filePath.substring(lastPoi + 1)
}

/**
 * 将当前日期时间以特定格式生成文件名
 *
 * @param sdfFormat 格式String
 * @return 文件名
 */
fun generateFileNameByCurrentTime(sdfFormat: String): String {
    return generateFileNameByCurrentTime("", sdfFormat, "")
}

/**
 * 将当前日期时间以特定格式生成文件名
 *
 * @param sdf 时间格式
 * @return 文件名
 */
fun generateFileNameByCurrentTime(sdf: SimpleDateFormat): String {
    return generateFileNameByCurrentTime("", sdf, "")
}

/**
 * 将当前日期时间以特定格式生成文件名，并加上特定的前缀和后缀
 *
 * @param prefix    前缀
 * @param sdfFormat 格式String
 * @param suffix    后缀
 * @return 文件名
 */
fun generateFileNameByCurrentTime(
    prefix: String,
    sdfFormat: String,
    suffix: String,
): String {
    return generateFileNameByCurrentTime(prefix, SimpleDateFormat(sdfFormat, Locale.US), suffix)
}

/**
 * 将当前日期时间以特定格式生成文件名，并加上特定的前缀和后缀
 *
 * @param prefix 前缀
 * @param sdf    时间格式
 * @param suffix 后缀
 * @return 文件名
 */
fun generateFileNameByCurrentTime(
    prefix: String,
    sdf: SimpleDateFormat,
    suffix: String,
): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append(prefix)
        .append(sdf.format(Date()))
        .append(suffix)
    return stringBuilder.toString()
}

fun getDirSizeBytes(file: File): Long {
    return file.listFiles()
        ?.map { if (it.isDirectory) getDirSizeBytes(it) else getFileSizeBytes(it) }
        ?.fold(0, { acc: Long, e: Long -> acc.plus(e) }) ?: 0
}

fun getFileSizeBytes(file: File): Long {
    return if (!file.exists()) 0 else file.length()
}

fun getFileSizeKB(file: File): Long {
    return getFileSizeBytes(file) / 1024
}

fun replaceFileExtension(path: String, newExtension: String): String {
    val i: Int = path.lastIndexOf('.')
    if (i > 0 && i < path.length) {
        return path.substring(0, i + 1) + newExtension
    }
    return path
}

fun bytesToHexString(bytes: ByteArray): String {
    val sb = java.lang.StringBuilder()
    for (b in bytes) {
        val `val` = b.toInt() and 0xff
        if (`val` < 0x10) {
            sb.append("0")
        }
        sb.append(Integer.toHexString(`val`))
    }
    return sb.toString()
}

@SuppressLint("NewApi")
fun getPathFromUri(context: Context, uri: Uri): String? {
    // DocumentProvider
    if (DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = split[0]

            // This is for checking Main Memory
            return if ("primary".equals(type, ignoreCase = true)) {
                if (split.size > 1) {
                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    Environment.getExternalStorageDirectory().toString() + "/"
                }
                // This is for checking SD Card
            } else {
                "storage" + "/" + docId.replace(":", "/")
            }
        } else if (isDownloadsDocument(uri)) {
            val fileName = getFilePath(context, uri)
            if (fileName != null) {
                return Environment.getExternalStorageDirectory()
                    .toString() + "/Download/" + fileName
            }

            var id = DocumentsContract.getDocumentId(uri)
            if (id.startsWith("raw:")) {
                id = id.replaceFirst("raw:".toRegex(), "")
                val file = File(id)
                if (file.exists()) return id
            }

            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                id.toLong()
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = split[0]

            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }

                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(
                split[1]
            )

            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) {
        // Return the remote address

        if (isGooglePhotosUri(uri)) return uri.lastPathSegment

        return getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }

    return null
}

fun getDataColumn(
    context: Context, uri: Uri?, selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )

    try {
        cursor = context.contentResolver.query(
            uri!!, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

fun getFilePath(context: Context, uri: Uri?): String? {
    var cursor: Cursor? = null
    val projection = arrayOf(
        MediaStore.MediaColumns.DISPLAY_NAME
    )

    try {
        cursor = context.contentResolver.query(
            uri!!, projection, null, null,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}

@SuppressLint("DefaultLocale")
fun formatFileSize(path: String): String {
    val kb = 1024L
    val mb = kb * 1024
    val gb = mb * 1024
    val length = getFileSizeBytes(File(path))

    return when {
        length >= gb -> String.format("%.2f GB", length.toFloat() / gb)
        length >= mb -> String.format("%.2f MB", length.toFloat() / mb)
        length >= kb -> String.format("%.2f KB", length.toFloat() / kb)
        else -> "$length B"
    }
}

fun isSameFilePath(path1: String, path2: String): Boolean {
    val file1 = File(decodeFilePath(path1)).absolutePath
    val file2 = File(decodeFilePath(path2)).absolutePath
    return file1 == file2
}

fun decodeFilePath(rawPath: String): String {
    return if (rawPath.startsWith("file://")) {
        URLDecoder.decode(rawPath.removePrefix("file://"), "UTF-8")
    } else {
        rawPath
    }
}


fun md5UrlToName(input: String): String {
    val digest = MessageDigest.getInstance("MD5")
    val result = digest.digest(input.toByteArray())
    return result.joinToString("") { "%02x".format(it) }
}


const val MPR_ZIP_DIR = "admin/"

const val SVGA_DIR = "svga/"
const val Mp4_DIR = "mp4/"
const val EFFECT_DIR = "effect/"
