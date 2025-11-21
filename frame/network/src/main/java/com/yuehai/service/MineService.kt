package com.yuehai.service

import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.mine.BillConditionInfo
import com.yuehai.data.collection.path.mine.CoinFlowResult
import com.yuehai.data.collection.path.mine.MallCategory
import com.yuehai.data.collection.path.mine.MallResultInfo
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.wallet.RechargeUserListInfo
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MineService {
    @GET("api/wallet/coin_log")
    suspend fun getCoinFlow(
        @Query("page") page: Int,
        @Query("type") type: Int,
        @Query("log_type") logType: Int,

        ): Rlt<Res<CoinFlowResult>>

    @GET("api/wallet/diamond_log")
    suspend fun getDiamondFlow(
        @Query("page") page: Int,
        @Query("type") type: Int,
        @Query("log_type") logType: Int,

        ): Rlt<Res<CoinFlowResult>>

    @GET("api/wallet/detail_list")
    suspend fun getBillCondition(
        @Query("coin_type")
        queryType: Int,
    ): Rlt<Res<BillConditionInfo>>

    @GET("api/mall/list")
    suspend fun getMallInfo(@Query("type") type: Int): Rlt<Res<MallResultInfo>>

    @GET("/api/mall/buy")
    suspend fun getBuyDressUp(@Query("id") id: Int): Rlt<Res<Any>>

    @GET("api/my_backpack/dress_up_list")
    suspend fun getPackageDressUpList(@Query("type") type: Int): Rlt<Res<MallCategory>>

    @GET("api/my_backpack/use_dress_up")
    suspend fun usePackageDressUp(@Query("status") status: Int, @Query("id") id: Int): Rlt<Res<Any>>

    //绑定手机发送验证码
    @POST("api/user_bind/bind_mobile_code")
    @FormUrlEncoded
    suspend fun bindPhoneGetCode(
        @Field("area_code") areaCode: String,
        @Field("mobile") mobile: String
    ): Rlt<Res<Any>>

    //绑定手机
    @POST("api/user_bind/bind_mobile")
    @FormUrlEncoded
    suspend fun bindPhone(
        @Field("area_code") areaCode: String,
        @Field("mobile") mobile: String,
        @Field("password") password: String,
        @Field("code") phoneCode: String
    ): Rlt<Res<Any>>

    //解绑手机发送验证码
    @POST("api/user_bind/unbind_mobile_code")
    @FormUrlEncoded
    suspend fun unbindPhoneGetCode(
        @Field("area_code") areaCode: String,
        @Field("mobile") mobile: String
    ): Rlt<Res<Any>>

    //解绑手机api/user_bind/unbind_mobile
    @POST("api/user_bind/unbind_mobile")
    @FormUrlEncoded
    suspend fun unbindPhone(
        @Field("password") password: String,
        @Field("code") code: String
    ): Rlt<Res<Any>>


    //修改密码发送验证码
    @POST("api/user/change_pass_code")
    @FormUrlEncoded
    suspend fun changePasswordGetCode(
        @Field("area_code") areaCode: String,
        @Field("mobile") mobile: String
    ): Rlt<Res<Any>>

    //修改密码
    @POST("api/user/change_pass")
    @FormUrlEncoded
    suspend fun changePassword(
        @Field("password") password: String,
        @Field("code") code: String
    ): Rlt<Res<Any>>

    @GET("api/recharge/agent_list")
    suspend fun getRechargeUserList(): Rlt<Res<RechargeUserListInfo>>


}