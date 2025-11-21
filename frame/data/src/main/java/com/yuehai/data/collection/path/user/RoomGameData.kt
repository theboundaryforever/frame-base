package com.yuehai.data.collection.path.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


//房间游戏中心第三方游戏
data class GameCenterInfo(
    @SerializedName("game_list")
    val gameList: List<GameInfo> = ArrayList(),
    @SerializedName("msg_list")
    val msgList: List<GameMsg> = ArrayList()
)

@Parcelize
data class GameInfo(
    @SerializedName("game_id")
    val gameId: Int = 0,
    @SerializedName("game_name")
    val gameName: String = "",
    @SerializedName("game_link")
    var gameLink: String = "",
    @SerializedName("game_type")
    val gameType: Int = 0,
    @SerializedName("game_icon")
    val gameIcon: String = "",
    @SerializedName("browser_width")
    val browserWidth: Int = 10,
    @SerializedName("browser_height")
    val browserHeight: Int = 14
) : Parcelable

data class GameMsg(
    val uid: Long = 0,
    val nickname: String = "",
    @SerializedName("game_id")
    val gameId: Int = 0,
    @SerializedName("game_name")
    val gameName: String = "",
    val reward: Int = 0
)

data class RoomDataInfo(
    @SerializedName("total")
    val total: Double?,
    @SerializedName("today")
    val today: Double?,
    @SerializedName("yesterday")
    val yesterday: Double?,
    @SerializedName("this_week")
    val thisWeek: Double?,
    @SerializedName("last_week")
    val lastWeek: Double?,
    @SerializedName("this_month")
    val thisMonth: Double?,
    @SerializedName("last_month")
    val lastMonth: Double?
)

data class RoomCardInfo(
    @SerializedName("voice_info")
    val voiceInfo: VoiceInfo? = null,
    @SerializedName("user_info")
    val userInfo: UserInfo? = null,
    @SerializedName("room_data")
    val roomDataInfo: RoomDataInfo? = null,
    @SerializedName("is_follow")
    var isFollow: Int = 0,
    @SerializedName("is_owner")
    val isOwner: Int = 0,
    @SerializedName("is_admin")
    val isAdmin: Int = 0
) {
    fun haveFollow(): Boolean {
        return isFollow == 1
    }

    fun isRoomOwner(): Boolean {
        return isOwner == 1
    }

    fun isRoomAdmin(): Boolean {
        return isAdmin == 1
    }

    fun updateFollow(follow: Boolean) {
        isFollow = if (follow) {
            1
        } else {
            0
        }
    }
}

enum class RoomTimeType(val type: Int) {
    TIME_TYPE_TODAY(1),
    TIME_TYPE_YESTERDAY(2),
    TIME_TYPE_THIS_WEEK(3),
    TIME_TYPE_LAST_WEEK(4),
    TIME_TYPE_THIS_MONTH(5),
    TIME_TYPE_LAST_MONTH(6),
}

data class RoomTimeItemCoin(val type: RoomTimeType, val coin: Double = 0.0)