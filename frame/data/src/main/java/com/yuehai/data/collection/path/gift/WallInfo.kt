package com.yuehai.data.collection.path.gift

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GiftCategory(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("type")
    val type: Int = 0,
    @SerializedName("icon")
    val icon: String = "",
    @SerializedName("total")
    val totalGiftCount: Int = 0,
    @SerializedName("list")
    val wallList: List<WallGiftItem> = ArrayList()
) : Parcelable {
    var itemSelect: Boolean = false
}

enum class WallType(val type: Int) {
    WALL_GIFT(0),
    WALL_HEAD_WEAR(3),
    WALL_RIDE(6)

}

@Parcelize
data class WallGiftItem(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("icon")
    val icon: String = "",
    @SerializedName("mp4")
    val mp4: String = "",
    @SerializedName("count")
    val count: Int = 0
) : Parcelable {
    var itemType: Int = 0
}
