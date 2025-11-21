package com.yuehai.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import com.yuehai.util.AppUtil.application

object PackageUtil {
    var currentVersionName: String? = null
    var currentVersionCode: Int = 0

    fun getPackageManager(context: Context): PackageManager? {
        return context.packageManager
    }

    fun getPackageName(context: Context): String {
        return context.packageName
    }

    fun getAppName(context: Context): String {
        return context.applicationContext.packageManager
            .getApplicationLabel(application.applicationInfo).toString()
    }

    fun getVersionCode(context: Context): Int {
        if (currentVersionCode == 0) {
            try {
                val pi: PackageInfo? =
                    getPackageManager(context)?.getPackageInfo(getPackageName(context), 0)
                currentVersionCode = pi?.versionCode ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return currentVersionCode
    }

    fun getVersionName(context: Context): String {
        if (TextUtils.isEmpty(currentVersionName)) {
            try {
                val pi: PackageInfo? =
                    getPackageManager(context)?.getPackageInfo(getPackageName(context), 0)
                currentVersionName = pi?.versionName
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return currentVersionName ?: ""
    }
}