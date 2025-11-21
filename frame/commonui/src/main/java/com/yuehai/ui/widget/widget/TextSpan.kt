package com.yuehai.ui.widget.widget

fun safeSubstring(original: String, lengthShown: Int): String {
    if (original.isEmpty()) return ""

    if (lengthShown <= 0 || original.length <= lengthShown) return original
    val result = try {
        original.substring(
            original.offsetByCodePoints(0, 0),
            original.offsetByCodePoints(0, lengthShown)
        )

    } catch (e: Exception) {
        original
    }

    return result

}


