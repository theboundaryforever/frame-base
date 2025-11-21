package com.yuehai.data.collection.path

import com.yuehai.data.collection.path.global.GlobalConfig
import com.yuehai.data.collection.path.json.froJsonErrorNull
import com.yuehai.data.collection.path.json.toJsonErrorNull
import com.yuehai.data.collection.path.login.LoginType
import com.yuehai.data.collection.path.room.RocketProgressInfo
import com.yuehai.data.collection.path.user.UserInfo
import com.yuehai.util.BuildConfig

class Constants {
    companion object {

        const val yoopoTestUrl = "http://47.242.52.123/" //测试环境

        const val yoopoUrlRelease = "https://www.yoppo.net/"  //正式环境

        const val yoopolWebUrl = "http://47.242.52.123/"
        const val yoopolWebReleaseUrl = "https://cdn.yoppo.net/"
        const val GOOGLE_REQUEST_ID_TOKEN =
            "491363354710-mahnmol4i5m1jgq5anud60bu6p48oh16.apps.googleusercontent.com"
        const val GOOGLE_CODE_SIGN_IN: Int = 15000
        val FACE_BOOK_PERMISSION = listOf("public_profile")

        val yoopoUrl = if (BuildConfig.DEBUG) {
            yoopoTestUrl
        } else {
            yoopoUrlRelease
        }

        val yoppoWebUrl = if (BuildConfig.DEBUG) {
            yoopolWebUrl
        } else {
            yoopolWebReleaseUrl
        }

        //yoopo腾讯tim APPID
        const val YoopoTimAppIdDebug = 40000488 //测试环境
        const val YoopoTimAppIdReplease = 40000577 //正式环境
        val YoopoTimAppId = if (BuildConfig.DEBUG) {
            YoopoTimAppIdDebug
        } else {
            YoopoTimAppIdReplease
        }
        val BUGLY_ID = if (BuildConfig.DEBUG) {
            "13fe1542e6"
        } else {
            "0fc0d2b7f6"
        }

        //协程请求超时15s 比如登录 如果有问题 加载框会一直展示较长 用户体验很差  okhttp那里设置连接超时不起作用
        const val requestTimeOut = 15000L

        const val createGameVoiceRoomStepOne = "createGameVoiceRoomStepOne"
        const val mGameNameSelected = "mGameNameSelected"
        const val mGameIDSelected = "mGameIDSelected"
        const val mStrTeamRequireMents = "mStrTeamRequireMents"
        const val RoomThemeType = "RoomThemeType"
        const val RoomOwnerId = "RoomOwnerId"
        const val RoomGameId = "roomGameId"

        //礼物MP4动画下载到/data/data/com.yoopo.voiroo/cache/vap
        const val vapMp4Directory = "vap"


        //礼物svga动画下载到/data/data/com.yoopo.voiroo/cache/svga
        const val svgaDirectory = "svga"

        const val yoopoToken = "token"

        const val autoLogin = "autologin"


        const val spLoginUser = "sp_loginuser"
        const val ossbucket = "yoppo"

        const val AppVersion = "app-version"
        const val roomOwnerId = "roomOwnerId"
        const val userId = "id"
        const val channelUserId = "channelUserId"
        const val id = "id"
        const val uid = "uid"
        const val userToken = "token"
        const val userSign = "user_sign" //腾讯IM userSign
        const val userZoneId = "zone_id"//地区id

        const val AESCBCKey = "dxmvrevdjfftjaib" //声网Token解密需要的 后端给的
        const val AESCBCIvParameterSpec = "skkox6ivdu906dl4" //声网Token解密需要的 后端给的

        const val luck = "luck"

        const val OS = "os"  //操作系统
        const val AcceptLanguage = "Accept-Language"
        const val language = "language"

        /**
         * 腾讯IM类型开始  统一格式 {"type":XXXX,"data":{XXXX}}
         */
        const val imTypeKey = "type"
        const val imDataKey = "data"
        const val imTypeA = 1000  //纯文本
        const val imType100 = 100  //送礼物推送type=100
        const val imType101 = 101  //幸运礼物中奖推送type=101
        const val imType102 = 102 //幸运礼物全服推送type=102
        const val imType800 = 800  //送全服礼物推送type=800
        const val imType801 = 801  //幸运礼物中奖全服横幅推送type=801
        const val imType810 = 810  //发送福袋-全服通知
        const val imType811 = 811  //发送福袋-房间通知

        const val imType812 = 812 //福袋领完或已到期-房间通知
        const val imType820 = 820//火箭升级-房间通知
        const val imType821 = 821//火箭升级-全服横幅
        const val imType190 = 190  //房间里聊天输入框发送的内容
        const val imType200 = 200  //用户房间上麦成功 房间内其他用户会收到服务端推送的200类型的IM消息
        const val imType201 = 201  //房主在房间Setting里设置修改了房间主题 图片之类的信息 会收到201 IM
        const val imType202 = 202  //修改房间关联游戏信息, 推送type=202的IM消息
        const val imType203 = 203  //锁麦/解锁麦, 推送type=203的IM消息
        const val imType204 = 204  // 房间魅力值开关, 只有房主才有权限, 推送type=204的IM消息
        const val imType205 = 205  //房间踢人, 被踢用户单独收到type=205的IM消息 C2C类型
        const val imType206 = 206  //被邀请上麦用户收到type=206的IM消息
        const val imType207 = 207  //踢人下麦, 只有房主和管理员才有权限, 被踢下麦用户收到type=207的IM消息
        const val imType208 = 208  //用户禁止评论收到type=208的IM消息
        const val imType209 = 209  //用户解除禁止评论收到type=209的IM消息
        const val imType210 = 210  //添加管理员, 只有房主才有权限, 用户成为管理员收到type=210的IM消息
        const val imType211 = 211  //删除管理员, 只有房主才有权限, 用户解除管理员收到type=211的IM消息

        const val imType212 = 212  //表情包

        const val imType213 = 213 //进入房间
        const val imType214 = 214 //更改房间主题
        const val imType215 = 215 //更改房间麦位数量和类型
        const val imType216 = 216 //更改房间麦位数量和类型
        const val imType217 = 217 //用户开关隐身进房
        const val imType218 = 218 //房间背景

        //如果登录失效 那么后端回{"code":10001,"msg":"Login timed out, please log in again","data":{}}
        //所以在接口判断这个值 如果是这个值 直接跳转到登录页面
        const val LOGIN_INVALID = 10001
        const val USER_BALANCE_NOT_ENOUGH = 30001

        //所以在接口判断这个值 如果是这个值 直接跳转到完善登录界面
        const val LOGIN_INVALID_COMPLETE = 10005

        //由于腾讯IM官方明确指明群主是不可以退群的 也就是quitGroup无效
        //所以对于房主 在TA离开房间时向群发条信息告知离开房间  对于其他的用户 离开房间会回调腾讯的onMemberLeave
        //这个主要是用在房间有人进入和离开时在线用户实时人数和在房用户头像的展示  这个只是针对房主 其他用户不需要
        const val imType230 = 230
        const val imType290 = 290  //用户进入房间 房间内其他用户会收到290类型的IM消息


        const val imType301 = 301  //房间中奖公屏

        const val imType302 = 302  //游戏全服横幅
        const val imType400 = 400  //封禁
        const val imType401 = 401 //封禁房间和用户

        const val roomOwnerLeaveRoom = "roomOwnerLeaveRoom"

        const val mineTabIndex = "mineTabIndex"
        const val mineIndexFollowing = 0
        const val mineIndexFollower = 1
        const val mineIndexVisitor = 2
        const val spSearchHistory = "sp_searchHistory"


        /**
         * php金币兑换接口失败或者异常(如500错误)时 把谷歌的purchaseToken和php的订单号一一对应起来
         *
         */
        const val spFailureCoinOrder = "sp_FailureCoinOrder"


        const val spCoinOrderIdWithProduct = "sp_coinOrderWithProductID"


        const val login_userInfo = "login_userInfo"
        const val app_global_config = "app_global_config"
        const val app_login_type = "app_login_type"
        const val login_rocket_config_Info = "login_rocket_config_Info"


        //登录账户
        const val app_login_account = "app_login_account"

        //房间设置混音开关
        const val USER_ROOM_MIXER_SWITCH = "USER_ROOM_MIXER_SWITCH"

        //房间特效开关
        const val USER_ROOM_EFFECT_SWITCH = "USER_ROOM_EFFECT_SWITCH"

        //房间特效开关
        const val USER_ROOM_MUSIC_VOLUME = "USER_ROOM_MUSIC_VOLUME"

        //状态栏高度
        const val STATUS_BAR_HEIGHT = "STATUS_BAR_HEIGHT"

        fun putLoginUserInfo(userInfo: UserInfo?) {
            if (userInfo == null) {
                return
            }
            YQSPUtils.getInstance().apply {
                putInt(Constants.autoLogin, 1)
            }
            YQSPUtils.getInstance().putString(login_userInfo, "${toJsonErrorNull(userInfo)}")
            YQSPUtils.getInstance().putInt(userId, userInfo.id)
            if (userInfo.token.isNotEmpty()) {
                YQSPUtils.getInstance().putString(userToken, userInfo.token)
            }
        }

        fun putGlobalConfig(appConfig: GlobalConfig) {
            YQSPUtils.getInstance()
                .putString(app_global_config, "${toJsonErrorNull(appConfig)}")
        }

        fun putLoginType(loginType: LoginType) {
            YQSPUtils.getInstance()
                .putInt(app_login_type, loginType.type)
        }

        fun putLoginAccount(loginAccount: String) {
            YQSPUtils.getInstance()
                .putString(app_login_account, loginAccount)
        }

        fun getLoginAccount(): String {
            return YQSPUtils.getInstance().getString(app_login_account)
        }

        fun getLoginType(): LoginType? {
            return LoginType.fromType(YQSPUtils.getInstance().getInt(app_login_type))
        }

        fun getGlobalConfig(): GlobalConfig? {
            return froJsonErrorNull(
                YQSPUtils.getInstance().getString(app_global_config),
                GlobalConfig::class.java
            )
        }

        fun logout() {
            val loginType = getLoginType()
            val loginAccount = getLoginAccount()
            YQSPUtils.getInstance().clear()
            YQSPUtils.getInstance().putInt(app_login_type, loginType?.type ?: 0)
            putLoginAccount(loginAccount)
        }

        fun getLoginUserInfo(): UserInfo? {
            val loginStr = YQSPUtils.getInstance().getString(login_userInfo)
            return froJsonErrorNull(loginStr, UserInfo::class.java)
        }

        fun getUserId(): Int {
            return getLoginUserInfo()?.id ?: 0
        }

        fun getToken(): String {
            return getLoginUserInfo()?.token ?: ""
        }

        fun setEffectSwitch(switchState: Boolean = false) {
            YQSPUtils.getInstance().putBoolean(USER_ROOM_EFFECT_SWITCH, switchState)

        }

        fun getEffectSwitch(): Boolean {
            return YQSPUtils.getInstance().getBoolean(USER_ROOM_EFFECT_SWITCH, true)
        }

        fun saveStatusBarHeight(height: Int) {
            return YQSPUtils.getInstance().putInt(STATUS_BAR_HEIGHT, height)
        }

        fun getStatusBarHeight(): Int {
            return YQSPUtils.getInstance().getInt(STATUS_BAR_HEIGHT)
        }


        fun saveRocketConfigInfo(rocketConfigInfo: RocketProgressInfo) {
            YQSPUtils.getInstance()
                .putString(login_rocket_config_Info, toJsonErrorNull(rocketConfigInfo) ?: "")
        }

        fun getRocketConfigInfo(): RocketProgressInfo? {
            val str = YQSPUtils.getInstance()
                .getString(login_rocket_config_Info)
            return froJsonErrorNull(str, RocketProgressInfo::class.java)
        }


    }


}