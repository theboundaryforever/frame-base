package com.yuehai.util.util

import android.os.Handler
import android.os.Looper

fun isMainThread(): Boolean {
    return Looper.getMainLooper() == Looper.myLooper()
}

val uiThreadHandler by lazy { Handler(Looper.getMainLooper()) }

fun runOnUiThread(r: Runnable) {
    if (isMainThread()) {
        r.run()
        return
    }

    uiThreadHandler.post(r)
}

fun runOnUiThread(r: Runnable, delay: Long) {
    uiThreadHandler.postDelayed(r, delay)
}

fun removeUiCallbacks(r: Runnable) {
    uiThreadHandler.removeCallbacks(r)
}