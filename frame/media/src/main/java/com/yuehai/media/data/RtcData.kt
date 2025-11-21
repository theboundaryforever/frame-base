package com.yuehai.media.data


enum class ClientRole(val role: Int) {
    CLIENT_ROLE_BROADCASTER(1), //直播频道中的主播，可以发布和接收音视频流
    CLIENT_ROLE_AUDIENCE(2) //直播频道中的观众，只可以接收音视频流
}

enum class AudioRouter(private val value: Int) {
    AUDIO_ROUTE_DEFAULT(-1), //默认音频渠道
    AUDIO_ROUTE_HEADSET(0), //带麦克风的耳机
    AUDIO_ROUTE_EARPIECE(1),// 听筒
    AUDIO_ROUTE_HEADSET_NO_MIC(2), //不带麦克风的耳机
    AUDIO_ROUTE_SPEAKERPHONE(3), //设备自带的扬声器
    AUDIO_ROUTE_LOUDSPEAKER(4), //外接的扬声器
    AUDIO_ROUTE_HEADSET_BLUETOOTH(5); //蓝牙耳机

    companion object {
        fun getAudioRouterByValue(value: Int): AudioRouter {
            return when (value) {
                AUDIO_ROUTE_DEFAULT.value -> AUDIO_ROUTE_DEFAULT
                AUDIO_ROUTE_HEADSET.value -> AUDIO_ROUTE_HEADSET
                AUDIO_ROUTE_EARPIECE.value -> AUDIO_ROUTE_EARPIECE
                AUDIO_ROUTE_HEADSET_NO_MIC.value -> AUDIO_ROUTE_HEADSET_NO_MIC
                AUDIO_ROUTE_SPEAKERPHONE.value -> AUDIO_ROUTE_SPEAKERPHONE
                AUDIO_ROUTE_LOUDSPEAKER.value -> AUDIO_ROUTE_LOUDSPEAKER
                AUDIO_ROUTE_HEADSET_BLUETOOTH.value -> AUDIO_ROUTE_HEADSET_BLUETOOTH
                else -> AUDIO_ROUTE_DEFAULT
            }
        }
    }

}

enum class RtcType(val type: Int) {
    AGORA_RTC(0),
//    QTT_RTC(1),
    T_RTC(2),
    ZEGO_RTC(4);

    companion object {
        fun map(type: Int): RtcType {
            return values().find { it.type == type } ?: AGORA_RTC
        }
    }
}