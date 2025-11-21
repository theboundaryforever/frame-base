package com.yuehai.sound

import android.util.Log
import androidx.annotation.CallSuper
import com.yuehai.sound.data.PlayStatus
import com.yuehai.sound.data.SoundInfo
import com.yuehai.sound.data.SoundType
import com.yuehai.sound.data.TAG_SOUND
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.iterator


abstract class BaseSoundPlayer : ISoundPlayer {

    protected var soundInfoMap: MutableMap<String, SoundInfo> = ConcurrentHashMap()

    open fun isUnablePlaySound(): Boolean {
        return !config.isCanPlaySound()
    }

    open fun isUnablePlayMusic(): Boolean {
        return !config.isCanPlayMusic()
    }

    protected open fun isUnablePlay(soundType: SoundType): Boolean {
        if (soundType == SoundType.SOUND) {
            return isUnablePlaySound()
        }

        if (soundType == SoundType.MUSIC) {
            return isUnablePlayMusic()
        }

        return true
    }

    protected open fun getSoundInfoByStreamId(streamId: Int): SoundInfo? {
        for ((_, soundInfo) in soundInfoMap) {
            if (soundInfo.streamId == streamId) {
                return soundInfo
            }
        }

        return null
    }

    protected open fun getSoundInfoByFileName(fileName: String): SoundInfo? {
        return soundInfoMap[fileName]
    }

    @CallSuper
    override fun resumeAll(fileNames: Set<String>) {
        Log.d(TAG_SOUND, "BaseSoundPlayer, resumeAll, fileNames:${fileNames}")
        for ((_, soundInfo) in soundInfoMap) {
            if (!fileNames.contains(soundInfo.fileName)) {
                continue
            }

            if (soundInfo.playStatus === PlayStatus.PAUSE) {
                resume(soundInfo.fileName)
                soundInfo.switchPlayStatus(PlayStatus.RESUME)
            }
        }
    }

    @CallSuper
    override fun resumeAll() {
        Log.d(TAG_SOUND, "resumeAll")
        for ((_, soundInfo) in soundInfoMap) {
            if (soundInfo.playStatus === PlayStatus.PAUSE) {
                resume(soundInfo.fileName)
                soundInfo.switchPlayStatus(PlayStatus.RESUME)
            }
        }
    }

    @CallSuper
    override fun pauseAll() {
        Log.d(TAG_SOUND, "pauseAll")
        for ((_, soundInfo) in soundInfoMap) {
            if (soundInfo.playStatus === PlayStatus.PLAY ||
                soundInfo.playStatus === PlayStatus.RESUME
            ) {
                pause(soundInfo.fileName)
                soundInfo.switchPlayStatus(PlayStatus.PAUSE)
            }
        }
    }

    @CallSuper
    override fun stopAll() {
        Log.d(TAG_SOUND, "stopAll")
        for ((_, soundInfo) in soundInfoMap) {
            stop(soundInfo.fileName)
            soundInfo.switchPlayStatus(PlayStatus.STOP)
        }
    }

    override fun stopAll(vararg excludeSoundName: String) {
        Log.d(TAG_SOUND, "stopAll, exclude: ${excludeSoundName.joinToString(separator = ",")}")
        for ((_, soundInfo) in soundInfoMap) {
            if (excludeSoundName.contains(soundInfo.fileName)) {
                continue
            }
            stop(soundInfo.fileName)
            soundInfo.switchPlayStatus(PlayStatus.STOP)
        }
    }

    override fun isPlay(fileName: String): Boolean {
        val soundInfo = getSoundInfoByFileName(fileName) ?: return false
        return soundInfo.playStatus === PlayStatus.PLAY
    }

    override fun isResume(fileName: String): Boolean {
        val soundInfo = getSoundInfoByFileName(fileName) ?: return false
        return soundInfo.playStatus === PlayStatus.RESUME
    }

    override fun isPause(fileName: String): Boolean {
        val soundInfo = getSoundInfoByFileName(fileName) ?: return false
        return soundInfo.playStatus === PlayStatus.PAUSE
    }

    override fun isStop(fileName: String): Boolean {
        val soundInfo = getSoundInfoByFileName(fileName) ?: return true
        return soundInfo.playStatus === PlayStatus.STOP
    }

    @CallSuper
    override fun release() {
        soundInfoMap.clear()
    }

    protected fun getSoundPath(fileName: String): String? {
        val audioPath = config.audioPath ?: return null
        return "${audioPath}${fileName}"
    }
}