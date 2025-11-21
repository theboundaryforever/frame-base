package com.yuehai.data.collection.path.room

data class RoomMember(val id: Int)

enum class RoomOnLineType(val type: Int) {
    ROOM_ONLINE_OTHER(0),
    ROOM_ONLINE_AT_SING(1),

}