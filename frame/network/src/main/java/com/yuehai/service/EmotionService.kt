package com.yuehai.service

import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.room.EmotionInfoList
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface EmotionService {
    /**
     * 表情包列表
     * */

    @GET("api/voice/emoticon_list")
    suspend fun getEmotionList(): Rlt<Res<EmotionInfoList>>

    /**
     * 发送表情
     * */
    @POST("api/voice/send_emoticon")
    @FormUrlEncoded
    suspend fun sendEmotion(
        @Field("voice_uid") voiceUid: Int,
        @Field("id") emotionId: Int,
    ): Rlt<Res<Res<Any>>>
}