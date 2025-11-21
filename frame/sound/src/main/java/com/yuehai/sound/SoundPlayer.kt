package com.yuehai.sound

import android.text.TextUtils
import com.yuehai.sound.config.ISoundPlayerConfig
import com.yuehai.sound.data.PlayerType
import com.yuehai.sound.data.SoundPriority
import com.yuehai.sound.data.SoundType
import com.yuehai.sound.mediasdk.MediaSdkPlayer
import com.yuehai.coroutine.coroutine.dispatcher.Dispatcher
import com.yuehai.sound.soundpool.SoundPoolPlayer
import com.yuehai.sound.systemmedia.SystemMediaPlayer
import com.yuehai.util.util.runOnUiThread


@Synchronized
fun createSoundPlayer(config: ISoundPlayerConfig): ISoundPlayer {
    return SoundPlayer(config)
}

internal class SoundPlayer(override val config: ISoundPlayerConfig) : ISoundPlayer {

    private val mediaSdkPlayer: ISoundPlayer by lazy { MediaSdkPlayer(config) }
    private val soundPoolPlayer: ISoundPlayer by lazy { SoundPoolPlayer(config) }
    private val mediaPlayer: ISoundPlayer by lazy { SystemMediaPlayer(config) }
    private val playInfoMap =
        hashMapOf<String, PlayInfo>() // key: 播放文件filename, value: 播放信息PlayInfo

    override fun prepare() {
        Dispatcher.lowExecutor.execute {
            val audioPath = config.audioPath
            if (audioPath.isNullOrEmpty()) {
                return@execute
            }
//
//            if (copyVersionZipFromAssetsToFile(audioPath, config.audioZipFileName)) {
                runOnUiThread {
                    mediaSdkPlayer.prepare()
                    soundPoolPlayer.prepare()
                    mediaPlayer.prepare()
                }
//            }
        }
    }

    private fun isUseMediaSdk(): Boolean {
        return config.isUseMediaSdk()
    }

    private fun isUnablePlaySound(): Boolean {
        return !config.isInForeGround()
    }

    override fun play(fileName: String, playerType: PlayerType?) {
        play(fileName, 0, playerType)
    }

    override fun play(fileName: String, loop: Int, playerType: PlayerType?) {
        play(fileName, SoundPriority.NORMAL, loop, playerType)
    }

    override fun play(
        fileName: String,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType?
    ) {
        play(fileName, SoundType.SOUND, priority, loop, playerType)
    }

    override fun playCanInBg(
        fileName: String,
        soundType: SoundType,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType?
    ) {
        if (TextUtils.isEmpty(fileName)) {
            return
        }

        val playInfo = PlayInfo(soundType)
        playInfoMap[fileName] = playInfo
        if (playerType == null) {
            playInfo.playerType = if (isUseMediaSdk()) {
                PlayerType.MEDIA_SDK
            } else if (soundType == SoundType.MUSIC) {
                PlayerType.SYSTEM_MEDIA
            } else {
                PlayerType.SOUND_POOL
            }
        } else {
            playInfo.playerType = playerType
        }
        when (playInfo.playerType) {
            PlayerType.MEDIA_SDK -> mediaSdkPlayer.play(fileName, soundType, priority, loop)
            PlayerType.SOUND_POOL -> soundPoolPlayer.play(fileName, soundType, priority, loop)
            PlayerType.SYSTEM_MEDIA -> mediaPlayer.play(fileName, soundType, priority, loop)
        }
    }

    override fun play(
        fileName: String,
        soundType: SoundType,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType?
    ) {
        if (isUnablePlaySound()) {
            return
        }

        playCanInBg(fileName, soundType, priority, loop, playerType)
    }

    override fun resumeAll(fileNames: Set<String>) {
        if (isUnablePlaySound()) {
            return
        }

        mediaSdkPlayer.resumeAll(fileNames)
        mediaPlayer.resumeAll(fileNames)
        soundPoolPlayer.resumeAll(fileNames)
    }

    override fun resumeAll() {
        if (isUnablePlaySound()) {
            return
        }

        mediaSdkPlayer.resumeAll()
        mediaPlayer.resumeAll()
        soundPoolPlayer.resumeAll()
    }

    override fun resume(fileName: String) {
        if (isUnablePlaySound()) {
            return
        }

        val playInfo = playInfoMap[fileName] ?: return
        getPlayerByPlayerType(playInfo.playerType).resume(fileName)
    }

    override fun pauseAll() {
        mediaSdkPlayer.pauseAll()
        mediaPlayer.pauseAll()
        soundPoolPlayer.pauseAll()
    }

    override fun pause(fileName: String) {
        val playInfo = playInfoMap[fileName] ?: return
        getPlayerByPlayerType(playInfo.playerType).pause(fileName)
    }

    override fun stopAll() {
        mediaSdkPlayer.stopAll()
        mediaPlayer.stopAll()
        soundPoolPlayer.stopAll()
    }

    override fun stopAll(vararg excludeSoundName: String) {
        mediaSdkPlayer.stopAll(*excludeSoundName)
        mediaPlayer.stopAll(*excludeSoundName)
        soundPoolPlayer.stopAll(*excludeSoundName)
    }

    override fun stop(fileName: String) {
        val playInfo = playInfoMap[fileName] ?: return
        getPlayerByPlayerType(playInfo.playerType).stop(fileName)
    }

    override fun isPlay(fileName: String): Boolean {
        val playInfo = playInfoMap[fileName] ?: return false
        return getPlayerByPlayerType(playInfo.playerType).isPlay(fileName)
    }

    override fun isResume(fileName: String): Boolean {
        val playInfo = playInfoMap[fileName] ?: return false
        return getPlayerByPlayerType(playInfo.playerType).isResume(fileName)
    }

    override fun isPause(fileName: String): Boolean {
        val playInfo = playInfoMap[fileName] ?: return false
        return getPlayerByPlayerType(playInfo.playerType).isPause(fileName)
    }

    override fun isStop(fileName: String): Boolean {
        val playInfo = playInfoMap[fileName] ?: return true
        return getPlayerByPlayerType(playInfo.playerType).isStop(fileName)
    }

    override fun release() {
        mediaSdkPlayer.release()
        soundPoolPlayer.release()
        mediaPlayer.release()
        playInfoMap.clear()
    }

    override fun getPlayerType(fileName: String): PlayerType? {
        return playInfoMap[fileName]?.playerType
    }

    private fun getPlayerByPlayerType(playerType: PlayerType): ISoundPlayer {
        return when (playerType) {
            PlayerType.MEDIA_SDK -> mediaSdkPlayer
            PlayerType.SYSTEM_MEDIA -> mediaPlayer
            PlayerType.SOUND_POOL -> soundPoolPlayer
        }
    }

    internal data class PlayInfo(
        var soundType: SoundType,
        var playerType: PlayerType = PlayerType.SYSTEM_MEDIA
    )

}