package com.yuehai.data.collection.path.room

enum class RoomState {
    ROOM_LEAVE,
    ROOM_JOINING,
    ROOM_IN, //join room成功
    ROOM_LEAVING,
    CHANNEL_IN,
    CHANNEL_LEAVE
}

enum class ChannelState {
    CHANNEL_IN,
    CHANNEL_LEAVE
}

class FlowStateInfo(var roomId: Int = 0) {
    var from: String = ""
    var reason: String = ""
    var leaveUI = false
    var isRoomOwner = false
    var token: String? = null

    override fun toString(): String {
        return "FlowStateInfo(roomId=$roomId, from='$from', reason='$reason', onlineInfo=, leaveUI=$leaveUI, isRoomOwner=$isRoomOwner, token=$token)"
    }
}