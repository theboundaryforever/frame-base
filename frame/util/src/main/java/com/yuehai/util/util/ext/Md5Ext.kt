package com.yuehai.util.util.ext

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun md5(content: String): String {
    val hash: ByteArray
    try {
        hash = MessageDigest.getInstance("MD5").digest(content.toByteArray(charset("UTF-8")))
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("NoSuchAlgorithmException", e)
    } catch (e: UnsupportedEncodingException) {
        throw RuntimeException("UnsupportedEncodingException", e)
    }

    val hex = StringBuilder(hash.size * 2)
    for (b in hash) {
        if ((b.toInt() and 0xFF) < 0x10) {
            hex.append("0")
        }
        hex.append(Integer.toHexString(b.toInt() and 0xFF))
    }
    return hex.toString()
}
