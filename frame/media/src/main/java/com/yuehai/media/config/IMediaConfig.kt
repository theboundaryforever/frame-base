package com.yuehai.media.config

import android.content.Context
import com.yuehai.data.collection.path.Rlt
import com.yuehai.media.data.RtcType

interface IMediaConfig {

    val context: Context
    val mediaPath: String
    val agoraAppId: String


    suspend fun getChannelToken(channel: String, rtcType: RtcType): Rlt<String>

}