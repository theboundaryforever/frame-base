package com.yuehai.data.collection.path.room

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.yuehai.data.collection.path.json.toJsonErrorNull
import com.yuehai.data.collection.path.user.GiftInfo
import com.yuehai.data.collection.path.user.LuckyVoiceInfo
import com.yuehai.data.collection.path.user.UserInfo
import com.yuehai.data.collection.path.user.VoiceInfo
import kotlinx.parcelize.Parcelize


enum class MessageType(val types: String, val clazz: Class<*>?) {
    WELCOME("welcome", IMChatWelcomeInfo::class.java),
    NOTICE("notice", IMChatNoticeInfo::class.java),
    TEXT("text", IMChatMessageInfo::class.java),
    GIFT("gift", IMChatGiftMessageInfo::class.java),
    MANAGER("manager", IMChatManagerMessageInfo::class.java),
    LUCKY_GIFT("lucky_gift", IMChatLuckyGiftMessageInfo::class.java),
    GAME("game", IMGameGlobalMessageInfo::class.java),
    LUCKY_BAG_COINS("lucky_bag_coins", IMChatGlobalLucyCoinsInfo::class.java),

    UNKNOWN("unknown", String::class.java);


    companion object {

        fun getMessageType(types: String): MessageType {
            for (entry in MessageType.entries) {
                if (entry.types.equals(types, ignoreCase = true)) {
                    return entry
                }
            }
            return UNKNOWN
        }

        fun getMessageTypeClass(type: String): Class<*>? {
            return getMessageType(type).clazz
        }
    }

}

class Message(var types: MessageType? = null) : ILiveMessage {
    var content: Content? = null

    companion object {

        fun crateMessage(type: MessageType, content: Content): Message {
            return Message(type).apply {
                this.content = content
            }
        }

    }

    var index = -1L
    override fun index(): Long {
        return index
    }


}

infix fun Message.contentsTheSame(other: Message): Boolean {
    //消息体是否相同
    val isMessageSame =
        types == other.types && toJsonErrorNull(content) == toJsonErrorNull(other.content)
    //消息内容是否相同
    return content?.areContentsTheSame(other.content) ?: true
}

abstract class Content(val types: MessageType) {
    var isAristocrat: Boolean = false

    abstract fun isValid(): Boolean
    abstract fun areItemsTheSame(newItem: Content): Boolean

    abstract fun areContentsTheSame(newContent: Content?): Boolean
    override fun toString(): String {
        return "Content(type=$types)"
    }

}


//发送房间聊天消息
data class IMChatMessageInfo(
    @SerializedName("user")
    val user: UserInfo?,
    @SerializedName("content")
    val content: String
) : Content(MessageType.TEXT) {


    override fun isValid(): Boolean {
        return content.isEmpty()
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatMessageInfo) {
            return false
        }
        return this.content == newContent.content &&
                this.user?.id == newContent.user?.id &&
                this.types == newContent.types &&
                this.user?.sex == newContent.user?.sex &&
                this.user?.avatar == newContent.user?.avatar &&
                this.user?.countryInfo?.country_code == newContent.user?.countryInfo?.country_code
    }

}

//房间欢迎聊天消息
data class IMChatWelcomeInfo(
    @SerializedName("content")
    val content: String
) : Content(MessageType.WELCOME) {
    override fun isValid(): Boolean {
        return content.isEmpty()
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatMessageInfo) {
            return false
        }
        return this.content == newContent.content &&
                this.types == newContent.types
    }

}

//房间该同志聊天消息
data class IMChatNoticeInfo(
    @SerializedName("content")
    val content: String
) : Content(MessageType.NOTICE) {
    override fun isValid(): Boolean {
        return content.isEmpty()
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatMessageInfo) {
            return false
        }
        return this.content == newContent.content &&
                this.types == newContent.types
    }

}

//送礼时房间麦位人数类型
enum class RECEIVER_TYPE(val type: Int) {
    SINGLE_USER(1),
    MULTI_USER(2),
    ALL_USER(3)
}

//发送房间礼物聊天消息
data class IMChatGiftMessageInfo(
    @SerializedName("user_info")
    val fromUser: UserInfo?,
    @SerializedName("to_user_info")
    val toUserInfoList: List<UserInfo> = ArrayList(),
    @SerializedName("voice_info")
    val voiceInfo: VoiceInfo,
    @SerializedName("gift_info")
    val giftInfo: GiftInfo,
    @SerializedName("receiver_type")
    val receiverType: Int = RECEIVER_TYPE.SINGLE_USER.type,
    @SerializedName("rocket_info")
    val rocketInfo: RocketProgressInfo

) : Content(MessageType.GIFT) {
    override fun isValid(): Boolean {
        return toUserInfoList.isEmpty() == true
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatGiftMessageInfo) {
            return false
        }
        return this.fromUser?.id == newContent.fromUser?.id &&
                this.toUserInfoList.size == newContent.toUserInfoList.size &&
                voiceInfo.id == newContent.voiceInfo.id &&
                this.giftInfo.id == newContent.giftInfo.id &&
                this.rocketInfo.percent == newContent.rocketInfo.percent &&
                this.rocketInfo.validityTime == newContent.rocketInfo.validityTime &&
                this.rocketInfo.level == newContent.rocketInfo.level
    }

}


data class IMChatManagerMessageInfo(
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("nickname")
    val nickName: String = ""

) : Content(MessageType.MANAGER) {
    override fun isValid(): Boolean {
        return userId != 0
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatManagerMessageInfo) {
            return false
        }
        return this.userId == newContent.userId && this.voiceUid == newContent.userId
    }

}

//发送房间礼物聊天消息
data class IMChatLuckyGiftMessageInfo(
    @SerializedName("user_info")
    val fromUser: UserInfo?,
    @SerializedName("voice_info")
    val voiceInfo: LuckyVoiceInfo,
    @SerializedName("gift_info")
    val giftInfo: GiftInfo,

    ) : Content(MessageType.LUCKY_GIFT) {
    override fun isValid(): Boolean {
        return fromUser?.id == 0
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatLuckyGiftMessageInfo) {
            return false
        }
        return this.fromUser?.id == newContent.fromUser?.id &&
                voiceInfo.voiceId == newContent.voiceInfo.voiceId &&
                this.giftInfo.id == newContent.giftInfo.id &&
                this.giftInfo.img == newContent.giftInfo.img &&
                this.giftInfo.name == newContent.giftInfo.name &&
                this.fromUser?.sex == newContent.fromUser?.sex
    }

}

//发送房间全服消息
data class IMChatGlobalGiftMessageInfo(
    @SerializedName("user_info")
    val fromUser: UserInfo?,
    @SerializedName("to_user_info")
    val toUserInfo: UserInfo?,
    @SerializedName("gift_info")
    val giftInfo: GiftInfo,

    ) : Content(MessageType.LUCKY_GIFT) {
    override fun isValid(): Boolean {
        return fromUser?.id == 0
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatGlobalGiftMessageInfo) {
            return false
        }
        return this.fromUser?.id == newContent.fromUser?.id &&
                this.toUserInfo?.id == newContent.toUserInfo?.id &&
                this.giftInfo.id == newContent.giftInfo.id &&
                this.fromUser?.sex == newContent.fromUser?.sex &&
                this.fromUser?.avatar == newContent.fromUser?.avatar &&
                this.fromUser?.countryInfo?.country_code == newContent.fromUser?.countryInfo?.country_code &&
                this.toUserInfo?.sex == newContent.toUserInfo?.sex &&
                this.toUserInfo?.luck == newContent.toUserInfo?.luck &&
                this.toUserInfo?.countryInfo?.country_code == newContent.toUserInfo?.countryInfo?.country_code
    }

}


//发送房间全服消息
data class IMGameGlobalMessageInfo(
    @SerializedName("game_id")
    val gameId: Int = 0,
    @SerializedName("game_name")
    val gameName: String = "",
    @SerializedName("game_icon")
    val gameIcon: String = "",
    @SerializedName("game_link")
    val gameLink: String = "",
    @SerializedName("uid")
    val uid: Int = 0,
    @SerializedName("nickname")
    val nickname: String = "",
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("reward")
    val reward: Int = 0,
    @SerializedName("browser_width")
    val browserWidth: Int = 1,
    @SerializedName("browser_height")
    val browserHeight: Int = 1,

    ) : Content(MessageType.GAME) {
    override fun isValid(): Boolean {
        return uid == 0
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMGameGlobalMessageInfo) {
            return false
        }
        return this.uid == newContent.uid &&
                this.gameId == newContent.gameId &&
                this.gameName == newContent.gameName &&
                this.gameIcon == newContent.gameIcon &&
                this.gameLink == newContent.gameLink &&
                this.nickname == newContent.nickname &&
                this.avatar == newContent.avatar &&
                this.voiceUid == newContent.voiceUid &&
                this.reward == newContent.reward
    }

}

enum class ChatMessageType(val type: Int) {
    CHAT_MESSAGE_ALL(1),
    CHAT_MESSAGE_CHAT(2),
    CHAT_MESSAGE_SYSTEM(3)
}

data class IMChatGlobalLuckyBagInfo(
    @SerializedName("user_info")
    val fromUser: UserInfo? = null,
    @SerializedName("lucky_bag_info")
    val luckBagInfo: IMChatGlobalLucyCoinsInfo? = null

) : Content(MessageType.LUCKY_BAG_COINS) {
    override fun isValid(): Boolean {
        return fromUser?.uid == 0
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatGlobalLuckyBagInfo) {
            return false
        }
        return fromUser?.uid == newContent.fromUser?.uid && fromUser?.nickname == newContent.fromUser?.nickname &&
                fromUser?.avatar == newContent.fromUser?.avatar && luckBagInfo?.luckyBagType == newContent.luckBagInfo?.luckyBagType &&
                luckBagInfo?.voiceUid == newContent.luckBagInfo?.voiceUid && luckBagInfo?.luckBagId == newContent.luckBagInfo?.luckBagId
    }

}

//发送房间福袋金币全服消息
@Parcelize
data class IMChatGlobalLucyCoinsInfo(
    @SerializedName("type")
    var luckyBagType: Int = 0,
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("lucky_bag_id")
    var luckBagId: Int = 0,
    @SerializedName("validity_time")
    val validityTime: Long = 0
) : Parcelable


data class IMChatRocketInfo(
    @SerializedName("info")
    val rocketInfo: RocketInfo? = null,


    ) : Content(MessageType.LUCKY_BAG_COINS) {
    override fun isValid(): Boolean {
        return rocketInfo == null
    }

    override fun areItemsTheSame(newItem: Content): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newContent: Content?): Boolean {
        if (newContent !is IMChatRocketInfo) {
            return false
        }
        return rocketInfo?.voiceUid == newContent.rocketInfo?.voiceUid &&
                rocketInfo?.nickname == newContent.rocketInfo?.nickname
    }

}

data class RocketInfo(
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("nickname")
    val nickname: String = "",
    @SerializedName("level")
    val level: Int = 0,
    @SerializedName("validity_time")
    val validityTime: Long = 0L,
    @SerializedName("first_uid")
    val firstUid: Int = 0,
    @SerializedName("first_avatar")
    val firstAvatar: String = "",
    @SerializedName("boom_uid")
    val boomUid: Int = 0,
    @SerializedName("boom_avatar")
    val boomAvatar: String = "",
) {}










