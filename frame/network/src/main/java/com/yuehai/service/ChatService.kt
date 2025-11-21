package com.yuehai.service

import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.chat.NotificationUnReadCount
import com.yuehai.data.collection.path.chat.SystemMsgInfo
import com.yuehai.data.collection.path.login.OtherUserInResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ChatService {
    @GET("api/user/user_info")
    suspend fun getUserInfo(
        @Query("to_uid") toUid: String?, //用户id
    ): Rlt<Res<OtherUserInResult>>

    @POST("api/user/follow")
    @FormUrlEncoded
    suspend fun addUserFollow(
        @Field("to_uid") toUid: String?, //用户id
    ): Rlt<Res<Any>>

    @POST("api/user/unfollow")
    @FormUrlEncoded
    suspend fun unUserFollow(
        @Field("to_uid") toUid: String?, //用户id
    ): Rlt<Res<Any>>

    @FormUrlEncoded
    @POST("api/user/block_user")
    suspend fun blockUserStatus(
        @Field("to_uid") toUid: String, // 目标用户id 必填
        @Field("status") status: Int, // 是否拉黑用户: 1是; 0否;
    ): Rlt<Res<Any>>

    @GET("api/user/private_relation")
    suspend fun getChatUserInfo(@Query("to_uid") toUid: String?): Rlt<Res<OtherUserInResult>>

    //未读消息
    @GET("api/message/unread")
    suspend fun getNotificationCountMsg(): Rlt<Res<NotificationUnReadCount>>

    //系统消息
    @GET("api/message/system_list")
    suspend fun getSystemNotificationMsg(@Query("page") page: Int): Rlt<Res<SystemMsgInfo>>

    //活动消息
    @GET("api/message/event_list")
    suspend fun getActivityNotificationMsg(@Query("page") page: Int): Rlt<Res<SystemMsgInfo>>
}