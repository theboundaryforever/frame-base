package com.yuehai.util.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object NavigationBarUtils {

    /**
     * 推荐优先使用，支持 Android 10+
     */
    fun hasNavigationBar(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = activity.window.decorView.rootWindowInsets
            return windowInsets?.isVisible(WindowInsets.Type.navigationBars()) == true
        } else {
            val rootView = activity.window.decorView
            val insets = ViewCompat.getRootWindowInsets(rootView)
            return insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        }
    }

    /**
     * 备用方法：通过高度差判断（兼容旧设备）
     */
    fun hasNavigationBarByHeight(context: Context): Boolean {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return try {
            val realMetrics = DisplayMetrics()
            val usableMetrics = DisplayMetrics()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.getRealMetrics(realMetrics)
                context.display?.getMetrics(usableMetrics)
            } else {
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getRealMetrics(realMetrics)
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getMetrics(usableMetrics)
            }

            val heightDiff = realMetrics.heightPixels - usableMetrics.heightPixels
            val widthDiff = realMetrics.widthPixels - usableMetrics.widthPixels

            heightDiff > 0 || widthDiff > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 补充方法：是否有物理返回/菜单键（间接判断）
     */
    fun hasNavigationBarByKey(context: Context): Boolean {
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        return !(hasMenuKey || hasBackKey)
    }

    fun hasNavigationBarCompat(activity: Activity): Boolean {
        return NavigationBarUtils.hasNavigationBar(activity)
                || NavigationBarUtils.hasNavigationBarByHeight(activity)
                || NavigationBarUtils.hasNavigationBarByKey(activity)
    }

    /**
     * 获取导航栏高度（自动判断是否存在）
     */
    fun getNavigationBarHeight(context: Context): Int {
        if (!hasNavigationBar(context)) return 0

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val metrics = windowManager?.currentWindowMetrics
            val insets = metrics?.windowInsets
                ?.getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars())
            insets?.bottom ?: 0
        } else {
            val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
        }
    }

    /**
     * 判断当前设备是否显示导航栏
     */
    fun hasNavigationBar(context: Context): Boolean {
        if (context is Activity) {
            val rootView = context.window.decorView
            val insets = ViewCompat.getRootWindowInsets(rootView)
            return insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        }

        // fallback: 旧设备通过按键判断
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        return !(hasMenuKey && hasBackKey)
    }

    /**
     * 实时监听导航栏高度变化
     */
    fun listenNavigationBarHeight(view: View, onHeightChanged: (height: Int) -> Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            onHeightChanged(navBarInsets.bottom)
            insets
        }
    }

}
