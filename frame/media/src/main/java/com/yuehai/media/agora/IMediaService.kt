package com.yuehai.media.agora

import com.yuehai.data.collection.path.Rlt
import com.yuehai.media.data.ClientRole
import com.yuehai.media.data.MediaMusicPlayState
import com.yuehai.media.data.RtcType
import com.yuehai.media.listener.IMediaMusicListener
import com.yuehai.media.listener.IMediaRtcListener

interface ISoundEffectService {

    fun setSoundEffectVolume(soundId: Int, volume: Int)

    /**
     * 播放音效
     * @param filePath: 文件路径
     * @param loopCount: 播放次数，-1：无效循环
     * @param publish: 是否发布远端
     */
    fun playSoundEffect(filePath: String, loopCount: Int = 1, publish: Boolean = false): Int

    fun pauseSoundEffect(effectId: Int)

    fun resumeSoundEffect(effectId: Int)

    fun stopSoundEffect(effectId: Int)

}

interface IMusicService {

    /**
     * 获取音乐播放状态
     */
    fun getMusicPlayState(): MediaMusicPlayState

    /**
     * 获取当前播放音乐文件路径
     */
    fun getMusicFilePath(): String?

    /**
     * 播放音乐
     * @param filePath: 指定需要混音的本地或在线音乐文件的绝对路径
     * @param loopback: 设置是否只在本地播放音乐文件。true 表示只有本地用户能听到音乐；false 表示本地用户和远端用户都能听到音乐
     * @param replace:  设置是否用音乐文件替换麦克风采集的音频。true 表示用户只能听到音乐；false 表示用户可以听到音乐和麦克风采集的音频
     * @param cycle:    设置音乐文件的播放次数。-1 表示循环播放
     */
    fun playMusic(
        filePath: String,
        loopback: Boolean = false,
        replace: Boolean = false,
        cycle: Int = 1,
    )

    /**
     * 暂停播放音乐
     */
    fun pauseMusic()

    /**
     * 恢复播放音乐
     */
    fun resumeMusic()

    /**
     * 停止播放音乐
     */
    fun stopMusic()

    /**
     * 获取当前播放音乐时长，单位ms
     */
    fun getMusicDuration(): Int

    /**
     * 获取当前播放音乐位置，单位ms
     */
    fun getMusicPlayPos(): Int

    /**
     * 设置当前音乐播放位置
     */
    fun setMusicPlayPos(posMS: Int)

    /**
     * 设置音乐本地和远端播放音量
     * @param volume: 音量，取值范围为 [0,100]，100 表示原始音量
     */
    fun setMusicVolume(volume: Int)

    /**
     * 设置音乐本地播放音量
     * @param volume: 音量，取值范围为 [0,100]，100 表示原始音量
     */
    fun setMusicLocalVolume(volume: Int)

    /**
     * 获取音乐本地播放音量
     * @return 音量，取值范围为 [0,100]，100 表示原始音量
     */
    fun getMusicLocalVolume(): Int

    /**
     * 设置音乐远端播放音量
     * @param volume: 音量，取值范围为 [0,100]，100 表示原始音量
     */
    fun setMusicRemoteVolume(volume: Int)

    /**
     * 获取音乐远端播放音量
     * @return 音量，取值范围为 [0,100]，100 表示原始音量
     */
    fun getMusicRemoteVolume(): Int

    fun addMediaMusicListener(l: IMediaMusicListener)

    fun removeMediaMusicListener(l: IMediaMusicListener)

}

enum class ChannelProfile(val profile: Int) {
    COMMUNICATION(0), //通信场景。该场景下，频道内所有用户都可以发布和接收音、视频流。适用于语音通话、视频群聊等应用场景。
    LIVE_BROADCASTING(1), //直播场景。该场景有主播和观众两种用户角色，可以通过 setClientRole 设置。主播可以发布和接收音视频流，观众直接接收流。适用于语聊房、视频直播、互动大班课等应用场景。
}

enum class SystemVolumeType(val type: Int) {
    AUTO(0), //自动
    MEDIA(1), //扬声器
    VOIP(2), //听筒
}

enum class AudioScenario(val value: Int) {
    DEFAULT(0),
    CHATROOM_ENTERTAINMENT(1),
    EDUCATION(2),
    GAME_STREAMING(3),
    SHOWROOM(4),
    CHATROOM_GAMING(5),
    IOT(6),
    MEETING(8);
}

enum class AudioProfile(val value: Int) {
    DEFAULT(0),
    SPEECH_STANDARD(1),
    MUSIC_STANDARD(2),
    MUSIC_STANDARD_STEREO(3),
    MUSIC_HIGH_QUALITY(4),
    MUSIC_HIGH_QUALITY_STEREO(5);
}

interface IMediaRtcService {

    fun setRtcType(rtcType: RtcType)

    fun getRtcType(): RtcType

    fun setSystemVolumeType(type: SystemVolumeType)

    fun getCurrentChannel(): String?

    fun setAudioProfile(profile: AudioProfile, scenario: AudioScenario)

    /**
     * 设置频道场景
     */
    fun setChannelProfile(profile: ChannelProfile)

    suspend fun joinChannel(channelName: String, token: String, uid: Int): Rlt<Any>

    suspend fun leaveChannel(): Rlt<Any>

    /**
     * 已加入媒体渠道
     */
    fun isChannelJoined(): Boolean

    /**
     * 开启/关闭本地音频采集（是否采集）
     * enabled: true:（默认）重新开启本地语音，即开启本地语音采集，false: 关闭本地语音，即停止本地语音采集
     */
    fun enableLocalAudio(enabled: Boolean)

    /*
    *
    *
    * */


    /**
     * 取消或恢复发布本地音频流（是否发流）
     * mute: true：取消发布，false：发布
     */
    fun muteLocalAudioStream(mute: Boolean)

    /**
     * 取消或恢复订阅所有远端用户的音频流（是否推流）
     * mute: true: 取消订阅，false:（默认）订阅
     */
    fun muteAllRemoteAudioStreams(mute: Boolean)

    fun setEnableSpeakerphone(enable: Boolean)

    fun setEnableEarPhone(enable: Boolean)

    fun isSpeakerphoneEnable(): Boolean

    fun setClientRole(role: ClientRole)

    /**
     * 调节人声语音音量的大小
     * volume: 取值范围【0-100】默认值100
     */
    fun adjustRecordingSignalVolume(volume: Int)

    fun getRecordSignalVolume(): Int

    fun addMediaRtcListener(l: IMediaRtcListener)

    fun removeMediaRtcListener(l: IMediaRtcListener)

    /**
     * 媒体使用中
     */
    fun isMediaUsing(): Boolean

    fun destroy()

}

interface IMediaService : IMediaRtcService, IMusicService, ISoundEffectService {

}

