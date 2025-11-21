package com.yuehai.util.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.UUID
import androidx.core.content.edit

@SuppressLint("HardwareIds")
fun getPersistentDeviceId(context: Context): String {
    val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
    val cacheKey = "device_id"
    var deviceId = prefs.getString(cacheKey, null)

    if (!deviceId.isNullOrEmpty()) return deviceId

    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    deviceId = if (androidId.isNullOrEmpty() || androidId == "9774d56d682e549c") {
        UUID.randomUUID().toString()
    } else {
        androidId
    }

    prefs.edit() { putString(cacheKey, deviceId) }
    return deviceId
}
