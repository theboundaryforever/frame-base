package com.yuehai.sound.config

import com.yuehai.sound.mediasdk.IMediaSdkSoundEffect


interface ISoundPlayerConfig {

    val mediaSdkSoundEffect: IMediaSdkSoundEffect

    val audioZipFileName: String

    val defaultVolume: Int

    val audioPath: String?

    /**
     * 是否使用媒体sdk播放音效
     */
    fun isUseMediaSdk(): Boolean

    /**
     * 是否能够播放音效
     */
    fun isCanPlaySound(): Boolean

    /**
     * 是否能够播放音乐
     */
    fun isCanPlayMusic(): Boolean

    /**
     * 是否在前台
     */
    fun isInForeGround(): Boolean


}