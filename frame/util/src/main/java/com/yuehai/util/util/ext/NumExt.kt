package com.yuehai.util.util.ext

import android.annotation.SuppressLint

fun Int.toCompactString(): String {
    return toLong().toCompactString()
}

fun Long.toCompactString(): String {
    return when {
        this >= 1_000_000 -> String.format("%.1fm", this / 1_000_000f)
        this >= 1_000 -> String.format("%.1fk", this / 1_000f)
        else -> this.toString()
    }
}

@SuppressLint("DefaultLocale")
fun Double.toCompactString(): String {
    return when {
        this >= 1_000_000 -> String.format("%.1fm", this / 1_000_000f)
        this >= 1_000 -> String.format("%.1fk", this / 1_000f)
        else -> this.toString()
    }
}

@SuppressLint("DefaultLocale")
fun Float.toCompactString(): String {
    return when {
        this >= 1_000_000 -> String.format("%.1fm", this / 1_000_000f)
        this >= 1_000 -> String.format("%.1fk", this / 1_000f)
        else -> this.toString()
    }
}

fun Int.toTwoDigitString(): String = this.toString().padStart(2, '0')



