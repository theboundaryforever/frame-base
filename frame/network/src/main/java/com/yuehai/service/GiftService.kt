package com.yuehai.service

import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.wallet.EntityWalletInfo
import com.yuehai.data.collection.path.room.GiftResultList
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface GiftService {
    /**
     * 礼物列表, VIP栏的礼物需要是VIP才能送, Gift Bag栏要显示礼物数量
     * */
    @GET("api/gift/list")
    suspend fun getGiftList(): Rlt<Res<GiftResultList>>

    /**
     *
     * 获取钱包
     * */
    @GET("api/user/wallet")
    suspend fun getWallet(
    ): Rlt<Res<EntityWalletInfo>>

    /**
     * 送礼物推送type=100, 送全服礼物推送type=800, 幸运礼物中奖推送type=101
    voice_uid	    int	    房主id
    to_uid_list	string	收礼人uid, 多个收礼人用逗号隔开, 例如: 900,901,902
    gift_id		int	    礼物id
    count	1		int	    礼物数量
    is_bag		int	    是否背包礼物, 1是; 0否;
     * */
    @FormUrlEncoded
    @POST("api/gift/give_gift")
    suspend fun sendGift(
        @Field("voice_uid") voiceUid: Int,
        @Field("to_uid_list") toUidList: String,
        @Field("gift_id") giftId: Int,
        @Field("count") giftCount: Int,
        @Field("is_bag") isBag: Int,
        @Field("is_pm") isPrivate: Int=0
    ): Rlt<Res<EntityWalletInfo>>
}