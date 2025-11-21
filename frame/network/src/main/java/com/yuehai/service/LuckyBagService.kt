package com.yuehai.service

import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.room.LuckyBagInfo
import com.yuehai.data.collection.path.room.LuckyBagRecordResult
import com.yuehai.data.collection.path.room.LuckyBagRewardResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LuckyBagService {
    /**
     * 表情包列表
     * */

    @GET("api/lucky_bag/config")
    suspend fun getLuckyBagConfig(): Rlt<Res<LuckyBagInfo>>

    @FormUrlEncoded
    @POST("api/lucky_bag/send_lucky_bag")
    suspend fun sendLuckyBag(
        @Field("voice_uid") voiceUid: Int? = null,
        @Field("type") type: Int,
        @Field("range") range: Int,
        @Field("receive") receive: Int,
        @Field("validity") validity: Int,
        @Field("coin") coin: Int? = null,
        @Field("num") num: Int? = null,
        @Field("option_id") optionId: Int? = null,
    ): Rlt<Res<Any>>

    @GET("/api/lucky_bag/lucky_bag_info")
    suspend fun getLuckyBagInfo(
        @Query("voice_uid") voiceUid: Int?,
        @Query("lucky_bag_id") luckyBagId: Int?
    ): Rlt<Res<LuckyBagRewardResult>>

    @GET("/api/lucky_bag/receive")
    suspend fun receiveLuckyBag(
        @Query("voice_uid") voiceUid: Int?,
        @Query("lucky_bag_id") luckyBagId: Int?
    ): Rlt<Res<LuckyBagRewardResult>>

    @GET("/api/lucky_bag/lucky_bag_list")
    suspend fun getLuckyBagRecord(
        @Query("voice_uid") voiceUid: Int?,
    ): Rlt<Res<LuckyBagRecordResult>>


}