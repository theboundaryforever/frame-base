package com.yuehai.data.collection.path.user

import android.annotation.SuppressLint
import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.google.gson.annotations.SerializedName
import com.yuehai.data.collection.path.Constants
import com.yuehai.data.collection.path.gift.GiftCategory
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.login.Country
import com.yuehai.data.collection.path.login.CountryInfo
import com.yuehai.data.collection.path.login.Photo
import com.yuehai.data.collection.path.room.IMChatGlobalLucyCoinsInfo
import com.yuehai.data.collection.path.user.RoomMicMode.Companion.getMicModeByMode
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

class SeatListItemDiffUtil : DiffUtil.ItemCallback<SeatListData>() {

    override fun areItemsTheSame(oldItem: SeatListData, newItem: SeatListData): Boolean {
        return oldItem.areItemsTheSame(newItem)
    }

    override fun areContentsTheSame(oldItem: SeatListData, newItem: SeatListData): Boolean {
        return oldItem.areContentsTheSame(newItem)
    }

}

interface SeatListData {
    fun areItemsTheSame(newItem: SeatListData): Boolean
    fun areContentsTheSame(newItem: SeatListData): Boolean
}


@Parcelize
data class EnterRoomInfo(
    val channelId: Int,
    var password: String? = null,
) : Parcelable

/**
 * 进房结果
 */
data class JoinRoomReq(
    val roomId: Int,
    val roomUserId: Int,
    val password: String? = null,
    val voiceId: Int = 0,
    val reconnectJoinRoom: Boolean = false
)

data class LeaveRoomReq(
    @SerializedName("roomId") val roomId: Int
)

@Parcelize
data class RoomInfo(
    @SerializedName("user")
    val user: UserInfo,
    @SerializedName("voice")
    var voice: VoiceInfo,
    @SerializedName("lucky_bag_info")
    val luckyRoomBagInfo: IMChatGlobalLucyCoinsInfo? = null,
) : Parcelable


@Parcelize
data class UserInfo(
    @SerializedName("id")
    var id: Int = 0, // 用户id
    @SerializedName("nickname")
    val nickname: String = "",// 用户昵称
    @SerializedName("user_nickname")
    var userNickname: String = "",
    @SerializedName("token")
    var token: String = "", //token
    @SerializedName("user_sign")
    val userSign: String = "", //腾讯IM user sign
    @SerializedName("avatar")
    var avatar: String = "", // 用户头像url
    @SerializedName("sex")
    val sex: Int = 0,  // 用户性别: 1男, 2女
    @SerializedName("luck")
    val luck: Int = 0, // 用户靓号
    @SerializedName("zone_id")
    val zoneId: Int = 0, //用户地区id
    @SerializedName("guild_id")
    val guildId: Int = 0,// 用户公会id
    @SerializedName("guild_zone_id")
    val guildZoneId: Int = 0, //用户公会地区id
    @SerializedName("coin")
    val coin: Double = 0.00,//用户金
    @SerializedName("diamond")
    val diamond: Double = 0.0,//用户钻石
    @SerializedName("wealth_level")
    val wealthLevel: Int = 0, //用户财富等级
    @SerializedName("wealth_total")
    val wealthTotal: Int = 0, //用户财富值
    @SerializedName("wealth_icon")
    val wealthIcon: String = "", //用户财富等级图标
    @SerializedName("charming_level")
    val charmingLevel: Int = 0, //用户魅力等级
    @SerializedName("charming_total")
    val charmingTotal: Int = 0,//用户魅力值
    @SerializedName("charming_icon")
    val charmingIcon: String = "",//用户魅力等级图标
    @SerializedName("vip_level")
    var vipLevel: Int = 0, //用户vip等级
    @SerializedName("vip_icon")
    val vipIcon: String = "",//用户vip图标
    @SerializedName("vip_color")
    val vipColor: String = "",//用户vip昵称颜色
    @SerializedName("svip_level")
    val svipLevel: Int = 0, //用户vip等级
    @SerializedName("svip_icon")
    val svipIcon: String = "", //用户svip图标
    @SerializedName("is_stealth")
    var isStealth: Int = 0, //用户是否隐身进房: 1是, 0否
    @SerializedName("is_follow")
    var follow: Int = 0,//用户是否关注房主: 1是, 0否
    @SerializedName("is_owner")
    val owner: Int = 0,//用户是否房主: 1是, 0否
    @SerializedName("is_admin")
    var admin: Int = 0,//用户是否管理员: 1是, 0否
    @SerializedName("is_ban_comment")
    var banComment: Int = 0,//用户是否禁止评论: 1是, 0否
    @SerializedName("headwear_url")
    val headwearUrl: String = "", //用户头像框
    @SerializedName("effects_url")
    val effectsUrl: String = "",//用户进房svga
    @SerializedName("car_name")
    val carName: String = "",//用户座驾名称
    @SerializedName("car_url")
    val carUrl: String = "",//用户座驾名svga
    @SerializedName("car_is_cp")
    val carIsCp: Int = 0,//用户是否cp座驾: 1是, 0否
    @SerializedName("car_cp_uid1_avatar")
    val carCpUid1Avatar: String = "",//用户cp座驾用户1头像
    @SerializedName("car_cp_uid2_avatar")
    val carCpUid2Avatar: String = "",//用户cp座驾用户2头像
    @SerializedName("gift_earnings")
    val giftEarnings: Float = 0f,//麦位用户在房间的魅力值
    @SerializedName("uid")
    val uid: Int = -1,
    @SerializedName("chat_bubble_url")
    val chatBubbleUrl: String = "",//聊天气泡
    @SerializedName("country_info")
    val countryInfo: Country? = null,
    @SerializedName("visit_time")
    val visitTime: Long = 0,
    @SerializedName("gap")
    val gap: Float = 0f,
    @SerializedName("follow_count")
    val followCount: Int = 0,
    @SerializedName("fans_count")
    val fansCount: Int = 0,
    @SerializedName("visitor_count")
    val visitorCount: Int = 0,
    @SerializedName("gift_wall_list")
    val giftWallList: GiftCategory? = null,
    @SerializedName("img_list")
    val imgList: List<Photo> = ArrayList<Photo>(),
    @SerializedName("gift_dress_up_list")
    val giftDressList: List<GiftCategory> = ArrayList(),
    @SerializedName("is_live")
    val isLive: Int = 0,
    @SerializedName("is_black")
    var isBlack: Int = 0,
    @SerializedName("birthday")
    val birthday: String = "",
    @SerializedName("constellation")
    val constellation: String = "",
    @SerializedName("signature")
    var signature: String = "",
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("is_reg_perfect")
    var isRegPerfect: Int = 0,
    @SerializedName("mobile")
    val mobile: String = "", //mobile
    @SerializedName("medal_list")
    val medalList: List<String> = ArrayList(),
    @SerializedName("vip_profile_card")
    val vipProfileCard: String = "",//用户资料卡动态图片

    @SerializedName("is_agent")
    val isAgent: Int = 0,
    @SerializedName("is_gold_agents")
    val isGoldAgent: Int = 0,
    @SerializedName("btn_status")
    var btnStatus: Int = 0,
    @SerializedName("is_color_speech")
    var isColorSpeech: Int = 0,

    @SerializedName("identity_tag_list")
    val identityTagList: List<String> = ArrayList(1)

) : Parcelable {
    @IgnoredOnParcel
    var shareSelected: Boolean = false
    fun isAdmin(): Boolean {
        return admin == 1
    }

    fun isEmpty(): Boolean {
        return id <= 0
    }


    fun isRoomOwner(): Boolean {
        return owner == 1
    }

    fun hasFollow(): Boolean {
        return follow == 1
    }

    fun isStealth(): Boolean {
        return isStealth == 1
    }

    fun isBanComment(): Boolean {
        return banComment == 1
    }

    fun updateBanCommentState(banState: Int) {
        banComment = banState
    }

    fun updateStealth(status: Int) {
        isStealth = status
    }

    fun hasBeautyNumber(): Boolean {
        return luck > 0
    }

    fun hasLive(): Boolean {
        return isLive == 1
    }

    fun hasBlock(): Boolean {
        return isBlack == 1
    }

    fun updatePerfect() {
        isRegPerfect = 1
    }

    fun hasRegPerfect(): Boolean {
        return isRegPerfect == 1
    }

    fun hasAgency(): Boolean {
        return guildId > 0
    }

    fun isAgentState(): Boolean {
        return isAgent == 1
    }

    fun hasGoldAgent(): Boolean {
        return isGoldAgent == 1
    }

    fun haveStealth(): Boolean {
        return isStealth == 1
    }

    fun haveHighlightName(): Boolean {
        return isColorSpeech == 1
    }

}

enum class UserBanComment(val type: Int) {
    ALLOW_COMMENT(0),
    FORBID_COMMENT(1),
}

enum class UserSetAdmin(val type: Int) {
    SET_ADMIN(1),
    CANCEL_ADMIN(0),
}

data class LuckyVoiceInfo(
    @SerializedName("voice_uid")
    val voiceId: Int
)

@Parcelize
data class VoiceInfo(
    @SerializedName("id")
    val id: Int = 0,  //房间id
    @SerializedName("group_id")
    val groupId: String = "", //房间腾讯IM群组ID
    @SerializedName("heat")
    var heat: Int = 0,//房间热度
    @SerializedName("online_number")
    val onlineNumber: Int = 0, //房间在线人数
    @SerializedName("user_id")
    val userId: Int = 0,  //房主id
    @SerializedName("user_nickname")
    val userNickname: String = "",  //房主昵称
    @SerializedName("avatar")
    var avatar: String = "", //房间封面
    @SerializedName("luck")
    val luck: Int = 0, //房主靓号
    @SerializedName("guild_id")
    val guildId: Int = 0, //房主公会id
    @SerializedName("user_zone_id")
    val userZoneId: Int = 0, //房主地区id
    @SerializedName("guild_zone_id")
    val guildZoneId: Int = 0, //房主公会地区id
    @SerializedName("voice_name")
    var voiceName: String = "", //房间名称
    @SerializedName("topic")
    var topic: String = "",//房间话题
    @SerializedName("game_id")
    var gameId: Int = 0,//房间关联游戏id
    @SerializedName("game_name")
    val gameName: String = "",//房间关联游戏名称
    @SerializedName("game_icon")
    var gameIcon: String = "",//房间关联游戏图标
    @SerializedName("requirements")
    var requirements: String = "", //房间要求
    @SerializedName("theme_id")
    var themeId: Int = 0,//房间主题id
    @SerializedName("theme_name")
    var themeName: String = "",//房间主题名称
    @SerializedName("theme_icon")
    var themeIcon: String = "",//房间主题名称
    @SerializedName("theme_intro")
    var themeIntroduce: String = "",//主题介绍
    @SerializedName("theme_bg")
    var themeBg: String = "",//房间主题背景
    @SerializedName("voice_have_psd")
    var voiceHavePsd: Int = 0, //房间是否有密码: 1是, 0否
    @SerializedName("voice_psd")
    var voicePsd: String = "", //房间密码, 对称加密
    @SerializedName("charm_status")
    var charmStatus: Int = 0,//魅力值开关: 1开, 0关
    @SerializedName("mic_num_type")
    val micNumType: Int = 0,//麦位数量类型：1十个麦位；2十五个麦位; 3十二个麦位; 4二十个麦位;
    @SerializedName("voice_bg_id")
    val voiceBgId: Int = 0,//房间背景图id, 此版本没有, 需要客户端根据game_id显示对应的背景图
    @SerializedName("voice_bg_img")
    var voiceBgImg: String = "",//房间背景图url, 此版本没有, 需要客户端根据game_id显示对应的背景图
    @SerializedName("voice_psd_origin")
    val voicePsdOrigin: String = "",//测试线: 房间密码, 未加密
    @SerializedName("mic_user_list")
    var micUserList: MutableList<MicUserInfo> = ArrayList(), //麦位列表
    @SerializedName("sound_net_token")
    val soundNetToken: String = "",
    @SerializedName("sound_net_token_origin")
    val soundNetTokenOrigin: String = "",
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("seat_style_type")
    var seatStyleType: Int = 0,//房间麦位样式
    @SerializedName("country_info")
    val countryInfo: CountryInfo? = null,
    var isAristocrat: Boolean = false,
    @SerializedName("lucky_bag_switch")
    val luckyBagSwitch: Int = 0
) : Parcelable {
    var micMode: Int = 0

    fun haveAristocrat(): Boolean {
        return isAristocrat
    }

    fun charmSwitchOpen(): Boolean {
        return charmStatus == 1
    }

    fun setCharSwitchState(state: Int) {
        charmStatus = state
    }

    fun havePWD(): Boolean {
        return voiceHavePsd == 1
    }

    fun checkRoomPWD(inputPWD: String): Boolean {
        return inputPWD == voicePsd
    }


    fun getMicSeatMode(): RoomMicMode {
        return RoomMicMode.getMicMode(micUserList.size)
    }

    fun getMicSeatModeBy(): RoomMicMode {
        return getMicModeByMode(micMode, seatStyleType)
    }

    fun haveLuckyBag(): Boolean {
        return luckyBagSwitch == 1
    }


}

const val OWNER_MIC_TYPE = 1

const val DEFAULT_MIC_TYPE = 0

//麦位信息
@Parcelize
data class MicUserInfo(
    @SerializedName("mic_id")
    val micId: Int = -1,//麦位id, 从0开始
    @SerializedName("type")
    val type: Int = 0, //麦位是否锁麦: 0否, 1是
    @SerializedName("silence")
    val silence: Int = 0,//麦位是否静音: 0否, 1是
    @SerializedName("speaking")
    var speaking: Boolean = false,
    @SerializedName("user_info")
    val seatInfo: MicSeatInfo,//麦位用户信息
    override var itemType: Int,
    var charmState: Int = 0,
    var isAristocrat: Boolean = false

) : Parcelable, MultiItemEntity, MicSeatListData {
    var isSelected: Boolean = false


    fun isMute(): Boolean {
        return silence == 1
    }

    fun isLock(): Boolean {
        return type == 1
    }

    fun isSpeaking(): Boolean {
        return speaking
    }

    fun showCharmValue(): Boolean {
        return charmState == 1
    }

    fun setTypeItemTypes(): Int {
        return if (micId == 0) {
            OWNER_MIC_TYPE
        } else {
            DEFAULT_MIC_TYPE
        }
    }


    override fun areItemsTheSame(newItem: MicSeatListData): Boolean {
        if (newItem !is MicUserInfo) return false
        return this.micId == newItem.micId
    }

    override fun areContentsTheSame(newItem: MicSeatListData): Boolean {
        if (newItem !is MicUserInfo) return false
        return (this.micId == newItem.micId &&
                this.type == newItem.type &&
                this.silence == newItem.silence
                && this.speaking == newItem.speaking &&
                this.seatInfo.userId == newItem.seatInfo.userId) &&
                this.isAristocrat == newItem.isAristocrat &&
                this.charmState == newItem.charmState &&
                this.itemType == newItem.itemType &&
                this.isMute() == newItem.isMute() &&
                this.isLock() == newItem.isLock() &&
                this.showCharmValue() == newItem.showCharmValue()

    }

}

@Parcelize
data class MicSeatInfo(
    @SerializedName("user_id")
    val userId: Int = 0, //麦位用户id
    @SerializedName("user_nickname")
    var userNickname: String = "",//麦位用户昵称
    @SerializedName("avatar")
    val avatar: String = "", //麦位用户头像
    @SerializedName("gift_earnings")
    var giftEarnings: Float = 0f,//麦位用户在房间的魅力值
    @SerializedName("is_owner")
    var owner: Int = 0,//麦位用户是否房主: 1是, 0否
    @SerializedName("is_admin")
    var admin: Int = 0,//麦位用户是否管理员: 1是, 0否
    @SerializedName("headwear_url")
    val headWearUrl: String = "", //麦位用户头像框
    @SerializedName("vip_level")
    var vipLevel: Int = 0, //麦位用户vip等级
    @SerializedName("vip_color")
    val vipColor: String = "",//麦位用户vip昵称颜色
    @SerializedName("svip_level")
    val svipLevel: Int = 0,//麦位用户svip等级
    @SerializedName("is_ban_comment")
    var banComment: Int = 0, //麦位用户是否禁止评论

) : Parcelable, SeatListData {
    @SerializedName("is_black")
    var isBlack: Int = 0

    fun haveBlack(): Boolean {
        return isBlack == 1
    }

    fun isOwner(): Boolean {
        return owner == 1
    }

    fun isAdmin(): Boolean {
        return admin == 1
    }

    fun isEmpty(): Boolean {
        return userId == 0
    }

    fun isBanComment(): Boolean {
        return banComment == 1
    }

    fun isSelf(): Boolean {
        return userId == Constants.getUserId()
    }

    @SuppressLint("DefaultLocale")
    fun getCharmValue(): String {
        return if (giftEarnings >= 1000_10000) {
            String.format("%.1fM", giftEarnings / 100_0000f)
        } else if (giftEarnings >= 1000) {
            String.format("%.1fK", giftEarnings / 1000f)
        } else {
            "$giftEarnings"
        }
    }


    override fun equals(other: Any?): Boolean {
        if (other !is MicSeatInfo) {
            return false
        }
        return if (this.userId == other.userId &&
            this.userNickname == other.userNickname &&
            this.avatar == other.avatar && this.giftEarnings == other.giftEarnings &&
            this.owner == other.owner && this.admin == other.admin && this.headWearUrl == other.headWearUrl &&
            this.vipLevel == other.vipLevel && this.vipColor == other.vipColor &&
            this.svipLevel == other.svipLevel && this.banComment == other.banComment
        ) {
            true
        } else {
            false
        }
    }

    override fun areItemsTheSame(newItem: SeatListData): Boolean {
        return this == newItem
    }

    override fun areContentsTheSame(newItem: SeatListData): Boolean {
        if (newItem !is MicSeatInfo) {
            return false
        }
        return this.userId == newItem.userId && this.userNickname == newItem.userNickname &&
                this.avatar == newItem.avatar && this.headWearUrl == newItem.headWearUrl &&
                this.giftEarnings == newItem.giftEarnings && this.svipLevel == newItem.svipLevel &&
                this.vipColor == newItem.vipColor && this.vipLevel == newItem.vipLevel &&
                this.banComment == newItem.banComment && this.owner == newItem.owner &&
                this.admin == newItem.admin
    }
}


/**
 * 麦位操作
 */
enum class MicOperate(val op: Long) {
    MIC_ON(0), //上麦
    MIC_OFF(1), //下麦
    MIC_LOCK(2), //封禁麦位
    MIC_UN_LOCK(3), //解除麦位封禁
    MIC_MUTE(4), //麦位静音
    MIC_UN_MUTE(5), //解除麦位静音
    MIC_KICK_OUT(6), //踢人下麦
    MIC_ROOM_KICK_OUT(7),//踢人出房间
    MIC_INVITE(8),//邀请
    MIC_PERSON_INFO(9),//查看个人信息
    MIC_COMMENT(10), //评论
    MIC_FORBID_COMMENT(11), //禁止评论
    MIC_SET_ADMIN(12), //設置管理員
    MIC_CANCEL_ADMIN(13), //取消管理員
    MIC_ADD_BLACK(14), //加入黑名单
    MIC_PERSON_REPORT(15),//举报
    MIC_UN_BLACK(16), //加入黑名单

}

enum class OfficalOperate(val op: Int) {
    BAN_ACCOUNT(1), //封禁账户
    BLOCKED_DEVICE(2), //封禁设备
    BANNED_ROOM(3), //封禁房间
    FREEZE_COINS(5), //冻结金币
    FREEZE_DIAMONDS(6), //冻结钻石
    FREEZE_WALLET(4), //冻结钱包

}

enum class OfficalBanReason(val type: Int) {
    BAN_TYPE_PUBLISH_ADVERTISE(1),
    BAN_TYPE_VERBAL_ABUSER(2),
    BAN_TYPE_SEXUAL_HARASSMENT(3),
    BAN_TYPE_POLITICAL_SENSITIVE(4),
    BAN_FRAUD(5),
    BAN_OTHER(6)

}


data class OfficialBanReasonItem(
    val reason: OfficalBanReason,
    var conditionSelect: Boolean = false
)

data class OfficialBanTimeItem(
    val reason: OfficalBanTime,
    var conditionSelect: Boolean = false
)

enum class OfficalBanTime(val type: Int) {
    BAN_TIME_8_HOUR(1),
    BAN_TIME_1_DAY(2),
    BAN_TIME_3_DAY(3),
    BAN_TIME_7_DAY(4),
    BAN_TIME_FOREVER(5)
}

/**
 * 麦位操作结果
 */
data class MicSeatOpResult(
    val op: MicOperate,
    val index: Int,
    val uid: Int,
    val rlt: Res<Any>,
)

data class MemberSpeaking(val uid: Int)

/**
 * 邀请在线列表
 * */
class InviteUserInfoList(val list: ArrayList<UserInfo>)


//踢出时间选项: 1:本次踢出,2:踢出5分钟,3:踢出1小时,4:踢出24小时
enum class KICK_OUT(val type: Int) {

    KICK_ONCE(1),
    KICK_FIVE_MINUTES(2),
    KICK_ONE_HOUR(3),
    KICK_24_HOUR(4)

}

//im收到踢人变动通知
data class EventKickOut(
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,
    @SerializedName("group_id")
    val groupId: String = ""
)

//im收到麦位变动通知
class EventOnMic {
    val voice_uid: Int = 0
    val mic_id: Int = 0
    val from_uid: Int = 0
    val from_user_nickname: String = ""

    override fun toString(): String {
        return "Info(voice_uid=$voice_uid, mic_id=$mic_id, from_uid=$from_uid, from_user_nickname='$from_user_nickname')"
    }
}

//房间在线用户列表
data class OnLineUsersInfo(
    @SerializedName("online_user_list")
    val onlineUserList: ArrayList<UserInfo> = ArrayList()
)

//房间礼物信息
@Parcelize
class GiftInfo(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("img")
    val img: String = "",

    @SerializedName("price")
    val price: Int = 0,

    @SerializedName("svga")
    val svga: String = "",

    @SerializedName("mp4")
    val mp4: String = "",

    @SerializedName("count")
    var count: Int = 0,

    @SerializedName("is_lottery")
    val isLottery: Int = 0,

    @SerializedName("reward")
    val reward: Int = 0,

    @SerializedName("times")
    val times: Int = 0,

    @SerializedName("luck_rate_introduce")
    val luckRateIntroduce: String = "",

    @SerializedName("luck_rate")
    val luckRate: Int = 0,

    @SerializedName("first_avatar")
    val firstAvatar: String = "",
    @SerializedName("boom_avatar")
    val boomAvatar: String = "",

) : Parcelable {
    fun showLuckyHighDialog(): Boolean {
        return reward >= 500
    }
}

enum class CharmSWitch(val type: Int) {
    SWITCH_ON(1),
    SWITCH_OFF(0);
}

class AdminOperateInfo(
    @SerializedName("voice_uid")
    val voice_uid: Int,
    @SerializedName("user_id")
    val UserId: Int
)

//修改房间信息
class RoomUpdateInfo(
    @SerializedName("voice_uid")
    val voiceUid: Double = 0.0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("topic")
    val topic: String = "",
    @SerializedName("avatar")
    val avatar: String = "",
    @SerializedName("voice_have_psd")
    val voiceHavePsd: Int = 0,
    @SerializedName("voice_psd")
    val voicePsd: String = "",
    @SerializedName("themeId")
    val themeId: Int = 0,
    @SerializedName("themeIcon")
    val themeIcon: String = "",
    @SerializedName("themeName")
    val themeName: String = "",
    @SerializedName("heat")
    val hotValue: Int = 0,

    )

enum class RoomType(val type: Int) {
    GAME_TEAMING(3),
    GAME_MUSIC(4),
    CHATTING_FRIENDS(2),
    SOCIAL_SHARE(1)

}

data class RoomUserInfo(
    @SerializedName("info")
    val userInfo: UserInfo? = null
)

enum class RoomSeatStyleType(val seatStyleType: Int) {
    ROOM_SEAT_STYLE_GAME(1),
    ROOM_SEAT_STYLE_CLASSIC(2),
    ROOM_SEAT_STYLE_VIP(3);

    companion object {
        fun getType(value: Int): RoomSeatStyleType? {
            return entries.find { it.seatStyleType == value }
        }
    }

}

//房间麦位数量模式
enum class RoomMicMode(val micMode: Int, val seatCount: Int) {
    MIC_MODE_10(1, 10),
    MIC_MODE_CLASSIC_10(1, 10),
    MIC_MODE_15(2, 15),
    MIC_MODE_12(3, 12),
    MIC_MODE_20(4, 20);

    companion object {
        private val seatCountToModeMap = RoomMicMode.entries.associateBy { it.seatCount }
        private val micModeToEnumMap = entries.associateBy { it.micMode }

        fun getMicMode(
            seatCount: Int,
            seatStyle: Int = RoomSeatStyleType.ROOM_SEAT_STYLE_GAME.seatStyleType
        ): RoomMicMode {
            val oldMode = seatCountToModeMap[seatCount] ?: MIC_MODE_10
            return if (oldMode == MIC_MODE_10) {
                if (seatStyle == RoomSeatStyleType.ROOM_SEAT_STYLE_GAME.seatStyleType) {
                    oldMode
                } else {
                    MIC_MODE_CLASSIC_10
                }
            } else {
                oldMode
            }


        }

        fun getMicModeByMode(
            micMode: Int,
            seatStyle: Int = RoomSeatStyleType.ROOM_SEAT_STYLE_GAME.seatStyleType
        ): RoomMicMode {
            val oldMode = micModeToEnumMap[micMode] ?: MIC_MODE_10
            return if (oldMode == MIC_MODE_10) {
                if (seatStyle == RoomSeatStyleType.ROOM_SEAT_STYLE_GAME.seatStyleType) {
                    oldMode
                } else {
                    MIC_MODE_CLASSIC_10
                }
            } else {
                oldMode
            }

        }
    }
}

data class IMChangeMicModeInfo(
    @SerializedName("voice_uid") val voiceUid: Int = 0,
    @SerializedName("mic_num_type")
    val micModeType: Int = 1,
    @SerializedName("style_type")
    val styleType: Int = 1
)


data class IMHighLightNameSWitch(
    @SerializedName("voice_uid") val voiceUid: Int = 0,
    @SerializedName("status")
    val status: Int = 0,
) {
    fun haveHighLightName(): Boolean {
        return status == 1
    }
}


data class IMTurnOffIncognitoSWitch(
    @SerializedName("voice_uid") val voiceUid: Int = 0,
    @SerializedName("status") // 是否开启隐身进房: 1-是; 0-否;
    val status: Int = 0,
)

data class IMBackgroundInfo(
    @SerializedName("voice_uid") val voiceUid: Int = 0,
    @SerializedName("voice_bg_img")
    val voiceBgImg: String = "",
)


data class MemberEnterInfo(
    @SerializedName("voice_uid")
    val voiceUid: Int = 0,  // 用 Long 类型而不是 Int/Double

    @SerializedName("uid")
    val uid: Int = 0,       // 同上，避免小数导致解析异常

    @SerializedName("effects_url")
    val effectsUrl: String = "",

    @SerializedName("car_url")
    val carUrl: String = "",
    @SerializedName("nickname")
    val nickName: String,
    @SerializedName("avatar")
    val avatar: String
)

data class BlockInfo(
    @SerializedName("msg")
    var msg: String = "",

    )

data class RoomOnLineAvatarInfo(
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("avatar_list")
    val avatarList: List<String> = emptyList(),
)


