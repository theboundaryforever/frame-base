package com.yuehai.ui.widget.ext

import android.app.Activity
import android.os.Build
import android.view.WindowManager

fun setupWindowBlur(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // 1. 设置窗口为半透明
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )

        // 2. 设置背景模糊半径
        activity.window.setBackgroundBlurRadius(80) // 模糊半径 (int)

        // 3. 确保你的 Activity 根布局背景颜色是透明或半透明的，才能看到效果
        // 例如：findViewById<View>(android.R.id.content).setBackgroundColor(0x88FFFFFF.toInt())
    }
}