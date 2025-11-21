package com.yuehai.data.collection.path.chat

import com.google.gson.annotations.SerializedName


//系统未读消息
data class NotificationUnReadCount(
    @SerializedName("system_msg")
    val systemMsg: MsgUnReadCount? = null,
    @SerializedName("event_msg")
    val eventMsg: MsgUnReadCount? = null,
)

data class MsgUnReadCount(
    @SerializedName("unread")
    val unRead: Long = 0,
    @SerializedName("last_msg")
    val lastMsg: String = ""
)

data class SystemMsgInfo(
    @SerializedName("list")
    val msgList: List<MessageNotificationItem> = ArrayList()
)

data class MessageNotificationItem(
    @SerializedName("title")
    val title: String = "",
    @SerializedName("content")
    val content: String = "",
    @SerializedName("img")
    val img: String = "",
    @SerializedName("jump_url")
    val jumpUrl: String = "",
    @SerializedName("time")
    val time: Long = 0
)
