package com.yuehai.yoppo.frame.ui.theme

import android.content.Context
import com.yuehai.data.collection.path.Rlt
import com.yuehai.media.config.IMediaConfig
import com.yuehai.media.data.RtcType
import com.yuehai.util.AppUtil
import com.yuehai.media.file.FilePath
import com.yuehai.util.BuildConfig


class MediaConfig : IMediaConfig {

    override val context: Context
        get() = AppUtil.appContext
    override val mediaPath: String
        get() = FilePath.mediaPath
    override val agoraAppId: String
        get() = if (BuildConfig.DEBUG) {
           ""
        } else {
           " BuildConfig.AGORA_APP_ID_RELEASE"
        }


    override suspend fun getChannelToken(channel: String, rtcType: RtcType): Rlt<String> {

        return Rlt.Success("")
    }

}