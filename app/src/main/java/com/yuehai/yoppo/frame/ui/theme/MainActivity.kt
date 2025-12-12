package com.yuehai.yoppo.frame.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tencent.qgame.animplayer.AnimView
import com.yuehai.util.util.svga.PlayCallback
import com.yuehai.util.util.svga.checkMp4Cache
import com.yuehai.yoopo.frame.R

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mp4Url = "https://cdn.yoppo.net/admin/d3463a31d3533caddce33325efb6e6b1.mp4"
        val animView = findViewById<AnimView>(R.id.anim_mp)
        animView.checkMp4Cache(mp4Url,-1, callback = object:PlayCallback{
            override fun onPlayStart() {
                super.onPlayStart()
                Log.d("-------------onPlayStart:","111")
            }

            override fun onPlayComplete() {
                super.onPlayComplete()
                Log.d("-------------onPlayComplete:","111")
            }

            override fun onPlayFailed(error: String?) {
                super.onPlayFailed(error)
                Log.d("-------------failed:","$error")
            }
        })

    }
}