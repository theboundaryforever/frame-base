package com.yuehai.data.collection.path.mine

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yuehai.data.collection.path.user.UserInfo
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MallType(val type: Int) : Parcelable {
    TYPE_HEAD_WEAR(3),
    TYPE_BUBBLE(4),
    TYPE_CAR(6),
    TYPE_ENTRANCE(7),
    TYPE_VIP(8),

}

//商城类型
@Parcelize
class MallTypeItem(val itemType: MallType, var selected: Boolean = false) : Parcelable

@Parcelize
class MallItemInfo(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("icon")
    val icon: String = "",
    @SerializedName("mp4")
    val mp4: String = "",
    @SerializedName("price")
    val price: String = "",
    @SerializedName("coin_type")
    val coinType: String = "",
    @SerializedName("use_limit")
    val userLimit: Int = 0,
    @SerializedName("use_status")
    var useStatus: Int = 0,
    @SerializedName("time")
    val userTime: Long = 0,
    @SerializedName("discount")
    val discount: Float = 0f,
    @SerializedName("discount_msg")
    val discountMsg: String = "",
    @SerializedName("original_price")
    val originPrice: Float = 0f,
) : Parcelable {

    @IgnoredOnParcel
    var select: Boolean = false

    fun isDressUse(): Boolean {
        return useStatus == 1
    }

}

data class MallUserInfo(
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("coin")
    val coin: Double = 0.0
)

@Parcelize
data class MallCategory(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("list")
    val mallList: List<MallItemInfo> = ArrayList(),
    @SerializedName("icon")
    val icon: String = "",
    @SerializedName("type")
    val mallTabType: Int = 0

) : Parcelable


enum class MallTabType(val type: Int) {
    MALL(1),
    ACTIVITY(2),
    VIP(3)
}

data class MallResultInfo(
    @SerializedName("user_info")
    val userInfo: MallUserInfo,
    @SerializedName("list")
    val categoryList: List<MallCategory> = ArrayList()
)


class MineInfoData(@SerializedName("info") val info: UserInfo)

