package com.yuehai.data.collection.path.room

import com.google.gson.annotations.SerializedName

//创建房间主题
data class RoomThemeInfo(
    @SerializedName("theme_list")
    val themeList: List<ThemeItemInfo> = ArrayList()
)

data class ThemeItemInfo(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("icon")
    val icon: String = "",
    @SerializedName("intro_list")
    val introList: List<String> = ArrayList()
){
    var itemSelect: Boolean = false
}

data class ThemeDetailInfo(
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("theme_id")
    val themeId: Int = 0,
    @SerializedName("theme_name")
    val themeName: String = "",
    @SerializedName("theme_icon")
    val themeIcon: String="",
    @SerializedName("theme_bg")
    val themeBg: String="",
    @SerializedName("theme_intro")
    val themeIntro: String="",
)

