package com.yuehai.data.collection.path.room

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.IgnoredOnParcel

enum class RoomBackgroundType(val type: Int) {
    ROOM_BACKGROUND_TYPE_MINE(1), ROOM_BACKGROUND_TYPE_MALL(2), ROOM_BACKGROUND_TYPE_FREE(3)
}


data class BackgroundResult(
    val list: List<BackgroundItemInfo> = ArrayList()
)

@Parcelize
data class BackgroundItemInfo(
    @SerializedName("id")
    val bgId: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("icon")
    val icon: String = "",
    @SerializedName("mp4")
    val mp4: String = "",
    @SerializedName("price")
    val price: Int = 0,
    @SerializedName("remaining_time")
    val remainintTime: Long = 0L,

    @SerializedName("coin_type")
    val coinType: String = "",

    @SerializedName("use_limit")
    val useLimit: Long = 0,

    @SerializedName("shop_type")
    val shopType: Int = 0,

    @SerializedName("is_use")
    var isUse: Int = 0,

    @SerializedName("is_bug")
    val isBuy: Int = 0
) : Parcelable {
    @IgnoredOnParcel
    var itemSelected: Boolean = false
    fun haveBuy(): Boolean {
        return isBuy == 1
    }

    fun haveUse(): Boolean {
        return isUse == 1
    }

    fun haveLimit(): Boolean {
        return useLimit == 1L
    }
}

enum class RoomBackgroundItemType(val type: Int) {
    ROOM_BACKGROUND_TYPE_PAY(1),
    ROOM_BACKGROUND_TYPE_ACTIVITY(2),
    ROOM_BACKGROUND_TYPE_EXCLUSIVE(3),
    ROOM_BACKGROUND_TYPE_FREE(6)

}

data class PurchaseBackgroundInfo(
    @SerializedName("info") val purchaseInfo: PurchaseBackgroundTimeInfo? = null
)

data class PurchaseBackgroundTimeInfo(
    @SerializedName("remaining_time") val remainTime: Long = 0
)