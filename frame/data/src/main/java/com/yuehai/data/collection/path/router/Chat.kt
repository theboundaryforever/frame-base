package com.yuehai.data.collection.path.router




interface Chat {

    interface Chat {

        companion object {
            const val PATH = "/chat"
            const val CHAT_USER_ID = "chatId"
        }

    }
    interface IMConversation {

        companion object {
            const val PATH = "/IMConversation"
            const val MESSAGE_INFO_PATH = "/ImessageInfo"
        }


    }
    interface ChatNotification {

        companion object {
            const val SYSTEM_MSG_PATH = "/SystemNotificationMsg"
            const val EVENT_MSG_PATH = "/EventNotificationMsg"
            const val CHAT_USER_ID = "CHAT_USER_ID"
        }

    }

}