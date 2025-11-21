package com.yuehai.data.collection.path.global

import com.google.gson.annotations.SerializedName

data class GlobalConfig(
    @SerializedName("group_id")
    val groupID: String = "",
    @SerializedName("is_check_version")
    val checkVersion: Int = 0
) {
    fun showAppCheck(): Boolean {
        return checkVersion == 1
    }
}