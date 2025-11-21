package com.yuehai.sound.mediasdk

import android.util.Log
import com.yuehai.sound.BaseSoundPlayer
import com.yuehai.sound.config.ISoundPlayerConfig
import com.yuehai.sound.data.MediaSdkSoundInfo
import com.yuehai.sound.data.PlayStatus
import com.yuehai.sound.data.PlayerType
import com.yuehai.sound.data.SoundPriority
import com.yuehai.sound.data.SoundType
import com.yuehai.sound.data.TAG_SOUND


class MediaSdkPlayer(override val config: ISoundPlayerConfig) : BaseSoundPlayer() {

    override fun prepare() {

    }

    override fun play(fileName: String, playerType: PlayerType?) {
        play(fileName, 0, playerType)
    }

    override fun play(fileName: String, loop: Int, playerType: PlayerType?) {
        play(fileName, SoundPriority.NORMAL, 0, playerType)
    }

    override fun play(
        fileName: String,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType?
    ) {
        play(fileName, SoundType.SOUND, priority, loop, playerType)
    }

    override fun play(
        fileName: String,
        soundType: SoundType,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType?
    ) {
        Log.d(
            TAG_SOUND,
            "MediaSdkPlayer, play, fileName:${fileName}, soundType:${soundType}, priority:${priority}, loop:${loop}"
        )
        if (isUnablePlay(soundType)) {
            return
        }

        var soundInfo = soundInfoMap[fileName] as? MediaSdkSoundInfo
        if (soundInfo == null) {
            val filePath = getSoundPath(fileName)
            if (filePath.isNullOrEmpty()) {
                return
            }

            soundInfo = MediaSdkSoundInfo(fileName, soundType, priority, loop)
            soundInfo.filePath = filePath
            soundInfoMap[fileName] = soundInfo
        } else {
            config.mediaSdkSoundEffect.stopSoundEffect(soundInfo.streamId)
        }
        soundInfo.streamId = config.mediaSdkSoundEffect.playSoundEffect(soundInfo.filePath, loop)
        config.mediaSdkSoundEffect.setSoundEffectVolume(soundInfo.streamId, config.defaultVolume)
        soundInfo.switchPlayStatus(PlayStatus.PLAY)
    }

    override fun resumeAll(fileNames: Set<String>) {
        super.resumeAll(fileNames)
    }

    override fun resumeAll() {
        super.resumeAll()
    }

    override fun resume(fileName: String) {
        Log.d(TAG_SOUND, "MediaSdkPlayer, resume, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) ?: return
        Log.d(TAG_SOUND, "MediaSdkPlayer, resume, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.RESUME)
        if (isUnablePlay(soundInfo.soundType)) {
            return
        }

        config.mediaSdkSoundEffect.resumeSoundEffect(soundInfo.streamId)
    }

    override fun pauseAll() {
        super.pauseAll()
    }

    override fun pause(fileName: String) {
        Log.d(TAG_SOUND, "MediaSdkPlayer, pause, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) ?: return
        Log.d(TAG_SOUND, "MediaSdkPlayer, pause, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.PAUSE)
        config.mediaSdkSoundEffect.pauseSoundEffect(soundInfo.streamId)
    }

    override fun stopAll() {
        super.stopAll()
    }

    override fun stop(fileName: String) {
        Log.d(TAG_SOUND, "MediaSdkPlayer, stop, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) ?: return
        Log.d(TAG_SOUND, "MediaSdkPlayer, stop, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.STOP)
        config.mediaSdkSoundEffect.stopSoundEffect(soundInfo.streamId)
    }

    override fun getPlayerType(fileName: String): PlayerType {
        return PlayerType.MEDIA_SDK
    }

    override fun release() {
        super.release()
        Log.d(TAG_SOUND, "MediaSdkPlayer, release")
    }

}