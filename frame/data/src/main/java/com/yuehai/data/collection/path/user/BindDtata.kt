package com.yuehai.data.collection.path.user

import com.google.gson.annotations.SerializedName

data class BindPlatInfoResult(

    @SerializedName("list")
    val bindList: List<BindPlatInfo> = ArrayList()
)

data class BindPlatInfo(
    @SerializedName("type")
    val type: Int = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("is_bind")
    val bindState: Int = 0,
    @SerializedName("info")
    val bindInfo: BindInfo? = null
) {
    fun haveBind(): Boolean {
        return bindState == 1
    }
}

data class BindInfo(
    @SerializedName("area_code")
    val areaCode: String = "",
    @SerializedName("mobile")
    val mobile: String = ""

)

enum class BindPlatformType(val type: Int) {
    // 1-手机, 2-谷歌, 3-facebook, 4-apple
    BIND_PLATFORM_PHONE(1),
    BIND_PLATFORM_GOOGLE(2),
    BIND_PLATFORM_FACEBOOK(3),
    BIND_PLATFORM_APPLE(4)
}