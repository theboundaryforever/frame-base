package com.yuehai.data.collection.path.login

enum class LoginType(val type: Int) {
    LOGIN_TYPE_GOOGLE(1),
    LOGIN_TYPE_FACEBOOK(2),
    LOGIN_TYPE_PHONE(3);

    companion object {
        fun fromType(type: Int): LoginType? {
            return LoginType.entries.find { it.type == type }
        }
    }
}