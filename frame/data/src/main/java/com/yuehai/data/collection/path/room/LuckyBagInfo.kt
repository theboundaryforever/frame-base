package com.yuehai.data.collection.path.room

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class LuckyBagInfo(
    @SerializedName("coin")
    val coinOptions: CoinOptions?,
    @SerializedName("gift")
    val giftOptions: GiftOptions?,
    @SerializedName("wallet")
    val wallet: WalletInfo? = null,
    @SerializedName("ts")
    val timestamp: Long,
    @SerializedName("validity")
    val validity: List<ValidityTimeInfo> = ArrayList()
)

// --- 币值 (Coin) 结构 ---
data class CoinOptions(
    @SerializedName("world")
    val worldCoin: CoinListConfig?,
    @SerializedName("room")
    val roomCoin: CoinListConfig?
)

data class CoinListConfig(
    @SerializedName("coin_list")
    val coinList: List<CoinInfo> = ArrayList(),
    @SerializedName("num_list")
    val numList: List<Int> = ArrayList()
)

data class CoinInfo(
    @SerializedName("coin")
    val coin: Int,
    @SerializedName("img")
    val coinImg: String
) {
    var itemSelected: Boolean = false
}

data class GiftOptions(
    @SerializedName("world")
    val worldGifts: List<GiftOptionItem> = ArrayList(),
    @SerializedName("room")
    val roomGifts: List<GiftOptionItem> = ArrayList()
)

data class GiftOptionItem(
    @SerializedName("option_id")
    val optionId: Int = 0,
    @SerializedName("option_list")
    val optionList: GiftListDetail?
) {
    var giftItemSelected: Boolean = false
}

data class GiftListDetail(
    @SerializedName("gift_list")
    val giftList: List<GiftItem> = ArrayList(),
    @SerializedName("total")
    val totalCount: Int = 0,
    @SerializedName("total_coin")
    val totalCoin: Int = 0
)

data class GiftItem(
    @SerializedName("coin")
    val coinValue: Int = 0,
    @SerializedName("img")
    val imageUrl: String = "",
    @SerializedName("num")
    val number: Int = 0
)

// --- 钱包 (Wallet) 结构 ---
data class WalletInfo(
    @SerializedName("coin")
    val coinBalance: Double = 0.0,
    @SerializedName("diamond")
    val diamondBalance: Double = 0.0
)

@Parcelize
data class ValidityTimeInfo(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("value")
    val value: Long = 0,


    ) : Parcelable

enum class LuckyBagRang(val type: Int) {
    LUCKY_BAG_RANGE_WORLD(1),
    LUCKY_BAG_RANGE_ROOM(2)
}

enum class LuckyBagType(val type: Int) {
    LUCKY_BAG_COINS(1),
    LUCKY_BAG_TYPE(2)
}

enum class LuckyBagParticipation(val type: Int) {
    AVAILABLE_TO_EVERYONE(1),
    FOLLOW_THE_ROOM_OWNER_AND_RECEIVE(2)
}

enum class LuckyBagReceiveStatus(val type: Int) {
    LUCKY_BAG_STATUS_NOT_RECEIVED(1),
    LUCKY_BAG_STATUS_COMPLETE_RECEIVED(2),
    LUCKY_BAG_STATUS_EXPIRED_RECEIVED(3)
}

data class LuckyBagRewardResult(
    @SerializedName("info")
    val info: LuckyBagRewardInfo? = null
)

/**
 * 信息对象
 */
data class LuckyBagRewardInfo(
    @SerializedName("type")
    val type: Int = 0, // 默认值为 0
    @SerializedName("status")
    val status: Int = 0, // 默认值为 0
    @SerializedName("uid")
    val uid: Long = 0L, // 默认值为 0L (使用 Long 以防 UID 过大)
    @SerializedName("avatar")
    val avatar: String = "", // 默认值为 ""
    @SerializedName("nickname")
    val nickname: String = "", // 默认值为 ""
    @SerializedName("is_receive")
    val isReceive: Int = 0, // 默认值为 0
    @SerializedName("coin")
    val coin: Int = 0, // 默认值为 0
    @SerializedName("img")
    val img: String = "", // 默认值为 ""
    @SerializedName("num")
    val num: Int = 0, // 默认值为 0
    @SerializedName("received_num")
    val receivedNum: Int = 0, // 默认值为 0
    @SerializedName("receive_list")
    val receiveList: List<LuckyBagReceiveItem> = emptyList(), // 默认值为空列表
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("lucky_bag_id")
    val luckyBagId: Int = 0

) {
    fun haveGetLuckBag(): Boolean {
        return isReceive == 1
    }
}


/**
 * 领取列表项对象
 */
data class LuckyBagReceiveItem(
    @SerializedName("uid")
    val uid: Long = 0L, // 默认值为 0L
    @SerializedName("avatar")
    val avatar: String = "", // 默认值为 ""
    @SerializedName("nickname")
    val nickname: String = "", // 默认值为 ""
    @SerializedName("coin")
    val coin: Int = 0, // 默认值为 0
    @SerializedName("img")
    val img: String = "" // 默认值为 ""
) {
    var luckyBest: Boolean = false
}


fun List<LuckyBagReceiveItem>.setBestLuckyCoinStatus() {

    val maxCoin = this.maxOfOrNull { it.coin }

    if (maxCoin != null) {
        this.forEach { item ->

            item.luckyBest = (item.coin == maxCoin)
        }
    }
}


data class LuckyBagRecordResult(
    @SerializedName("list")
    val list: List<LuckyBagRecordInfo> = emptyList() // 列表，默认值为空列表
)


data class LuckyBagRecordInfo(
    @SerializedName("type")
    val type: Int = 0, // 类型
    @SerializedName("id")
    val id: Int = 0, // ID (使用 Long 以防值较大)
    @SerializedName("uid")
    val uid: Long = 0L, // 用户ID
    @SerializedName("nickname")
    val nickname: String = "", // 昵称
    @SerializedName("create_time")
    val createTime: Long = 0L, // 创建时间 (时间戳，使用 Long)
    @SerializedName("coin")
    val coin: Int = 0, // 金币数量
    @SerializedName("img")
    val img: String = "" // 图片URL
)
