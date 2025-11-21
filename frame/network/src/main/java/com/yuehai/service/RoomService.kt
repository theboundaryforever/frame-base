package com.yuehai.service

import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.room.BackgroundResult
import com.yuehai.data.collection.path.room.BlockResultInfo
import com.yuehai.data.collection.path.room.PurchaseBackgroundInfo
import com.yuehai.data.collection.path.room.PurchaseBackgroundTimeInfo
import com.yuehai.data.collection.path.user.GameCenterInfo
import com.yuehai.data.collection.path.user.InviteUserInfoList
import com.yuehai.data.collection.path.user.MicSeatInfo
import com.yuehai.data.collection.path.user.OnLineUsersInfo
import com.yuehai.data.collection.path.user.RoomCardInfo
import com.yuehai.data.collection.path.user.RoomInfo
import com.yuehai.data.collection.path.user.RoomOnLineAvatarInfo
import com.yuehai.data.collection.path.user.RoomUserInfo
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RoomService {
    @GET("api/voice/index")
    suspend fun getRoomInfo(
        @Query("uid") uid: Int,
        @Query("voice_uid") voiceUid: String, //房主id
    ): Rlt<Res<RoomInfo>>


    @FormUrlEncoded
    @POST("api/voice/mic_online")
    suspend fun micOn(
        @Field("voice_uid") voiceUid: Int?, //房主id
        @Field("mic_id") micId: Int, //麦位id
    ): Rlt<Res<MicSeatInfo>>

    @FormUrlEncoded
    @POST("api/voice/mic_offline")
    suspend fun micOff(
        @Field("voice_uid") voiceUid: Int,
        @Field("uid") uid: Int
    ): Rlt<Res<MicSeatInfo>>

    @FormUrlEncoded
    @POST("api/voice/mic_lock")
    suspend fun micLockOrUnLock(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("mic_id") micId: Int, //麦位id
        @Field("type") type: Int, //是否锁麦: 1是; 0否;
    ): Rlt<Res<Any>>

    @FormUrlEncoded
    @POST("api/voice/mic_mute")
    suspend fun micMuteOrUnMute(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("mic_id") micId: Int, //麦位id
        @Field("type") type: Int, //是否静音: 1是; 0否;

    ): Rlt<Res<Any>>


    /**
    抱人上麦用户列表, 只有房主和管理员才能查看
    uid	             string	用户id
    token	         string	token
    voice_uid	     string	房主id
    ts	             string	随机数, 例如时间戳
    sign	         string	签名signpi/voice/invite_mic_user_list

     */
    @GET("api/voice/invite_mic_user_list")
    suspend fun getInviteMicUserList(
        @Query("voice_uid") voiceUid: Int, //房主id
    ): Rlt<Res<InviteUserInfoList>>

    /**
     *踢人下麦, 只有房主和管理员才有权限, 被踢下麦用户收到type=207的IM消息
     *
     * uid	        int	     用户id
     * token		string	 token
     * voice_uid	int  	 房主id
     * to_uid	    int	     用户id
     * ts	        int  	 随机数, 例如时间戳
     * sign	        string	 签名sign
     */
    @FormUrlEncoded
    @POST("api/voice/kick_off_mic")
    suspend fun kickOffMic(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("to_uid") micUid: Int, //用户id

    ): Rlt<Res<Any>>


    /**
     * 房间踢人接口
    uid		    是	int	用户id
    token		   是	string	token
    voice_uid  	是	int	房主id
    to_uid	   	是	int	被踢用户id
    type		    是	int	踢出时间选项: 1:本次踢出,2:踢出5分钟,3:踢出1小时,4:踢出24小时
    ts		    是	int	随机数, 例如时间戳
    sign		   是	string	签名sign
     *
     */
    @FormUrlEncoded
    @POST("api/voice/kick_out")
    suspend fun kickUserOut(
        @Field("voice_uid") voice_uid: Int, //房主id
        @Field("to_uid") to_uid: Int, //被踢用户id
        @Field("type") type: Int,  //踢出时间选项: 1:本次踢出,2:踢出5分钟,3:踢出1小时,4:踢出24小时
        @Field("is_block") isBlock: Int,  //踢出房间时是否拉黑

    ): Rlt<Res<Any>>


    /**
    邀请上麦, 只有房主和管理员才有权限, 被锁的麦位不能邀请, 被邀请上麦用户收到type=206的IM消息
    uid	       int	    用户id
    token	   string	token
    voice_uid	int  	房主id
    to_uid   	int	    用户id
    mic_id		int	    麦位id
     */
    @FormUrlEncoded
    @POST("api/voice/invite_mic")
    suspend fun inviteOnMic(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("to_uid") toUid: Int, //被邀请用户id
        @Field("mic_id") micId: Int, //麦位id
    ): Rlt<Res<Any>>

    /**
     * 房间在线用户列表
    uid      	string	用户id
    token	    string	token
    voice_uid	string	房主id
    ts	        string	随机数, 例如时间戳
    sign	    string	签名sign
     */
    @FormUrlEncoded
    @POST("api/voice/online_user_list")
    suspend fun getOnlineUserList(
        @Field("voice_uid") voiceUid: Int, //房主id
    ): Rlt<Res<OnLineUsersInfo>>

    /**
     * 退出房间
     * */
    @GET("api/voice/room_exit")
    suspend fun getExitRoom(@Query("voice_uid") voiceUid: Int): Rlt<Res<Any>>

    /**
     * 魅力值开关
     * */
    @FormUrlEncoded
    @POST("api/voice/charm_value")
    suspend fun charmSwitch(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("status") status: Int, //房间是否显示魅力值: 1是; 0否;
    ): Rlt<Res<Any>>


    /**
     * 禁止/解禁用户评论, 用户禁止评论收到type=208的IM消息, 用户解除禁止评论收到type=209的IM消息
    uid		        用户id
    token	     	token
    voice_uid		房主id
    to_uid	        被操作用户id
    status	        是否禁止用户评论: 1是; 0否;
    ts	            时间戳
    sign		    签名sign
     */
    @FormUrlEncoded
    @POST("api/voice/ban_comment")
    suspend fun banComment(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("to_uid") toUid: Int, //被操作用户id
        @Field("status") status: Int, //是否禁止用户评论: 1是; 0否;
    ): Rlt<Res<Any>>


    @FormUrlEncoded
    @POST("api/voice/add_admin")
    suspend fun addRoomAdmin(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("to_uid") toUid: Int, //目标操作的用户id

    ): Rlt<Res<Any>>

    @FormUrlEncoded
    @POST("api/voice/del_admin")
    suspend fun removeRoomAdmin(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("to_uid") toUid: Int, //目标操作的用户id

    ): Rlt<Res<Any>>


    /**
     *拉黑/解除拉黑用户, 只有房主才有权限   移除和拉黑都是跑这个接口
    uid		int 用户id
    token		string	token
    voice_uid		int	房主id
    to_uid	int	用户id
    status		int	是否拉黑用户: 1是; 0否;
    ts	int	随机数, 例如时间戳
    sign	string	签名sign
     */

    @FormUrlEncoded
    @POST("api/voice/block_user")
    suspend fun blackOrUnBlackUser(
        @Field("voice_uid") voiceUid: Int, //房主id
        @Field("to_uid") toUid: Int, //目标操作id
        @Field("status") status: Int = 1,
    ): Rlt<Res<Any>>

    /**
     * 获取用户信心
     * */
    @GET("api/voice/user_info")
    suspend fun getRoomUserInfo(
        @Query("voice_uid") voiceUid: Int,
        @Query("to_uid") toUid: Int
    ): Rlt<Res<RoomUserInfo>>


    /**
     *
     * 更改麦位数量模式
     * */
    @FormUrlEncoded
    @POST("api/voice/mic_num_type")
    suspend fun changeMicSeatNum(
        @Field("voice_uid") voiceUid: Int,
        @Field("type") type: Int,
        @Field("style_type") styleType: Int,

        ): Rlt<Res<Any>>

    /**
     *
     * 获取房间游戏列表
     *
     * */
    @GET("/api/game/list")
    suspend fun getRoomGameInfo(): Rlt<Res<GameCenterInfo>>

    /**
     *
     * 获取房间信息
     *
     * */

    @GET("api/voice/room_data")
    suspend fun getRoomOwnerInfo(@Query("voice_uid") voiceUid: Int): Rlt<Res<RoomCardInfo>>


    /**
     * 是否开启隐身
     *
     * type 权限: 1-禁止关注; 2-隐身进房; 3-隐身访客; 4-防踢权限;
     * */
    @POST("api/vip/set_privilege")
    @FormUrlEncoded
    suspend fun turnOffIncognito(
        @Field("status") status: Int = 0,
        @Field("type") type: Int = 2
    ): Rlt<Res<Any>>

    /**
     * 封禁权限列表
     * */
    @GET("api/block/user_block_permission")
    suspend fun getUserBlockPermission(@Query("to_uid") toUid: Int): Rlt<Res<BlockResultInfo>>

    /**
     *
     * 封禁用户
     * */
    @POST("api/block/block_user")
    @FormUrlEncoded
    suspend fun blockUser(
        @FieldMap map: HashMap<String, Any>
    ): Rlt<Res<Any>>

    /**
     *
     * 解封用户
     * */
    @POST("api/block/unblock_user")
    @FormUrlEncoded
    suspend fun unBlockUser(
        @FieldMap map: HashMap<String, Any>
    ): Rlt<Res<Any>>

    /**
     *
     * 获取房间商城背景
     *
     * */
    @GET("/api/mall/room_bg_list")
    suspend fun getRoomMallBackground(): Rlt<Res<BackgroundResult>>

    /**
     *
     * 获取房间免费背景
     *
     * */
    @GET("/api/mall/free_room_bg_list")
    suspend fun getRoomFreeBackground(): Rlt<Res<BackgroundResult>>

    @GET("/api/my_backpack/my_room_bg_list")
    suspend fun getRoomMineBackground(): Rlt<Res<BackgroundResult>>

    @GET("/api/mall/buy_room_bg")
    suspend fun buyRoomBackground(@Query("id") bgId: Int): Rlt<Res<PurchaseBackgroundInfo>>

    @GET("/api/my_backpack/use_room_bg")
    suspend fun useRoomBackground(
        @Query("id") bgId: Int,
        @Query("status") status: Int
    ): Rlt<Res<Any>>

    //房间重连
    @GET("api/voice/reconnect")
    suspend fun onVoiceRoomReconnect(
        @Query("voice_uid") voiceUid: String,
    ): Rlt<Res<Any>>

    @GET("api/voice/online_number")
    suspend fun getOnLineUserAvatarList(@Query("voice_uid") voiceUid: Int): Rlt<Res<RoomOnLineAvatarInfo>>
}