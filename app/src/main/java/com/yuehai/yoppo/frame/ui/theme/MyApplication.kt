package com.yuehai.yoppo.frame.ui.theme

import android.app.Application
import com.yuehai.media.agora.createMediaService
import com.yuehai.util.AppUtil

class MyApplication: Application() {
    val mediaService by lazy { createMediaService(MediaConfig()) }
    override fun onCreate() {
        super.onCreate()
        AppUtil.init(this@MyApplication,mediaService)
    }
}