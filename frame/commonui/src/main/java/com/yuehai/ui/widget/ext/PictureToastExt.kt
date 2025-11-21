package com.yuehai.ui.widget.ext

import android.os.FileUtils
import android.util.Log
import com.yuehai.data.collection.path.Constants
import com.yuehai.util.util.WebPUtils
import com.yuehai.util.util.getCompatString
import com.yuehai.util.util.toast.showToast
import java.io.File
import com.yuehai.ui.R

fun File.PicturDynamicToastExt(isEditInfo: Boolean = false): Boolean {
    if ((WebPUtils.isAnimatedWebP(this) || WebPUtils.isAnimatedGif(this.absolutePath)) && (Constants.getLoginUserInfo()?.vipLevel
            ?: 0) < 5
    ) {
        if (!isEditInfo) {
            showToast(getCompatString(R.string.common_dynamic_picture_tips2))
            return true
        }
        showToast(getCompatString(R.string.common_dynamic_picture_tips))
        return true
    }
    if (!isEditInfo && ((WebPUtils.isAnimatedWebP(this) || WebPUtils.isAnimatedGif(this.absolutePath)))) {
        showToast(getCompatString(R.string.common_dynamic_picture_tips2))
        return true
    }

    if (isFileLargerThan4M(path) && ((WebPUtils.isAnimatedWebP(this) || WebPUtils.isAnimatedGif(
            this.absolutePath
        )))
    ) {
        showToast(getCompatString(R.string.common_gif_tips))
        return true
    }
    return false
}

fun File.isDynamicPictureExt(): Boolean {
    return WebPUtils.isAnimatedWebP(this) || WebPUtils.isAnimatedGif(this.absolutePath)
}


fun isFileLargerThan4M(filePath: String): Boolean {
    val file = File(filePath)
    if (!file.exists()) return false
    val fileSize = file.length() // 返回字节数
    return fileSize > 4 * 1024 * 1024
}
