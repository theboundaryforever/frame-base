package com.yuehai.util.util.ext

import android.view.View

private const val CLICK_INTERVAL = 1500L // 毫秒

fun View.setSafeClickListener(interval: Long = CLICK_INTERVAL, onSafeClick: (View) -> Unit) {
    var lastClickTime = 0L

    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= interval) {
            lastClickTime = currentTime
            onSafeClick(view)
        }
    }
}
