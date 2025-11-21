package com.yuehai.data.collection.path.router


interface Room {

    interface Room {

        companion object {
            const val PATH = "/room"
            const val EXTRA_ENTER_ROOM_INFO = "extra_enter_room_info"
            const val EXTRA_TOKEN = "extra_token"
            const val EXTRA_RTC_TOKEN = "extra_rtc_token"
            const val EXTRA_CHANNEL_ID = "extra_channel_id"
            const val EXTRA_UID = "extra_uid"
            const val EXTRA_FROM = "from"
            const val EXTRA_ROOM_INFO = "extra_room_info"
            const val EXTRA_ROOM_TYPE = "extra_room_type"
        }

    }

    interface RoomTheme {

        companion object {
            const val PATH = "/roomTheme"
            const val EXTRA_ENTER_ROOM_INFO = "extra_enter_room_info"
            const val EXTRA_TOKEN = "extra_token"
            const val EXTRA_RTC_TOKEN = "extra_rtc_token"
            const val EXTRA_CHANNEL_ID = "extra_channel_id"
            const val EXTRA_UID = "extra_uid"
            const val THEME_INTRODUCE = "extra_theme_introduce"
            const val EXTRA_ROOM_INFO = "extra_room_info"
            const val EXTRA_ROOM_TYPE = "extra_room_type"
        }

    }

    interface RoomSet {

        companion object {
            const val PATH = "/setRoom"
        }

    }

    interface RoomReport {

        companion object {
            const val PATH = "/reportRoom"
        }

    }

    interface Interceptor {

        companion object {

            const val ROOM_INTERCEPTOR = "room_interceptor"


        }

    }

    interface CreateRoom {

        companion object {
            const val PATH = "/create/room"

        }

    }

    interface RoomOnLineUser {

        companion object {
            const val PATH = "/RoomOnLineUser"

        }

    }

    interface RoomUserInfo {
        companion object {
            const val PATH = "/RoomUserInfo"
            const val EXTRA_USER = "ROOM_USER_INFO"

        }
    }

    interface RoomGameInfo {
        companion object {
            const val PATH = "/RoomGameInfo"

        }
    }

    interface RoomInfo {

        companion object {
            const val PATH = "/RoomInfo"
        }

    }

    interface UserPermission {

        companion object {
            const val PATH = "/UserPermission"

        }
    }


    interface RoomBackground {

        companion object {
            const val PATH = "/RoomBackground"

        }
    }

    interface RocketGame {

        companion object {
            const val PATH = "/RocketGame"
            const val EXTRA_VOICE_UID = "EXTRA_VOICE_UID"

            const val EXTRA_VOICE_ROCKET_RECORD_PERMISSION = "EXTRA_VOICE_ROCKET_RECORD_PERMISSION"
        }

    }


}