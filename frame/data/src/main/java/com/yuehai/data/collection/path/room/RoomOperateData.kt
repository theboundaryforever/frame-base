package com.yuehai.data.collection.path.room

//房间 底部操作
enum class RoomSetOperate(val type: Int) {
    //音量开关
    OPERATE_TYPE_VOLUME_OPEN(1),
    OPERATE_TYPE_VOLUME_CLOSE(2),

    //音乐设置
    OPERATE_TYPE_MUSIC(3),

    //魅力值开关
    OPERATE_TYPE_GIFT_VALUE(4),

    //魅力值开关房间设置
    OPERATE_TYPE_ROOM_SET(5),

    //座位风格设置
    OPERATE_TYPE_SEAT_STYLE(6),

    //游戏特效设置
    OPERATE_TYPE_EFFECT_SET(7),

    //房间举报
    OPERATE_TYPE_ROOM_REPORT(8),

    //房间背景
    OPERATE_TYPE_ROOM_BACKGROUND(9),
    //房间福袋
    OPERATE_TYPE_ROOM_INTERACTION(10),

}


data class RoomOperateFunctionInfo(val operateItem: RoomSetOperate) : RoomOperateDiffUtil {
    override fun areItemsTheSame(newItem: RoomOperateDiffUtil): Boolean {
        return newItem is RoomOperateFunctionInfo

    }

    override fun areContentsTheSame(newItem: RoomOperateDiffUtil): Boolean {
        if (newItem !is RoomOperateFunctionInfo) {
            return false
        }
        return this.operateItem == newItem.operateItem
    }

}

data class RoomOperateFunctionTitleInfo(val title: String) : RoomOperateDiffUtil {
    override fun areItemsTheSame(newItem: RoomOperateDiffUtil): Boolean {
        return newItem == this
    }

    override fun areContentsTheSame(newItem: RoomOperateDiffUtil): Boolean {
        if (newItem !is RoomOperateFunctionTitleInfo) {
            return false
        }
        return this.title == newItem.title
    }

}



