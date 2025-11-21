package com.yuehai.util.util.ext

import java.security.MessageDigest

fun String.safeFileName(): String {
    val digest = MessageDigest.getInstance("MD5").digest(toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
}