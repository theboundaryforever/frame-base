package com.yuehai.data.collection.path.gift

const val SEND_FROM_TYPE = "SEND_FROM_TYPE"
const val SEND_CHAT_PRIVATE_ID = "SEND_CHAT_PRIVATE_ID"
const val SEND_CHAT_PRIVATE_INFO = "SEND_CHAT_PRIVATE_INFO"
const val SEND_USER_PRIVATE_INFO = "SEND_USER_PRIVATE_INFO"

enum class SendGiftFrom(val from: Int) {
    SEND_FROM_ROOM(0),
    SEND_FROM_CHAT(1),
    SEND_FROM_USER(2),
}