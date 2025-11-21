package com.yuehai.media.file

import com.yuehai.util.AppUtil
import com.yuehai.util.util.createOrExistsDir
import com.yuehai.util.util.isExternalStorageAvailable
import java.io.File

object FilePath {

    class Path(val path: String, private val parentPath: String? = null) {
        val absolutePath: String? = null
            get() {
                if (field.isNullOrEmpty().not()) {
                    return field
                }

                val absPath: StringBuilder = StringBuilder()
                if (parentPath.isNullOrEmpty().not()) {
                    absPath.append(parentPath).append(File.separator)
                }
                absPath.append(path).append(File.separator)
                val fullPath = absPath.toString()
                return if (createOrExistsDir(fullPath)) {
                    fullPath
                } else {
                    null
                }
            }
    }

    /**
     * 在一个应用私有的目录下生成一个文件路径，其他应用无法访问到该目录，用于存放对安全性比较敏感的文件
     */
    private val privateFilesPath by lazy { AppUtil.appContext.filesDir.absolutePath }

    /**
     * 在一个应用私有的目录下生成一个缓存路径，其他应用无法访问到该目录。
     * 用于存放对安全性比较敏感的文件，该目录下的文件在系统存储空间不足时，很可能被系统清理掉，所以该目录主要用于存放临时文件。
     */
    private val privateCachePath by lazy { AppUtil.appContext.cacheDir.absolutePath }

    /**
     * 在一个公有的目录下生成一个缓存路径，其他应用都可以访问到该目录。
     * 用于存放对对安全性不敏感的文件，比如图片，视频，音乐等
     */
    private val publicFilesPath by lazy {
        if (isExternalStorageAvailable()) {
            AppUtil.appContext.getExternalFilesDir(null)?.absolutePath ?: privateFilesPath
        } else {
            privateFilesPath
        }
    }

    /**
     * 在一个公有的目录下生成一个缓存路径，其他应用都可以访问到该目录。
     * 用于存放对对安全性不敏感的文件，该目录下的文件在系统存储空间不足时，很可能被系统清理掉，所以该目录主要用于存放临时文件
     */
    private val publicCachePath by lazy {
        if (isExternalStorageAvailable()) {
            AppUtil.appContext.externalCacheDir?.absolutePath ?: privateCachePath
        } else {
            privateCachePath
        }
    }

    val giftPath by lazy { Path("gift", privateFilesPath).absolutePath }

    val imagePath by lazy { Path("image", publicFilesPath).absolutePath }

    val tempImagePath by lazy { Path("temp", privateFilesPath).absolutePath }

    val clipImagePath by lazy { Path("clip_image", publicFilesPath).absolutePath }

    val takePhotoImagePath by lazy { Path("take_photo", publicFilesPath).absolutePath }

    val spPath by lazy { Path("sp", privateFilesPath).absolutePath }

    val releaseXlogPath by lazy { Path("xlog", privateFilesPath).absolutePath }

    val backupXlogPath by lazy { "${AppUtil.appContext.filesDir}${File.separator}xlog" }

    val xlogZipPath by lazy { Path("xlogz", publicFilesPath).absolutePath }

    val emotionPath by lazy { Path("emotion", publicFilesPath).absolutePath }

    val shareImagePath by lazy { Path("share_image", publicFilesPath).absolutePath }

    val musicPath by lazy { Path("music", publicFilesPath).absolutePath }

    val mediaPath by lazy { releaseXlogPath ?: backupXlogPath }

    val imageProgressPath by lazy { Path("image_progress", publicFilesPath).absolutePath }

    val videoProgressPath by lazy { Path("video_progress", publicFilesPath).absolutePath }

    val themePath by lazy { Path("theme", publicFilesPath).absolutePath }

    val filePath by lazy { Path("file", publicFilesPath).absolutePath }

    val skinPath by lazy { Path("skin", publicFilesPath).absolutePath }

    val carPath by lazy { Path("car", publicFilesPath).absolutePath }

    val videoPath by lazy { Path("video", publicFilesPath).absolutePath }

    val webPath by lazy { Path("web", publicFilesPath).absolutePath }

    val activityPath by lazy { Path("activity", publicFilesPath).absolutePath }

    val rocketPath by lazy { Path("rocket", publicFilesPath).absolutePath }

    val levelPath by lazy { Path("level", publicFilesPath).absolutePath }

    val audioPath by lazy { Path("audio", publicFilesPath).absolutePath }

    val recordPath by lazy { Path("record", publicFilesPath).absolutePath }

    val offlineH5 by lazy { Path("offline_h5", publicFilesPath).absolutePath }

    val cpPath by lazy { Path("cp", publicFilesPath).absolutePath }

    val familyPath by lazy { Path("family", publicFilesPath).absolutePath }

    val httpCachePath by lazy { Path("http_cache", publicCachePath).absolutePath }

    val virtualAppConfigPath by lazy { Path("virtualAppConfigPath", privateFilesPath).absolutePath }

    val gameHubPath by lazy { Path("game_hub", publicFilesPath).absolutePath }

    val gameSkinPath by lazy { Path("game_skin", privateFilesPath).absolutePath }

    val defaultGameSkinPath by lazy { Path("default_game_skin", privateFilesPath).absolutePath }

}