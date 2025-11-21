package com.yuehai.data.collection.path.login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yuehai.data.collection.path.user.UserInfo
import kotlinx.parcelize.Parcelize


data class CountryHomeList(
    @SerializedName("list")
    val countryList: List<CountryInfo> = ArrayList()
)

@Parcelize
data class CountryInfo(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("country_code")
    var countryCode: Int = -1,
    @SerializedName("img")
    val img: String = "",
    @SerializedName("zone_id")
    val zoneId: Int = 0,
): Parcelable {
    var select: Boolean = false
}

//国家区码列表
class CountryCodeList(
    @SerializedName("list")
    val codeList: List<CountryCode> = ArrayList()
)

class CountryCode(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("name_en")
    val name: String = "",
    @SerializedName("name_ar")
    val translate: String = "",
    @SerializedName("locale")
    val locale: String = ""
) {
    var select: Boolean = false
}

data class PhoneLoginInfo(
    @SerializedName("user")
    var user: UserInfo
)

class OtherUserInResult(
    @SerializedName("info")
    val info: OtherUserRelation,

    )

class OtherUserRelation(
    @SerializedName("is_follow")
    val isFollow: Int = 0,
    @SerializedName("is_black")
    val isBlack: Int = 0,
    @SerializedName("is_chat")
    val isChat: Int = 0,
    @SerializedName("to_user_info")
    val toUserInfo: OtherUserInfo

) {
    fun canChat(): Boolean {
        return isChat == 1
    }

    fun hasFollow(): Boolean {
        return isFollow == 1
    }

    fun hasBlack(): Boolean {
        return isBlack == 1
    }
}


class OtherUserInfo(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_nickname")
    val userNickname: String = "",
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("age")
    val age: Int = 0,
    @SerializedName("birthday")
    val birthday: String = "",
    @SerializedName("constellation")
    val constellation: String = "",
    @SerializedName("signature")
    val signature: String = "",
    @SerializedName("luck")
    val luck: Int = 0,
    @SerializedName("zone_id")
    val zoneid: Int = 0,
    @SerializedName("guild_id")
    val guildId: Int = 0,
    @SerializedName("guild_zone_id")
    val guildZoneId: Int = 0,
    @SerializedName("headwear_url")
    val headwearUrl: String = "",
    @SerializedName("sex")
    val sex: Int = 0,
    @SerializedName("wealth_level")
    val wealthLevel: Int = 0,
    @SerializedName("wealth_total")
    val wealthTotal: Int = 0,
    @SerializedName("wealth_icon")
    val wealthIcon: String = "",
    @SerializedName("charming_level")
    val charmingLevel: Int = 0,
    @SerializedName("charming_total")
    val charmingTotal: Int = 0,
    @SerializedName("charming_icon")
    val charminIcon: String = "",
    @SerializedName("vip_level")
    val vipLevel: Int = 0,
    @SerializedName("vip_icon")
    val vipIcon: String = "",
    @SerializedName("vip_color")
    val vipColor: String = "",
    @SerializedName("svip_level")
    val svipLevel: Int = 0,
    @SerializedName("svip_icon")
    val svipIcon: String = "",
    @SerializedName("country_info")
    val countryInfo: Country = Country(),
    @SerializedName("img_list")
    val imgList: ArrayList<Photo> = ArrayList<Photo>(),
    @SerializedName("is_follow")
    val isFollow: Int = 0,
    @SerializedName("is_black")
    val isBlack: Int = 0,
    @SerializedName("is_self")
    val isSelf: Int = 0,
) {
    fun hasFollow(): Boolean {
        return isFollow == 1
    }
}

@Parcelize
class Country(
    @SerializedName("name")
    val name: String = "",

    @SerializedName("country_code")
    val country_code: Int = 0,

    @SerializedName("img")
    val img: String = "",

    @SerializedName("zone_id")
    val zone_id: Int = 0
) : Parcelable


@Parcelize
data class Photo(
    @SerializedName("img_id")
    var img_id: Int = 0,
    @SerializedName("img_url")
    var img_url: String = ""
) : Parcelable {

}


enum class SEX(val sex: Int) {
    MALE(1),
    FEMALE(2);

    companion object {
        fun map(sex: Int): SEX? {
            return SEX.entries.firstOrNull { it.sex == sex }
        }
    }
}

