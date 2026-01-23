package com.yuehai.media.agora


import android.util.Log
import com.adealik.frame.mvvm.util.removeUiCallbacks
import com.adealik.frame.mvvm.util.runOnUiThread
import com.yuehai.data.collection.path.Rlt
import com.yuehai.coroutine.coroutine.dispatcher.Dispatcher
import com.yuehai.media.VoiceRoomLagMonitor

import com.yuehai.media.constant.JoinChannelError
import com.yuehai.media.constant.MediaChannelNameNull
import com.yuehai.media.constant.MediaTokenNull
import com.yuehai.media.constant.TAG_MEDIA
import com.yuehai.media.constant.TAG_MEDIA_MUSIC
import com.yuehai.media.data.AudioRouter
import com.yuehai.media.data.MediaMusicPlayState
import com.yuehai.media.data.MediaMusicReason
import com.yuehai.media.data.RtcType
import com.yuehai.media.listener.IMediaMusicListener
import com.yuehai.media.listener.IMediaRtcListener
import com.yuehai.util.AppUtil
import com.yuehai.util.language.collection.ConcurrentList
import com.yuehai.util.util.PackageUtil
import io.agora.rtc2.ClientRoleOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED
import io.agora.rtc2.Constants.AUDIO_MIXING_REASON_CAN_NOT_OPEN
import io.agora.rtc2.Constants.AUDIO_MIXING_REASON_INTERRUPTED_EOF
import io.agora.rtc2.Constants.AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED
import io.agora.rtc2.Constants.AUDIO_MIXING_REASON_RESUMED_BY_USER
import io.agora.rtc2.Constants.AUDIO_MIXING_REASON_STOPPED_BY_USER
import io.agora.rtc2.Constants.AUDIO_MIXING_REASON_TOO_FREQUENT_CALL
import io.agora.rtc2.Constants.AUDIO_MIXING_STATE_FAILED
import io.agora.rtc2.Constants.AUDIO_MIXING_STATE_PAUSED
import io.agora.rtc2.Constants.AUDIO_MIXING_STATE_STOPPED

import io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
import io.agora.rtc2.Constants.ERR_ADM_GENERAL_ERROR
import io.agora.rtc2.Constants.ERR_ADM_INIT_PLAYOUT
import io.agora.rtc2.Constants.ERR_JOIN_CHANNEL_REJECTED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import com.yuehai.media.config.IMediaConfig as IMediaConfig1

@Synchronized
fun createMediaService(config: IMediaConfig1): IMediaService {
    return MediaService(config)
}

internal class MediaService(private val config: IMediaConfig1) : IMediaService,
    IRtcEngineEventHandler(), CoroutineScope {

    companion object {
        private const val JOIN_CHANNEL_TIMEOUT = 10_000L
    }

    override val coroutineContext = SupervisorJob()
    private var agoraRtcEngine: RtcEngine? = null
    private val mediaRtcListeners = ConcurrentList<IMediaRtcListener>()
    private val mediaMusicListeners = ConcurrentList<IMediaMusicListener>()
    private var leavingChannel: String? = null
    private var currentChannel: String? = null
    private var selfUid: Int = 0
    private var speakingUidSet = hashSetOf<Int>()
    private var rtcType: RtcType = RtcType.AGORA_RTC
    private var recordingSignalVolume = 50


    interface OnJoinChannelCallback {

        fun onJoinSuccess()

        fun onJoinFailed(channel: String?, errCode: String)

    }

    private var joinChannelCallback: OnJoinChannelCallback? = null

    private fun log(msg: String, xlog: Boolean = false) {
        if (xlog) {
            Log.i(
                TAG_MEDIA,
                "rtcType:$rtcType, currentChannel:$currentChannel, selfUid:$selfUid, $msg"
            )
        } else {
            Log.d(
                TAG_MEDIA,
                "rtcType:$rtcType, currentChannel:$currentChannel, selfUid:$selfUid, $msg"
            )
        }
    }

    override fun setRtcType(rtcType: RtcType) {
        this.rtcType = rtcType
    }

    override fun getRtcType(): RtcType {
        return rtcType
    }

    override fun setSystemVolumeType(type: SystemVolumeType) {

    }


    override fun setClientRole(role: com.yuehai.media.data.ClientRole) {
        log("setClientRole, role:$role", true)
        if (getRtcEngine() != null) {
            getRtcEngine().setClientRole(role.role)
        }

    }

    private fun destroyAgoraRtcEngine() {
        if (agoraRtcEngine != null) {
            RtcEngine.destroy()
            agoraRtcEngine = null
        }
    }

    private fun createAgoraRtcEngine(): RtcEngine {
        return agoraRtcEngine ?: synchronized(this) {
            if (agoraRtcEngine != null) return@synchronized agoraRtcEngine!!

            val config = RtcEngineConfig().apply {
                mAppId = this@MediaService.config.agoraAppId
                mEventHandler = this@MediaService
                mContext = this@MediaService.config.context
                mLogConfig = RtcEngineConfig.LogConfig().apply {
                    level = Constants.LogLevel.getValue(Constants.LogLevel.LOG_LEVEL_INFO)
                    val sdf = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                    filePath =
                        "${this@MediaService.config.mediaPath}${File.separator}media_${
                            PackageUtil.getVersionCode(AppUtil.appContext)
                        }_${sdf.format(Date())}.xlog"
                }
            }

            val engine = RtcEngine.create(config)
                ?: throw IllegalStateException("RtcEngine.create(config) 返回 null，请检查 AppId/Context/EventHandler 配置")

            engine.setChannelProfile(CHANNEL_PROFILE_LIVE_BROADCASTING)
            engine.enableAudioVolumeIndication(1000, 3, false)
            agoraRtcEngine = engine
            engine
        }
    }


    fun getRtcEngine(): RtcEngine {
        return createAgoraRtcEngine()
    }

    /**
     * 网络连接状态已改变回调
     */
    override fun onConnectionStateChanged(state: Int, reason: Int) {
        super.onConnectionStateChanged(state, reason)
        log("onConnectionStateChanged, state$state, reason:$reason", true)
    }

    override fun onLocalUserRegistered(uid: Int, userAccount: String?) {
        super.onLocalUserRegistered(uid, userAccount)
        log("onLocalUserRegistered, uid$uid, userAccount:$userAccount")
    }

    /**
     * 表示客户端已经登入服务器，且分配了频道 ID 和用户 ID
     * @param elapsed: 从 joinChannel 开始到发生此事件过去的时间（毫秒)
     */
    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        super.onJoinChannelSuccess(channel, uid, elapsed)
        log("onJoinChannelSuccess, channel$channel, uid:$uid, elapsed:$elapsed", true)
        if (currentChannel == channel) {
            joinChannelCallback?.onJoinSuccess()
        }
    }

    /**
     * 因网络问题断开逻辑后，重新加入channel，sdk自动触发的重连
     * @param elapsed: 从开始重连到重连成功的时间（毫秒）
     */
    override fun onRejoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
        super.onRejoinChannelSuccess(channel, uid, elapsed)
        log("onRejoinChannelSuccess, channel$channel, uid:$uid, elapsed:$elapsed", true)
        mediaRtcListeners.dispatch {
            it.onRejoinSuccess()
        }
    }

    /**
     * App 调用 leaveChannel 方法时，SDK 提示 App 离开频道成功
     * @param stats: 此次通话的总通话时长、SDK 收发数据的流量等信息
     */
    override fun onLeaveChannel(stats: RtcStats?) {
        super.onLeaveChannel(stats)
        log(
            "onLeaveChannel, leavingChannel:${leavingChannel}, totalDuration:${stats?.totalDuration}, txAudioKBitRate:${stats?.txAudioKBitRate}, rxAudioKBitRate:${stats?.rxAudioKBitRate}, lastmileDelay:${stats?.lastmileDelay}, gatewayRtt:${stats?.gatewayRtt}",
            true
        )
        leaveChannelContinuation?.let { cont ->
            if (cont.isActive) {
                cont.resume(Rlt.Success(Any())) // 成功恢复协程
            }
        }
        leaveChannelContinuation = null // <-- 恢复后，必须清理它

        leavingChannel = null
    }

    /**
     * 直播场景下用户角色已切换回调。如从观众切换为主播，反之亦然
     */
    override fun onClientRoleChanged(
        oldRole: Int,
        newRole: Int,
        newRoleOptions: ClientRoleOptions?
    ) {
        super.onClientRoleChanged(oldRole, newRole, newRoleOptions)
        log("onClientRoleChanged, oldRole$oldRole, newRole:$newRole", true)
    }

    override fun onError(err: Int) {
        super.onError(err)
        Log.e(TAG_MEDIA, "currentChannel:$currentChannel, selfUid:$selfUid, onError, err:$err")
        if (err == ERR_JOIN_CHANNEL_REJECTED) {
            joinChannelCallback?.onJoinFailed(currentChannel, ERR_JOIN_CHANNEL_REJECTED.toString())
        } else if (err == ERR_ADM_INIT_PLAYOUT) {
            // TODO 提示播放设备被占用
        } else if (err == ERR_ADM_GENERAL_ERROR) {
            // TODO 提示录音设备被占用
        }
        mediaRtcListeners.dispatch {
            it.onError(err)
        }
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        super.onUserJoined(uid, elapsed)
        log("onUserJoined, uid$uid, elapsed:$elapsed")
    }

    //远端用户离开频道后，会触发该回调
    override fun onUserOffline(uid: Int, reason: Int) {
        super.onUserOffline(uid, reason)
        log("onUserOffline, uid$uid, reason:$reason")
    }

    /**
     * 播放渠道改变时回调
     * AUDIO_ROUTE_DEFAULT(-1): 默认音频渠道
     * AUDIO_ROUTE_HEADSET(0): 带麦克风的耳机
     * AUDIO_ROUTE_EARPIECE(1): 听筒
     * AUDIO_ROUTE_HEADSETNOMIC(2): 不带麦克风的耳机
     * AUDIO_ROUTE_SPEAKERPHONE(3): 设备自带的扬声器
     * AUDIO_ROUTE_LOUDSPEAKER(4): 外接的扬声器
     * AUDIO_ROUTE_HEADSETBLUETOOTH(5): 蓝牙耳机
     */
    override fun onAudioRouteChanged(routing: Int) {
        super.onAudioRouteChanged(routing)
        log("onAudioRouteChanged, routing:$routing")
        mediaRtcListeners.dispatch {
            it.onAudioRouteChanged(AudioRouter.getAudioRouterByValue(routing))
        }
    }

    /**
     * Token 服务即将过期回调，SDK 会提前 30 秒触发该回调，提醒 App 更新 Token
     */
    override fun onTokenPrivilegeWillExpire(token: String?) {
        super.onTokenPrivilegeWillExpire(token)
        log(" onTokenPrivilegeWillExpire, token:$token")
        launch(Dispatcher.HIGH_SERIAL) {
            val newToken = getToken()
            if (newToken.isNullOrEmpty()) {
                log(" onTokenPrivilegeWillExpire, newToken is null", true)
                return@launch
            }

            withContext(Dispatcher.UI) {
                log(" onTokenPrivilegeWillExpire, token:${token}", true)
                getRtcEngine().renewToken(newToken)
            }
        }
    }

    private suspend fun getToken(): String? {
        if (currentChannel.isNullOrEmpty()) {
            log(" getToken, currentChannel is null", true)
            return null
        }

        val result = config.getChannelToken(currentChannel!!, getRtcType())
        if (result !is Rlt.Success) {
            log(" getToken, failed", true)
            return null
        }

        return result.data
    }

    /**
     * 在调用 joinChannel 时指定了 Token 失效，重连需要更新token
     */
    override fun onRequestToken() {
        super.onRequestToken()
        log(" onRequestToken")
        launch(Dispatcher.HIGH_SERIAL) {
            val token = getToken()
            if (token.isNullOrEmpty()) {
                log(" onRequestToken, token is null", true)
                return@launch
            }

            withContext(Dispatcher.UI) {
                log(" onRequestToken, renewToken, token:${token}", true)
                getRtcEngine().renewToken(token)
            }
        }
    }

    /**
     * 本地音频状态发生改变回调（包括本地麦克风采集状态和音频编码状态）
     */
    override fun onLocalAudioStateChanged(state: Int, error: Int) {
        super.onLocalAudioStateChanged(state, error)
        log("onLocalAudioStateChanged, state:$state, error:$error")
    }

    /**
     * 远端音频状态发生改变回调
     */
    override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
        log("onRemoteAudioStateChanged, uid:$uid, state:$state, reason:$reason, elapsed:$elapsed")

    }

    /**
     * 默认禁用。可以通过 enableAudioVolumeIndication 方法开启
     * 每次会触发两个 onAudioVolumeIndication 回调，一个报告本地发流用户的音量相关信息，另一个报告瞬时音量最高的远端用户（最多 3 位）的音量相关信息
     * 启用该功能后，如果有用户将自己静音（调用了 muteLocalAudioStream），SDK 行为会受如下影响：
     *  * 本地用户静音后，SDK 立即停止报告本地用户的音量提示回调
     *  * 瞬时音量最高的远端用户静音后 20 秒，远端的音量提示回调中将不再包含该用户；如果远端所有用户都将自己静音，20 秒后 SDK 停止报告远端用户的音量提示回调
     * 可用来暂时判断当前说话成员
     */
    private val speakingLock = Any()

    private var lastDispatchTime = 0L
    private var lastStableChangeTime = 0L

    private val TAG = "Speaking Volume"

    private val VOLUME_THRESHOLD = 12
    private val STABLE_WINDOW = 150L
    private val MIN_DISPATCH_INTERVAL = 400L
    private var pendingUidSet: Set<Int> = emptySet()

    override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
        val now = System.currentTimeMillis()
        val currentUids = HashSet<Int>()

        speakers?.forEach { info ->
            if (info != null && info.volume > VOLUME_THRESHOLD) {
                val uid = if (info.uid == 0) selfUid else info.uid
                currentUids.add(uid)
            }
        }

        var dispatchSet: HashSet<Int>? = null
        var dispatchReason: String? = null

        synchronized(speakingLock) {
            if (currentUids != pendingUidSet) {
                pendingUidSet = currentUids
                lastStableChangeTime = now
            }

            val timeSinceLastDispatch = now - lastDispatchTime
            val timeSinceStable = now - lastStableChangeTime

            val shouldDispatch = when {

                // ✅【新增】首次有人说话：必须立刻亮灯
                speakingUidSet.isEmpty() && pendingUidSet.isNotEmpty() -> {
                    dispatchReason = "first speaker"
                    true
                }

                // 全部静音：立即熄灯
                pendingUidSet.isEmpty() && speakingUidSet.isNotEmpty() -> {
                    dispatchReason = "all silent"
                    true
                }

                // 稳定变化
                pendingUidSet != speakingUidSet &&
                        timeSinceStable >= STABLE_WINDOW &&
                        timeSinceLastDispatch >= MIN_DISPATCH_INTERVAL -> {
                    dispatchReason = "stable change (${timeSinceStable}ms)"
                    true
                }

                // keep alive
                pendingUidSet.isNotEmpty() &&
                        pendingUidSet == speakingUidSet &&
                        timeSinceLastDispatch >= MIN_DISPATCH_INTERVAL -> {
                    dispatchReason = "keep alive"
                    true
                }

                else -> false
            }


            if (shouldDispatch) {
                speakingUidSet.clear()
                speakingUidSet.addAll(pendingUidSet)
                lastDispatchTime = now
                dispatchSet = HashSet(speakingUidSet)
            }
        }

        // 4. 锁外分发，避免阻塞 RTC 线程
        dispatchSet?.let { set ->
            Log.d(TAG, "🚀 dispatch: $set | reason: $dispatchReason")
            mediaRtcListeners.dispatch { it.onUsersSpeaking(set) }
        }
    }

    /**
     * 媒体引擎成功加载的回调
     */
    override fun onMediaEngineLoadSuccess() {
        super.onMediaEngineLoadSuccess()
        log(" onMediaEngineLoadSuccess")
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
        super.onRemoteAudioStats(stats)
        VoiceRoomLagMonitor.onAudioStatsUpdate(
            frozenRate = stats?.frozenRate?.toDouble() ?: return,
            rtt = stats?.networkTransportDelay ?: return
        )

    }

    /**
     * 媒体引擎成功启动的回调
     */
    override fun onMediaEngineStartCallSuccess() {
        super.onMediaEngineStartCallSuccess()
        log(" onMediaEngineStartCallSuccess")
    }

    /**
     * 本地网络类型发生改变回调
     */
    override fun onNetworkTypeChanged(type: Int) {
        super.onNetworkTypeChanged(type)
        log(" onNetworkTypeChanged, type:$type", true)
    }

    override fun getCurrentChannel(): String? {
        return currentChannel
    }

    override fun setAudioProfile(profile: AudioProfile, scenario: AudioScenario) {
        getRtcEngine().setAudioProfile(profile.value, scenario.value)
    }

    override fun setChannelProfile(profile: ChannelProfile) {
        log("setChannelProfile, profile:$profile", true)
        getRtcEngine().setChannelProfile(profile.profile)
        if (profile == ChannelProfile.LIVE_BROADCASTING) {
            val rtcType = getRtcType()
            getRtcEngine().enableAudioVolumeIndication(
                if (rtcType == RtcType.T_RTC || rtcType == RtcType.ZEGO_RTC) 300 else 100,
                3,
                false
            )
        } else {
            getRtcEngine().enableAudioVolumeIndication(0, 3, false)
        }
    }

    private val joinChannelTimeout by lazy {
        Runnable {
            joinChannelCallback?.onJoinFailed(currentChannel, "timeout")
        }
    }

    private fun cancelJoinChannelTimeout() {
        Dispatcher.removeFromHighSerialThread(joinChannelTimeout)
    }

    /**
     * 加入频道
     * @param channelName: 标识通话的频道名称，长度在 64 字节以内的字符串
     */
    override suspend fun joinChannel(channelName: String, token: String, uid: Int): Rlt<Any> {
        return withContext(this.coroutineContext) {
            if (channelName.isEmpty()) {
                return@withContext Rlt.Failed(MediaChannelNameNull())
            }

            if (token.isEmpty()) {
                return@withContext Rlt.Failed(MediaTokenNull())
            }

            if (channelName == leavingChannel) {
                leavingChannel = null
            }

//        if (getRtcType() != RtcType.T_RTC) {
//            cancelJoinChannelTimeout()
//            Dispatcher.highExecutor.submit(joinChannelTimeout, JOIN_CHANNEL_TIMEOUT)
//        }
            return@withContext suspendCancellableCoroutine<Rlt<Any>> { continuation ->
                currentChannel = channelName
                selfUid = uid
                joinChannelCallback = object : OnJoinChannelCallback {

                    var resumed = false

                    override fun onJoinSuccess() {
                        cancelJoinChannelTimeout()
                        // isActive在独立dispatcher分发，有延迟问题
                        if (continuation.isActive && !resumed) {
                            resumed = true
                            continuation.resume(Rlt.Success(Any()))
                        }
                    }

                    override fun onJoinFailed(channel: String?, errCode: String) {
                        cancelJoinChannelTimeout()
                        // isActive在独立dispatcher分发，有延迟问题
                        if (currentChannel == channel) {
                            currentChannel = null
                            selfUid = 0
                        }
                        if (continuation.isActive && !resumed) {
                            resumed = true
                            continuation.resume(Rlt.Failed(JoinChannelError(errCode)))
                        }
                    }

                }
                getRtcEngine().joinChannel(token, channelName, "", uid)
            }
        }
    }

    private var leaveChannelContinuation: CancellableContinuation<Rlt<Any>>? = null

    /**
     * 调用 joinChannel 后，必须调用 leaveChannel 结束通话，否则无法开始下一次通话
     * 不管当前是否在通话中，都可以调用 leaveChannel，没有副作用
     */
    override suspend fun leaveChannel(): Rlt<Any> {
        // ⚠️ 核心修改：设置 1000 毫秒（1秒）的超时保护
        val result = withTimeoutOrNull(5000L) {

            suspendCancellableCoroutine<Rlt<Any>> { continuation ->

                log("leaveChannel starting suspend block (1000ms timeout)", true)

                // 1. 设置状态和 Continuation
                leaveChannelContinuation = continuation
                leavingChannel = currentChannel
                currentChannel = null // 清理当前频道状态
                selfUid = 0           // 清理 UID
                speakingUidSet.clear() // 清理说话列表

                // 2. 调用声网 API
                val rtcResult = getRtcEngine().leaveChannel()

                if (rtcResult != 0) {
                    // 如果 API 调用本身失败，立即失败并清理
                    leaveChannelContinuation = null
                    continuation.resume(Rlt.Failed(JoinChannelError("Agora leaveChannel API failed: $rtcResult")))
                    return@suspendCancellableCoroutine
                }

                // 3. 设置协程取消回调 (清理状态)
                continuation.invokeOnCancellation {
                    leaveChannelContinuation = null
                }
                // 协程在这里挂起，直到 onLeaveChannel 回调恢复它。
            }
        }

        // 处理超时情况
        return result ?: run {
            log("leaveChannel TIMEOUT after 1000ms! Forcing local cleanup and proceeding.", true)

            // 即使超时，也必须清理挂起的 Continuation，防止内存泄漏。
            leaveChannelContinuation = null

            // 返回失败结果，但允许上层流程继续，确保零延时。
            Rlt.Failed(JoinChannelError("RTC leaveChannel timeout (1000ms)"))
        }
    }

    override fun isChannelJoined(): Boolean {
        return currentChannel.isNullOrEmpty().not()
    }

    override fun enableLocalAudio(enabled: Boolean) {
        getRtcEngine().enableLocalAudio(enabled)
    }

    override fun muteLocalAudioStream(mute: Boolean) {
        getRtcEngine().muteLocalAudioStream(mute)
        enableLocalAudio(!mute)
    }

    override fun setEnableSpeakerphone(enable: Boolean) {
        log("setEnableSpeakerphone, enable:$enable")
        getRtcEngine().setEnableSpeakerphone(enable)
    }

    override fun setEnableEarPhone(enable: Boolean) {
        log("setEnableErphone, enable:$enable")
        setRtcEnableInEarMonitoring(enable)
    }


    fun setRtcEnableInEarMonitoring(enable: Boolean) {
        try {
            getRtcEngine().enableInEarMonitoring(enable)
            if (enable) {
                val volume = 80 // 范围：0~100
                getRtcEngine().setInEarMonitoringVolume(volume)
            } else {
                val volume = 0 // 范围：0~100
                getRtcEngine().setInEarMonitoringVolume(volume)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun muteAllRemoteAudioStreams(mute: Boolean) {
        getRtcEngine().muteAllRemoteAudioStreams(mute)
    }

    override fun isSpeakerphoneEnable(): Boolean {
        return getRtcEngine().isSpeakerphoneEnabled
    }


    override fun adjustRecordingSignalVolume(volume: Int) {
        this.recordingSignalVolume = volume
        getRtcEngine().adjustRecordingSignalVolume(volume)
    }

    override fun getRecordSignalVolume(): Int {
        return recordingSignalVolume
    }

    override fun addMediaRtcListener(l: IMediaRtcListener) {
        mediaRtcListeners.add(l)
    }

    override fun removeMediaRtcListener(l: IMediaRtcListener) {
        mediaRtcListeners.remove(l)
    }

    override fun isMediaUsing(): Boolean {
        return musicPlayState != MediaMusicPlayState.STOP
    }

    override fun destroy() {
        if (agoraRtcEngine != null) {
            synchronized(this) {
                destroyAgoraRtcEngine()
            }
        }

    }

    private var musicPlayState = MediaMusicPlayState.STOP
    private var toPlayMusicFilePath: String? = null
    private var playMusicFilePath: String? = null

    private fun logMusic(msg: String, xlog: Boolean = true) {
        Log.d(
            TAG_MEDIA_MUSIC,
            "currentChannel:$currentChannel, selfUid:$selfUid, playState:$musicPlayState, toPlayMusicFilePath:$toPlayMusicFilePath, playMusicFilePath:$playMusicFilePath, $msg"
        )
    }

    private val musicPlayProgressRunnable by lazy {
        Runnable {
            updateMusicPlayProgress()
        }
    }

    private fun updateMusicPlayProgress() {
        val musicDuration = getMusicDuration()
        val progress = if (musicDuration <= 0) 0F else getMusicPlayPos().toFloat() / musicDuration
        playMusicFilePath?.let { filePath ->
            mediaMusicListeners.dispatch {
                it.onMusicPlayProgress(filePath, progress)
            }
        }

        if (musicPlayState == MediaMusicPlayState.PLAY) {
            runOnUiThread(musicPlayProgressRunnable, 500)
        }
    }

    override fun getMusicPlayState(): MediaMusicPlayState {
        return musicPlayState
    }

    private fun switchMusicPlayState(playState: MediaMusicPlayState, reason: MediaMusicReason) {
        if (musicPlayState == playState) {
            logMusic("switchMusicPlayState, no change, playState:$playState, reason:$reason")
            return
        }

        logMusic("switchMusicPlayState, playState:$playState, reason:$reason")
        val filePath = playMusicFilePath ?: toPlayMusicFilePath
        if (musicPlayState == MediaMusicPlayState.STOP && playState == MediaMusicPlayState.PLAY) {
            playMusicFilePath = toPlayMusicFilePath
        }
        if (playState == MediaMusicPlayState.STOP) {
            playMusicFilePath = null
        }
        musicPlayState = playState
        filePath?.let {
            mediaMusicListeners.dispatch {
                it.onMusicPlayStateChanged(filePath, musicPlayState, reason)
            }
        }
    }

    override fun getMusicFilePath(): String? {
        return playMusicFilePath
    }

    override fun playMusic(
        filePath: String,
        loopback: Boolean,
        replace: Boolean,
        cycle: Int,
    ) {
        logMusic("playMusic, filePath:$filePath, loopback:$loopback, replace:$replace, cycle:$cycle")
        toPlayMusicFilePath = filePath
        getRtcEngine().startAudioMixing(
            filePath,
            loopback,
            1,
            cycle
        ) //如需多次调用 startAudioMixing，请确保调用间隔大于 500 ms
    }

    override fun pauseMusic() {
        logMusic("pauseMusic")
        if (musicPlayState != MediaMusicPlayState.PLAY) {
            return
        }

        getRtcEngine().pauseAudioMixing()
    }

    override fun resumeMusic() {
        logMusic("resumeMusic")
        if (musicPlayState != MediaMusicPlayState.PAUSE) {
            return
        }

        getRtcEngine().resumeAudioMixing()
    }

    override fun stopMusic() {
        logMusic("stopMusic")
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return
        }

        getRtcEngine().stopAudioMixing()
    }

    override fun getMusicDuration(): Int {
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return 0
        }

        return getRtcEngine().audioMixingDuration
    }

    override fun getMusicPlayPos(): Int {
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return 0
        }

        return getRtcEngine().audioMixingCurrentPosition
    }

    override fun setMusicPlayPos(posMS: Int) {
        logMusic("setMusicPlayPos, posMS:$posMS")
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return
        }

        getRtcEngine().setAudioMixingPosition(posMS)
    }

    private var musicVolume: Int = 50
    override fun setMusicVolume(volume: Int) {
        logMusic("setMusicVolume, volume:$volume")
        this.musicVolume = volume
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return
        }

        getRtcEngine().adjustAudioMixingVolume(volume)
    }

    override fun setMusicLocalVolume(volume: Int) {
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return
        }

        getRtcEngine().adjustAudioMixingPlayoutVolume(volume)
    }

    override fun getMusicLocalVolume(): Int {
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return musicVolume
        }

        return getRtcEngine().audioMixingPlayoutVolume
    }

    override fun setMusicRemoteVolume(volume: Int) {
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return
        }

        getRtcEngine().adjustAudioMixingPublishVolume(volume)
    }

    override fun getMusicRemoteVolume(): Int {
        if (musicPlayState == MediaMusicPlayState.STOP) {
            return 0
        }

        return getRtcEngine().audioMixingPublishVolume
    }

    override fun addMediaMusicListener(l: IMediaMusicListener) {
        mediaMusicListeners.add(l)
    }

    override fun removeMediaMusicListener(l: IMediaMusicListener) {
        mediaMusicListeners.remove(l)
    }

    private val soundEffectId = AtomicInteger(1000)

    override fun setSoundEffectVolume(soundId: Int, volume: Int) {
        getRtcEngine().audioEffectManager.setVolumeOfEffect(soundId, volume.toDouble())
    }

    override fun playSoundEffect(filePath: String, loopCount: Int, publish: Boolean): Int {
        val effectId = soundEffectId.incrementAndGet()
        getRtcEngine().audioEffectManager.playEffect(
            effectId,
            filePath,
            loopCount,
            1.0,
            0.0,
            100.0,
            publish,
            0
        )
        return effectId
    }

    override fun pauseSoundEffect(effectId: Int) {
        getRtcEngine().audioEffectManager.pauseEffect(effectId)
    }

    override fun resumeSoundEffect(effectId: Int) {
        getRtcEngine().audioEffectManager.resumeEffect(effectId)
    }

    override fun stopSoundEffect(effectId: Int) {
        getRtcEngine().audioEffectManager.stopEffect(effectId)
    }

    override fun onAudioMixingStateChanged(state: Int, reason: Int) {
        super.onAudioMixingStateChanged(state, reason)
        logMusic("onAudioMixingStateChanged, state:$state, reason:$reason")
        when (state) {
            Constants.AUDIO_MIXING_STATE_PLAYING -> {
                val mediaMusicReason = when (reason) {
                    AUDIO_MIXING_REASON_ONE_LOOP_COMPLETED -> MediaMusicReason.ONE_LOOP_COMPLETED
                    AUDIO_MIXING_REASON_RESUMED_BY_USER -> MediaMusicReason.RESUMED_BY_USER
                    else -> MediaMusicReason.UNKNOWN
                }
                switchMusicPlayState(MediaMusicPlayState.PLAY, mediaMusicReason)
                removeUiCallbacks(musicPlayProgressRunnable)
                runOnUiThread(musicPlayProgressRunnable, 500)
            }

            AUDIO_MIXING_STATE_PAUSED -> {
                val mediaMusicReason = when (reason) {
                    else -> MediaMusicReason.UNKNOWN
                }
                removeUiCallbacks(musicPlayProgressRunnable)
                switchMusicPlayState(MediaMusicPlayState.PAUSE, mediaMusicReason)
            }

            AUDIO_MIXING_STATE_STOPPED -> {
                val mediaMusicReason = when (reason) {
                    AUDIO_MIXING_REASON_ALL_LOOPS_COMPLETED -> MediaMusicReason.ALL_LOOPS_COMPLETED
                    AUDIO_MIXING_REASON_STOPPED_BY_USER -> MediaMusicReason.STOPPED_BY_USER
                    else -> MediaMusicReason.UNKNOWN
                }
                removeUiCallbacks(musicPlayProgressRunnable)
                switchMusicPlayState(MediaMusicPlayState.STOP, mediaMusicReason)
            }

            AUDIO_MIXING_STATE_FAILED -> {
                val mediaMusicReason = when (reason) {
                    AUDIO_MIXING_REASON_CAN_NOT_OPEN -> MediaMusicReason.CAN_NOT_OPEN
                    AUDIO_MIXING_REASON_TOO_FREQUENT_CALL -> MediaMusicReason.TOO_FREQUENT_CALL
                    AUDIO_MIXING_REASON_INTERRUPTED_EOF -> MediaMusicReason.INTERRUPTED_EOF
                    else -> MediaMusicReason.UNKNOWN
                }
                removeUiCallbacks(musicPlayProgressRunnable)
                switchMusicPlayState(MediaMusicPlayState.STOP, mediaMusicReason)
            }
        }
    }

}