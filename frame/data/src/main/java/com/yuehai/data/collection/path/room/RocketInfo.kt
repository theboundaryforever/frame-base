package com.yuehai.data.collection.path.room

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class RocketResultConfig(
    @SerializedName("current_level")
    val currentLevel: Int = 0,
    @SerializedName("current_score")
    val currentScore: Double = 0.0,
    @SerializedName("list")
    val rocketLevelList: List<RocketLevel> = ArrayList(),
    @SerializedName("small_config")
    val smallConfig: List<SmallConfig> = ArrayList()
)

data class RocketLevel(
    @SerializedName("level")
    val level: Int = 0,
    @SerializedName("min")
    val min: Int = 0,
    @SerializedName("max")
    val max: Int = 0,
    @SerializedName("rocket_img")
    val rocketImg: String = "",
    @SerializedName("box_img")
    val boxImg: String = "",
    @SerializedName("reward_list")
    val rewardList: List<RewardItem> = ArrayList(),
    @SerializedName("status")
    var status: Int = 0,
    @SerializedName("is_receive")
    val isReceive: Int = 0,
    @SerializedName("receive_info")
    var receiveInfo: List<RewardItem> = ArrayList(),
    @SerializedName("box_id")
    val boxId: Int = 0,
    @SerializedName("count_down")
    val countDown: Int = 0,
    @SerializedName("user_list")
    val userList: List<UserContribution> = ArrayList(),
    @SerializedName("percent")
    val percent: Float = 0f
) {
    fun getRocketLevelProgress(): Float {
        return percent * 100
    }

}

// --- reward_list 元素 (Reward Item) 实体 ---
data class RewardItem(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("img")
    val img: String = "",
    @SerializedName("coin")
    val coin: Double = 0.0,
    @SerializedName("day")
    val day: Int = 0,
    @SerializedName("type")
    val type: Int = 0
)

// --- user_list 元素 (User Contribution) 实体 ---
data class UserContribution(
    @SerializedName("uid")
    val uid: Int = 0,
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("coin")
    val coin: Double = 0.0
)

data class SmallConfig(
    @SerializedName("level")
    val level: Int = 0,
    @SerializedName("img")
    val img: String = ""
) {
    var levelSelect: Boolean = false
}

data class BoxReceiveInfo(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("img")
    val img: String = "",
    @SerializedName("coin")
    val coin: Double = 0.0,
    @SerializedName("day")
    val day: Int = 0,
)

data class BoxRewardInfo(
    @SerializedName("info")
    val rewardItem: List<RewardItem> = emptyList()
)

enum class RocketStatus(val state: Int) {
    ROCKET_STATE_IN_PROGRESS(1),//进行中
    ROCKET_STATE_UNCLAIMED(2),//未领取
    ROCKET_STATE_CHEST_UNCLAIMED(3),//已领取
    ROCKET_STATE_ENDED(4)//已结束

}

enum class RocketBoxType(val type: Int) {
    ROCKET_BOX_TYP_GIFT(1),
    ROCKET_BOX_TYP_CAR(2),
    ROCKET_BOX_TYP_HEADER(3),
    ROCKET_BOX_TYP_COIN(4),
}

data class RocketProgressInfo(
    @SerializedName("switch")
    val switch: Int = 0,
    @SerializedName("level")
    val level: Int = 0,
    @SerializedName("percent")
    val percent: Float = 0f,
    @SerializedName("validity_time")
    val validityTime: Long = 0L,
    @SerializedName("rocket_launch")
    val levelList: List<SmallConfig> = emptyList()

) {
    fun getRocketSwitchOpen(): Boolean {
        return switch == 1
    }
}

data class RocketProgressDetailInfo(
    @SerializedName("info")
    val info: RocketProgressInfo? = null
)

data class RecordResult(
    @SerializedName("list")
    val list: List<RocketRecordItem> = emptyList()
)

data class RocketRecordItem(

    @SerializedName("time")
    val time: Long = 0,

    @SerializedName("level")
    val level: Int = 0,

    @SerializedName("score")
    val score: Double = 0.0,

    @SerializedName("img")
    val img: String = ""
)


