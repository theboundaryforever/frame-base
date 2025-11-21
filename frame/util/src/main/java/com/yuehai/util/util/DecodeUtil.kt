package com.yuehai.util.util

object HexUtils {
    @JvmStatic
    fun bytesToHex(bytes: ByteArray, upperCase: Boolean = false): String {
        return bytes.joinToString("") { if (upperCase) "%02X".format(it) else "%02x".format(it) }
    }
}
