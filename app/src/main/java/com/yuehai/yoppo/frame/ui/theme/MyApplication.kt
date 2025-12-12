package com.yuehai.yoppo.frame.ui.theme

import android.app.Application
import com.yuehai.util.AppUtil

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppUtil.init(this,"")
    }
}