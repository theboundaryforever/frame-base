package com.yuehai.media.agora


interface IMediaOperatorGet {

    fun getMediaOperator(): IMediaOperator?

}

interface IMediaOperator {

    suspend fun isMediaIn(): Boolean

    suspend fun leaveMedia(): Boolean

    fun getConflictConfig(): MediaConflictConfig

    fun getJoinedRoomId(): Long?

    suspend fun rejoinRoom(): Boolean

}

data class MediaInfo(
    val mediaType: Int, /*媒体类型*/
    val mediaName: String, /*媒体功能名*/
)

data class MediaConflictConfig(
    val mediaInfo: MediaInfo,
    val sameTypeConflict: Boolean = false, /*是否做相同媒体功能冲突检测*/
)