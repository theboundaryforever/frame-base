package com.yuehai.data.collection.path.home

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class BannerActivityResult(
    @SerializedName("list")
    val bannerActivityList: List<BannerActivityInfo> = ArrayList(),
    @SerializedName("middle_list")
    val middleBannerActivityList: List<BannerActivityInfo> = ArrayList()
)

@Parcelize
data class BannerActivityInfo(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("image")
    val img: String = "",
    @SerializedName("url")
    val url: String = ""
) : Parcelable {

}