package com.yuehai.service

import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.room.FansAndFollowResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RoomShareService {

    @FormUrlEncoded
    @POST("api/user/follow_list")
    suspend fun getFollowListInfo(
        @Field("page") page: Int,
        @Field("key_word") keyWord: String?
    ): Rlt<Res<FansAndFollowResult>>

    @FormUrlEncoded
    @POST("api/user/fans_list")
    suspend fun getFansListInfo(
        @Field("page") page: Int,
        @Field("key_word") keyWord: String?
    ): Rlt<Res<FansAndFollowResult>>

    @GET("api/voice/share_room_pm")
    suspend fun shareRoomToUsers(
        @Query("voice_uid") voiceUid: String,
        @Query("to_uids") toUids: String
    ): Rlt<Res<Any>>
}