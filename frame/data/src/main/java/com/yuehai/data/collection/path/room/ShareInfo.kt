package com.yuehai.data.collection.path.room

import com.yuehai.data.collection.path.user.UserInfo

data class FansAndFollowResult(
    val list: List<UserInfo> = ArrayList(),
    val count: Int = 0
)

enum class ShareType(val type:Int){
    SHARE_ROOM_FANS(1),
    SHARE_ROOM_FOLLOW(2)
}