package com.yuehai.util.compress

import android.content.Context
import android.net.Uri
import com.yuehai.util.util.getFileFromUri
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

fun Context.compressImageFromUri(
    uri: Uri,
    ignoreSizeKb: Int = 250,
    onSuccess: (File) -> Unit,
    onError: ((Throwable?) -> Unit)? = null
) {
    val file = getFileFromUri(this, uri)

    Luban.with(this)
        .load(file)
        .ignoreBy(ignoreSizeKb)
        .setCompressListener(object : OnCompressListener {
            override fun onStart() {
                // 可选：加载动画等
            }

            override fun onSuccess(index: Int, compressFile: File) {
                onSuccess(compressFile)
            }

            override fun onError(index: Int, e: Throwable?) {
                onError?.invoke(e)
            }
        }).launch()
}
