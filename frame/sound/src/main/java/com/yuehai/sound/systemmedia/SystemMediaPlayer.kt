package com.yuehai.sound.systemmedia

import android.media.MediaPlayer
import android.util.Log
import com.yuehai.sound.BaseSoundPlayer
import com.yuehai.sound.config.ISoundPlayerConfig
import com.yuehai.sound.data.MediaPlayerSoundInfo
import com.yuehai.sound.data.PlayStatus
import com.yuehai.sound.data.PlayerType
import com.yuehai.sound.data.PrepareStatus
import com.yuehai.sound.data.SOUND_LOOP_INFINITE
import com.yuehai.sound.data.SoundInfo
import com.yuehai.sound.data.SoundPriority
import com.yuehai.sound.data.SoundType
import com.yuehai.sound.data.TAG_SOUND
import kotlin.collections.iterator


class SystemMediaPlayer(override val config: ISoundPlayerConfig) : BaseSoundPlayer(),
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private val player: MediaPlayer by lazy { MediaPlayer() }

    override fun prepare() {
        Log.d(TAG_SOUND, "SystemMediaPlayer, prepare")
        val soundInfo = getPlayedSoundInfo() ?: return
        if (soundInfo.fileName.isEmpty()) {
            return
        }

        Log.d(TAG_SOUND, "SystemMediaPlayer, prepare, play sound file:${soundInfo.fileName}")
        play(soundInfo.fileName, soundInfo.soundType, soundInfo.priority, soundInfo.loop)
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

    override fun play(
        fileName: String,
        soundType: SoundType,
        priority: SoundPriority,
        loop: Int,
        playerType: PlayerType?
    ) {
        Log.d(TAG_SOUND, "SystemMediaPlayer, play, fileName:${fileName}")
        if (isUnablePlay(soundType)) {
            return
        }

        var soundInfo = soundInfoMap[fileName] as? MediaPlayerSoundInfo
        Log.d(TAG_SOUND, "SystemMediaPlayer, play, soundInfo:${soundInfo}")
        if (soundInfo != null && soundInfo.fileName == fileName) {
            if (soundInfo.playStatus === PlayStatus.STOP) {
                try {
                    player.reset()
                    player.setDataSource(soundInfo.filePath)
                    player.setOnPreparedListener(this)
                    player.setOnErrorListener(this)
                    player.isLooping = loop == SOUND_LOOP_INFINITE
                    player.prepareAsync()
                    soundInfo.switchPrepareStatus(PrepareStatus.PREPARING)
                    soundInfo.switchPlayStatus(PlayStatus.PLAY)
                } catch (e : Exception) {
                    Log.e(TAG_SOUND, "SystemMediaPlayer, play, re prepare e:${e}")
                }
                return
            }

            if (soundInfo.callStart) {
                resume(fileName)
            }
            return
        } else {
            getPlayedSoundInfo()?.let {
                stop(it.fileName)
            }
        }
        soundInfoMap.clear()
        soundInfo = MediaPlayerSoundInfo(fileName, soundType, priority, loop)
        soundInfoMap[fileName] = soundInfo
        try {
            val path = getSoundPath(fileName)
            if (!path.isNullOrEmpty()) {
                Log.d(TAG_SOUND, "SystemMediaPlayer, play, path:${path}")
                player.reset()
                player.setDataSource(path)
                player.setOnPreparedListener(this)
                player.setOnErrorListener(this)
                player.isLooping = loop == SOUND_LOOP_INFINITE
                player.prepareAsync()
                soundInfo.switchPrepareStatus(PrepareStatus.PREPARING)
                soundInfo.switchPlayStatus(PlayStatus.PLAY)
                soundInfo.filePath = path
            }
        } catch (e: Exception) {
            Log.e(TAG_SOUND, "SystemMediaPlayer, play, prepare e:${e}")
        }
    }

    private fun getPlayedSoundInfo(): SoundInfo? {
        var soundInfo: MediaPlayerSoundInfo? = null
        for ((_, value) in soundInfoMap) {
            soundInfo = value as? MediaPlayerSoundInfo
            break
        }

        return soundInfo
    }

    override fun onPrepared(mp: MediaPlayer?) {
        Log.d(TAG_SOUND, "SystemMediaPlayer, onPrepared")
        val soundInfo = getPlayedSoundInfo() as? MediaPlayerSoundInfo ?: return
        Log.d(TAG_SOUND, "SystemMediaPlayer, onPrepared, soundInfo:${soundInfo}")
        soundInfo.switchPrepareStatus(PrepareStatus.SUCCESS)
        if (soundInfo.playStatus === PlayStatus.PLAY || soundInfo.playStatus === PlayStatus.RESUME) {
            callStart(soundInfo)
        }
    }

    private fun callStart(soundInfo: MediaPlayerSoundInfo?) {
        Log.d(TAG_SOUND, "SystemMediaPlayer, callStart, soundInfo:${soundInfo}")
        if (soundInfo == null) {
            return
        }

        try {
            if (!player.isPlaying) {
                Log.d(TAG_SOUND, "SystemMediaPlayer, callStart, start, soundInfo:${soundInfo}")
                val volume = config.defaultVolume.toFloat() / 100
                player.setVolume(volume, volume)
                player.start()
            }
            soundInfo.callStart = true
        } catch (e: IllegalStateException) {
            Log.e(TAG_SOUND, "SystemMediaPlayer, media player resume catch illegal state exception")
        }
    }

    override fun resume(fileName: String) {
        Log.d(TAG_SOUND, "SystemMediaPlayer, resume, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) as? MediaPlayerSoundInfo ?: return
        Log.d(TAG_SOUND, "SystemMediaPlayer, resume, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.RESUME)
        if (isUnablePlay(soundInfo.soundType)) {
            return
        }

        if (soundInfo.prepareStatus !== PrepareStatus.SUCCESS) {
            return
        }

        callStart(soundInfo)
    }

    override fun pause(fileName: String) {
        Log.d(TAG_SOUND, "SystemMediaPlayer, pause，fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) as? MediaPlayerSoundInfo ?: return
        Log.d(TAG_SOUND, "SystemMediaPlayer, pause, fileName:${fileName}")
        soundInfo.switchPlayStatus(PlayStatus.PAUSE)
        try {
            if (player.isPlaying) {
                player.pause()
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG_SOUND, "SystemMediaPlayer, media player pause catch illegal state exception")
        }
    }

    override fun stop(fileName: String) {
        Log.d(TAG_SOUND, "SystemMediaPlayer, stop, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) as? MediaPlayerSoundInfo ?: return
        Log.d(TAG_SOUND, "SystemMediaPlayer, stop, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.STOP)
        try {
            player.stop() // stop状态，需要prepare来恢复
        } catch (e: IllegalStateException) {
            Log.e(TAG_SOUND, "SystemMediaPlayer, media player stop catch illegal state exception")
        }
        player.reset() // error state 可以通过reset来恢复
    }

    override fun getPlayerType(fileName: String): PlayerType {
        return PlayerType.SYSTEM_MEDIA
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Log.d(
            TAG_SOUND,
            "SystemMediaPlayer, onError, called with: mp = [$mp], what = [$what], extra = [$extra]"
        )
        return false
    }

    override fun release() {
        super.release()
//        if (mPlayer != null) {
//            mPlayer!!.release()
//            mPlayer = null
//        }
    }

}