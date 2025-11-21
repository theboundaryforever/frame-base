package com.yuehai.ui.widget.ext


import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned

fun SpannableStringBuilder.safeSetSpan(what: Any, start: Int, end: Int, flags: Int) {
    if (end < start) {
        return
    }

    val len: Int = length
    if (start > len || end > len) {
        return
    }

    if (start < 0 || end < 0) {
        return
    }

    this.setSpan(what, start, end, flags)
}

fun Spannable.safeSetSpan(what: Any, start: Int, end: Int, flags: Int) {
    if (end < start) {
        return
    }

    val len: Int = length
    if (start > len || end > len) {
        return
    }

    if (start < 0 || end < 0) {
        return
    }

    this.setSpan(what, start, end, flags)
}

fun SpannableStringBuilder.findAndSetSpan(what: Any, targetStr: String) {
    val startIndex = indexOf(targetStr)
    if (startIndex != -1) {
        safeSetSpan(
            what,
            startIndex,
            startIndex + targetStr.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }
}
