package com.yuehai.data.collection.path.room

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BlockUserInfo(
    @SerializedName("block_type_id")
    val blockTypeId: Int = 0,
    var status: Int = 0,
    @SerializedName("is_permission")
    val isPermission: Int = 0
) : Parcelable {
    fun haveBlock(): Boolean {
        return status == 1
    }

    fun havePermission(): Boolean {
        return isPermission == 1
    }
}


data class BlockResultInfo(
    @SerializedName("list")
    val blockList: ArrayList<BlockUserInfo> = ArrayList()
)

enum class UserPermissionFrom(val from: Int) {
    FROM_ROOM(0),
   FROM_USER(1),
}