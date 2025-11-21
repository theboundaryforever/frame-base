package com.yuehai.sound.soundpool

import android.media.AudioAttributes
import android.media.SoundPool
import android.media.audiofx.AudioEffect
import android.os.Build
import android.util.Log
import com.yuehai.sound.BaseSoundPlayer
import com.yuehai.sound.config.ISoundPlayerConfig
import com.yuehai.sound.data.INVALID_STREAM_ID
import com.yuehai.sound.data.PlayStatus
import com.yuehai.sound.data.PlayerType
import com.yuehai.sound.data.SOUND_LOOP_INFINITE
import com.yuehai.sound.data.SoundInfo
import com.yuehai.sound.data.SoundLoadStatus
import com.yuehai.sound.data.SoundPoolSoundInfo
import com.yuehai.sound.data.SoundPriority
import com.yuehai.sound.data.SoundType
import com.yuehai.sound.data.TAG_SOUND
import kotlin.collections.iterator


class SoundPoolPlayer(override val config: ISoundPlayerConfig) : BaseSoundPlayer(),
    SoundPool.OnLoadCompleteListener {

    companion object {
        private const val MAX_STREAMS = 10
    }

    private val soundPool: SoundPool by lazy {
        val soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioEffect.CONTENT_TYPE_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            SoundPool(MAX_STREAMS, AudioEffect.CONTENT_TYPE_MUSIC, 0)
        }
        soundPool.setOnLoadCompleteListener(this)
        return@lazy soundPool
    }

    override fun prepare() {
        Log.d(TAG_SOUND, "SoundPoolPlayer, prepare")
    }

    protected fun getSoundInfoBySoundId(soundId: Int): SoundInfo? {
        for ((_, value) in soundInfoMap) {
            val soundInfo = value as? SoundPoolSoundInfo
            if (soundInfo?.soundId == soundId) {
                return soundInfo
            }
        }

        return null
    }

    private fun load(filename: String, soundInfo: SoundPoolSoundInfo): Int {
        var soundId: Int = INVALID_STREAM_ID
        try {
            val filePath = getSoundPath(filename)
            if (filePath.isNullOrEmpty()) {
                Log.e(TAG_SOUND, "SoundPoolPlayer, load filename:${filename}, filePath is null")
                return soundId
            }

            soundInfo.filePath = filePath
            soundId = soundPool.load(filePath, 0)
        } catch (e: Exception) {
            Log.e(TAG_SOUND, "SoundPoolPlayer, load e:${e}")
        }
        return soundId
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
        Log.d(TAG_SOUND, "")
        if (isUnablePlay(soundType)) {
            return
        }

        var soundInfo = soundInfoMap[fileName] as? SoundPoolSoundInfo
        if (soundInfo == null) {
            soundInfo = SoundPoolSoundInfo(fileName, soundType, priority, loop)
            soundInfo.soundId = load(fileName, soundInfo)
            soundInfoMap[fileName] = soundInfo
        }
        if (loop == SOUND_LOOP_INFINITE && soundInfo.callPlay) {
            resume(fileName)
            return
        }

        if (soundInfo.loadStatus === SoundLoadStatus.SUCCESS) {
            val streamId = soundPool.play(soundInfo.soundId, 1f, 1f, priority.priority, loop, 1f)
            val volume = config.defaultVolume.toFloat() / 100
            soundPool.setVolume(streamId, volume, volume)
            soundInfo.streamId = streamId
            soundInfo.callPlay = true
        } else if (soundInfo.loadStatus === SoundLoadStatus.FAILED) {
            soundInfo.soundId = load(fileName, soundInfo)
        }
        soundInfo.switchPlayStatus(PlayStatus.PLAY)
    }

    override fun resumeAll(fileNames: Set<String>) {
        super.resumeAll(fileNames)
    }

    override fun resumeAll() {
        super.resumeAll()
    }

    override fun resume(fileName: String) {
        Log.d(TAG_SOUND, "SoundPoolPlayer, resume, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) as? SoundPoolSoundInfo ?: return
        Log.d(TAG_SOUND, "SoundPoolPlayer, resume, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.RESUME)
        if (isUnablePlay(soundInfo.soundType)) {
            return
        }

        if (soundInfo.loadStatus !== SoundLoadStatus.SUCCESS) {
            return
        }

        if (!soundInfo.callPlay) {
            play(soundInfo.fileName, soundInfo.soundType, soundInfo.priority, soundInfo.loop)
            return
        }

        if (soundInfo.streamId == INVALID_STREAM_ID) {
            return
        }

        soundPool.resume(soundInfo.streamId)
    }

    override fun pauseAll() {
        super.pauseAll()
    }

    override fun pause(fileName: String) {
        Log.d(TAG_SOUND, "SoundPoolPlayer, pause, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) ?: return
        Log.d(TAG_SOUND, "SoundPoolPlayer, pause, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.PAUSE)
        if (soundInfo.streamId == INVALID_STREAM_ID) {
            return
        }

        soundPool.pause(soundInfo.streamId)
    }

    override fun stopAll() {
        super.stopAll()
    }

    override fun stop(fileName: String) {
        Log.d(TAG_SOUND, "SoundPoolPlayer, stop, fileName:${fileName}")
        val soundInfo = getSoundInfoByFileName(fileName) ?: return
        Log.d(TAG_SOUND, "SoundPoolPlayer, stop, soundInfo:${soundInfo}")
        soundInfo.switchPlayStatus(PlayStatus.STOP)
        if (soundInfo.streamId == INVALID_STREAM_ID) {
            return
        }

        soundPool.stop(soundInfo.streamId)
    }

    override fun getPlayerType(fileName: String): PlayerType {
        return PlayerType.SOUND_POOL
    }

    override fun onLoadComplete(soundPool: SoundPool?, sampleId: Int, status: Int) {
        Log.d(TAG_SOUND, "SoundPoolPlayer, onLoadComplete, sampleId:${sampleId}, status:${status}")
        val soundInfo = getSoundInfoBySoundId(sampleId) as? SoundPoolSoundInfo
        if (soundInfo != null) {
            soundInfo.switchLoadStatus(if (status == 0) SoundLoadStatus.SUCCESS else SoundLoadStatus.FAILED)
            if (soundInfo.loadStatus === SoundLoadStatus.SUCCESS &&
                (soundInfo.playStatus === PlayStatus.PLAY || soundInfo.playStatus === PlayStatus.RESUME)
            ) {
                play(soundInfo.fileName, soundInfo.soundType, soundInfo.priority, soundInfo.loop)
            }
        }
    }

    override fun release() {
        super.release()
//        if (mSoundPool != null) {
//            mSoundPool!!.release()
//            mSoundPool = null
//        }
    }


}