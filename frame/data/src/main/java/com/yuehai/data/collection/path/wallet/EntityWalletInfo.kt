package com.yuehai.data.collection.path.wallet

import com.google.gson.annotations.SerializedName
import com.yuehai.data.collection.path.home.BannerActivityInfo
import com.yuehai.data.collection.path.user.UserInfo
import java.math.BigDecimal
import java.text.DecimalFormat

data class EntityWalletInfo(
    val wallet: Wallet,
    @SerializedName("banner_list")
    val bannerList: List<BannerActivityInfo> = ArrayList(),
    @SerializedName("jump_list")
    val jumpList: List<BannerActivityInfo> = ArrayList()
)

data class Wallet(
    val coin: Double = 0.0, val diamond: Double = 0.0,

    )


//充值列表

data class ChargeResultInfo(
    @SerializedName("list") val chargeInfoList: List<ChargeInfo> = ArrayList<ChargeInfo>()
)

data class ChargeInfo(
    @SerializedName("icon") val icon: String,
    @SerializedName("money") val money: String,
    @SerializedName("coin") val coin: BigDecimal = BigDecimal(0),
    @SerializedName("give") val give: Int,
    @SerializedName("is_hot") val isHot: Int,
    @SerializedName("product_id") val productId: String,
) {
    fun haveHot(): Boolean {
        return isHot == 1
    }
}


data class DiamondResultInfo(
    @SerializedName("list") val diamondList: List<DiamondInfo> = ArrayList()
)

data class DiamondInfo(
    @SerializedName("coin") val coin: Double = 0.0,
    @SerializedName("award") val award: Int = 0,
    @SerializedName("diamond") val diamond: Int = 0,

    )

data class RechargeUserListInfo(@SerializedName("list") val userList: List<UserInfo> = ArrayList())

