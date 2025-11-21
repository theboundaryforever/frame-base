package com.yuehai.media.data


/**
 * 媒体音乐播放状态
 */
enum class MediaMusicPlayState {
    PLAY,
    PAUSE,
    STOP
}

/**
 * 媒体音乐播放原因
 */
enum class MediaMusicReason {
    CAN_NOT_OPEN, //701，音乐文件打开出错。例如，本地音乐文件不存在、文件格式不支持或无法访问在线音乐文件 URL。
    TOO_FREQUENT_CALL, //702，音乐文件打开太频繁。如需多次调用 startAudioMixing，请确保调用间隔大于 500 ms。
    INTERRUPTED_EOF, //703，音乐文件播放中断。
    STARTED_BY_USER, //720，成功调用 startAudioMixing 播放音乐文件。
    ONE_LOOP_COMPLETED, //721，音乐文件完成一次循环播放。
    START_NEW_LOOP, //722，音乐文件开始新的一次循环播放。
    ALL_LOOPS_COMPLETED, //723，音乐文件完成所有循环播放。
    STOPPED_BY_USER, //724，成功调用 stopAudioMixing 停止播放音乐文件。
    PAUSED_BY_USER, //725，成功调用 pauseAudioMixing 暂停播放音乐文件。
    RESUMED_BY_USER, //726，成功调用 resumeAudioMixing 恢复音乐文件播放。
    REAL_STOPPED_BY_USER, //WeNext自定义实现
    UNKNOWN, //未知错误
}

enum class StopMusicReason {
    INITIATIVE,
    RTC_CHANGED,
    ROOM_LEAVED
}