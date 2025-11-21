package com.yuehai.sound.mediasdk


interface IMediaSdkSoundEffect {

    fun setSoundEffectVolume(soundId: Int, volume: Int)

    /**
     * 播放音效
     * @param filePath: 文件路径
     * @param loopCount: 播放次数，-1：无效循环
     */
    fun playSoundEffect(filePath: String, loopCount: Int = 0): Int

    fun pauseSoundEffect(effectId: Int)

    fun resumeSoundEffect(effectId: Int)

    fun stopSoundEffect(effectId: Int)

}