package com.yuehai.media.listener

import com.yuehai.media.data.AudioRouter
import com.yuehai.media.data.MediaMusicPlayState
import com.yuehai.media.data.MediaMusicReason


interface IMediaMusicListener {

    /**
     * 音乐播放进度变化回调
     */
    fun onMusicPlayProgress(filePath: String, progress: Float) {}

    /**
     * 音乐播放状态变化回调
     */
    fun onMusicPlayStateChanged(
        filePath: String,
        state: MediaMusicPlayState,
        reason: MediaMusicReason,
    ) {
    }

}

interface IMediaRtcListener {

    /**
     * 音频渠道改变回调
     */
    fun onAudioRouteChanged(routing: AudioRouter) {}

    /**
     * 用户speaking回调
     */
    fun onUsersSpeaking(uidSet: Set<Int>) {}

    /**
     * 错误回调
     */
    fun onError(err: Int) {}

    fun onRejoinSuccess() {}

}

interface IMediaListener : IMediaRtcListener, IMediaMusicListener {


}