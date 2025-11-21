package com.yuehai.util.util.toast

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.yuehai.util.AppUtil
import com.yuehai.util.R
import com.yuehai.util.util.runOnUiThread

private var lastToastTime = 0L
private var lastToastMessage: CharSequence? = null
private const val TOAST_INTERVAL = 2000L // 2秒内不重复显示同样的Toast

fun showToast(text: CharSequence?) {
    if (text.isNullOrEmpty()) {
        return
    }
    showToast(text, 0)
}

fun showToast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    if (message.isBlank()) return

    val currentTime = System.currentTimeMillis()
    if (message == lastToastMessage && (currentTime - lastToastTime) < TOAST_INTERVAL) {
        // 短时间内重复相同内容，不显示
        return
    }

    lastToastMessage = message
    lastToastTime = currentTime

    runOnUiThread {
        try {
            val context = AppUtil.currentActivity ?: AppUtil.appContext
            val toast = Toast(context)
            val view = LayoutInflater.from(context).inflate(R.layout.layout_toast_center_infalter, null)
            view.findViewById<TextView>(R.id.toast_text).text = message

            toast.view = view
            toast.duration = duration
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        } catch (e: Exception) {
            e.printStackTrace() // 可替换为日志上报
        }
    }
}

fun showCenterToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    if (text.isBlank()) return

    val currentTime = System.currentTimeMillis()
    if (text == lastToastMessage && (currentTime - lastToastTime) < TOAST_INTERVAL) {
        // 短时间内重复相同内容，不显示
        return
    }

    lastToastMessage = text
    lastToastTime = currentTime

    runOnUiThread {
        try {
            val context = AppUtil.currentActivity ?: AppUtil.appContext
            val toast = Toast(context)
            val view = LayoutInflater.from(context).inflate(R.layout.layout_toast_center_infalter, null)
            view.findViewById<TextView>(R.id.toast_text).text = text

            toast.view = view
            toast.duration = duration
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        } catch (e: Exception) {
            e.printStackTrace() // 可替换为日志上报
        }
    }
}
