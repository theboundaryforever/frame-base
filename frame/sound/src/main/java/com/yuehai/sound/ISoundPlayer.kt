package com.yuehai.sound

import com.yuehai.sound.config.ISoundPlayerConfig
import com.yuehai.sound.data.PlayerType
import com.yuehai.sound.data.SoundPriority
import com.yuehai.sound.data.SoundType


interface ISoundPlayer {

    val config: ISoundPlayerConfig

    fun prepare()

    /**
     * 播放音效
     *
     * @param fileName 文件名，需要带.acc等文件后缀
     */
    fun play(fileName: String, playerType: PlayerType? = null)

    fun play(fileName: String, loop: Int, playerType: PlayerType? = null)

    fun play(fileName: String, priority: SoundPriority, loop: Int, playerType: PlayerType? = null)

    fun play(
        fileName: String,
        soundType: SoundType,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType? = null
    )

    fun playCanInBg(
        fileName: String,
        soundType: SoundType,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType? = null
    ) {
    }

    fun resumeAll(fileNames: Set<String>)

    fun resumeAll()

    fun resume(fileName: String)

    fun pauseAll()

    fun pause(fileName: String)

    fun stopAll()

    fun stopAll(vararg excludeSoundName: String)

    fun stop(fileName: String)

    fun isPlay(fileName: String): Boolean

    fun isResume(fileName: String): Boolean

    fun isPause(fileName: String): Boolean

    fun isStop(fileName: String): Boolean

    fun release()

    fun getPlayerType(fileName: String): PlayerType?

}