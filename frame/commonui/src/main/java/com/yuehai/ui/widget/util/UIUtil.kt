package com.yuehai.ui.widget.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun Context.copyToClipBoard(label: CharSequence?, text: CharSequence?): Boolean {
    try {
        val clipboard =
            this.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.let {
            it.setPrimaryClip(ClipData.newPlainText(label, text))
            return true
        }
    } catch (ignore: Exception) {
    }
    return false
}