package com.yuehai.sound.data



/**
 * 音效优先级
 */
enum class SoundPriority(val priority: Int) {
    LOW(0),
    NORMAL(1),
    HIGH(2);
}

/**
 * 音效类型
 */
enum class SoundType(val type: Int) {
    SOUND(0), //短音效
    MUSIC(1); //长音效
}

/**
 * 播放器类型
 */
enum class PlayerType(val type: Int) {
    MEDIA_SDK(1), //媒体sdk
    SOUND_POOL(2), //SoundPool
    SYSTEM_MEDIA(3) //MediaPlayer
}

/**
 * 播放状态
 */
enum class PlayStatus(val status: Int) {
    STOP(0),
    PAUSE(1),
    RESUME(2),
    PLAY(3)
}

const val INVALID_STREAM_ID = 0

const val SOUND_LOOP_INFINITE = -1 // 无限循环

sealed class SoundInfo(
    open val fileName: String,
    open val soundType: SoundType,
    open val priority: SoundPriority,
    open val loop: Int,
    open var filePath: String = "",
    open var streamId: Int = 0,
    open var playStatus: PlayStatus = PlayStatus.STOP
) {
    @Synchronized
    fun switchPlayStatus(playStatus: PlayStatus) {
        this.playStatus = playStatus
    }

    override fun toString(): String {
        return "SoundInfo(fileName='$fileName', soundType=$soundType, priority=$priority, loop=$loop, filePath='$filePath', streamId=$streamId, playStatus=$playStatus)"
    }
}

enum class PrepareStatus(val status: Int) {
    PREPARING(0),
    SUCCESS(1),
    FAILED(2)
}

data class MediaPlayerSoundInfo(
    override val fileName: String,
    override val soundType: SoundType,
    override val priority: SoundPriority,
    override val loop: Int,
    var prepareStatus: PrepareStatus = PrepareStatus.PREPARING,
    var callStart: Boolean = false
) : SoundInfo(fileName, soundType, priority, loop) {

    @Synchronized
    fun switchPrepareStatus(prepareStatus: PrepareStatus) {
        this.prepareStatus = prepareStatus
    }

    override fun toString(): String {
        return "MediaPlayerSoundInfo(fileName='$fileName', soundType=$soundType, priority=$priority, loop=$loop, prepareStatus=$prepareStatus, callStart=$callStart) ${super.toString()}"
    }

}

enum class SoundLoadStatus(val status: Int) {
    LOADING(0),
    SUCCESS(1),
    FAILED(2)
}

class SoundPoolSoundInfo(
    override val fileName: String,
    override val soundType: SoundType,
    override val priority: SoundPriority,
    override val loop: Int,
    var loadStatus: SoundLoadStatus = SoundLoadStatus.LOADING,
    var callPlay: Boolean = false, // 是否真正调用了play播放方法
    var soundId: Int = 0, // 用于load返回值，用于play播放音乐和unload
) : SoundInfo(fileName, soundType, priority, loop) {

    @Synchronized
    fun switchLoadStatus(loadStatus: SoundLoadStatus) {
        this.loadStatus = loadStatus
    }

    override fun toString(): String {
        return "SoundPoolSoundInfo(fileName='$fileName', soundType=$soundType, priority=$priority, loop=$loop, loadStatus=$loadStatus, callPlay=$callPlay, soundId=$soundId) ${super.toString()}"
    }

}

class MediaSdkSoundInfo(
    override val fileName: String,
    override val soundType: SoundType,
    override val priority: SoundPriority,
    override val loop: Int,
) : SoundInfo(fileName, soundType, priority, loop) {

    override fun toString(): String {
        return "MediaSdkSoundInfo(fileName='$fileName', soundType=$soundType, priority=$priority, loop=$loop) ${super.toString()}"
    }

}