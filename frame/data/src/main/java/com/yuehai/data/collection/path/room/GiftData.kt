package com.yuehai.data.collection.path.room

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yuehai.data.collection.path.user.GiftInfo
import com.yuehai.data.collection.path.user.UserInfo
import kotlinx.parcelize.Parcelize

data class GiftResultList(
    @SerializedName("list")
    val giftTypeList: ArrayList<GiftTypeInfo> = ArrayList(),
    @SerializedName("weekly_star")
    var weeklyStar: WeeklyStarInfo? = null
)

@Parcelize
class GiftTypeInfo(
    @SerializedName("title")
    val title: String = "",
    @SerializedName("is_bag")
    val isBag: Int = 0,
    @SerializedName("list")
    val giftInfoList: ArrayList<GiftItemInfo> = ArrayList(),

) : Parcelable {
    fun giftBag(): Boolean {
        return isBag == 1
    }
}

@Parcelize
data class GiftItemInfo(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("price")
    val price: Int = 0,

    @SerializedName("currency_type")
    val currencyType: Int = 0,

    @SerializedName("img")
    val img: String = "",

    @SerializedName("svga")
    val svga: String = "",

    @SerializedName("mp4")
    val mp4: String = "",

    @SerializedName("is_luck")
    val showLuck: Int = 0,//1显示; 0不显示;

    @SerializedName("is_music")
    val showMusic: Int = 0, //1显示; 0不显示;

    @SerializedName("is_star")
    val showStar: Int = 0,//1显示; 0不显示;

    @SerializedName("luck_rate_introduce")
    val luckRateIntroduce: String = "",
    @SerializedName("luck_rate_param")
    val luckRateParam: String = "",
    @SerializedName("luck_rate")
    val luckRate: Int = 0,

    @SerializedName("gift_num")
    var giftNum: Int = 0,
    var selected: Boolean = false
) : Parcelable {


    fun showLuckIcon(): Boolean {
        return showLuck == 1
    }

    fun showMusicIcon(): Boolean {
        return showMusic == 1
    }

    fun showStarIcon(): Boolean {
        return showStar == 1
    }
}

data class NoticeDataInfo(
    val sendUser: UserInfo,
    val toUserInfoList: List<UserInfo> = ArrayList(),
    val giftInfo: GiftInfo,
    val allMic: Boolean = false
) {
    fun comboTag(): String {
        return "${sendUser.uid}-${toUserInfoList.firstOrNull()?.uid}-${giftInfo.id}"
    }
}


/**
 * 送礼参数
 */
data class SendGiftParam(
    val uidList: Set<Int>,
    val giftId: Int,
    val giftCount: Int,
    val isBag: Int
)

@Parcelize
data class WeeklyStarInfo(
    @SerializedName("uid")
    val uid: Int = 0,
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("url")
    val url: String = "",

    ) : Parcelable

