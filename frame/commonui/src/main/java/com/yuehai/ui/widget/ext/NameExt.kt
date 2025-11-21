package com.yuehai.ui.widget.ext

import android.graphics.Color
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.yuehai.data.collection.path.parse.Res
import com.yuehai.ui.R
import com.yuehai.util.util.getCompatColor


fun String?.subName(length: Int = 6): String {

    return if (this == null) {
        return ""
    } else
        if (this.length > length) {
            this.take(length) + "..."
        } else {
            this
        }
}

fun String.safeSubstring(startIndex: Int, endIndex: Int): String {
    if (this.isEmpty() || startIndex >= this.length || startIndex >= endIndex) {
        return ""
    }
    val safeStart = startIndex.coerceAtLeast(0)
    val safeEnd = endIndex.coerceAtMost(this.length)
    return if (safeStart < safeEnd) this.substring(safeStart, safeEnd) else ""
}

fun AppCompatTextView.setLevelNameColor(
    level: Int,
    defaultColor: Int = 0
) {
    when (level) {
        3 -> {
            setTextColor(getCompatColor(R.color.color_FF217BEE))
        }

        4 -> {
            setTextColor(getCompatColor(R.color.color_FF8B3EFF))
        }

        5 -> {
            setTextColor(getCompatColor(R.color.color_FFE13A23))
        }

        6 -> {
            setTextColor(getCompatColor(R.color.color_FFEAA22B))

        }

        else -> {
            try {
                if (defaultColor != 0) {
                    setTextColor(defaultColor)
                } else {
                    setTextColor(this.textColors)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
}

fun AppCompatTextView.setLevelChatMsgColor(
    level: Int,
    defaultColor: Int = 0
) {
    when (level) {

        6 -> {
            setTextColor(getCompatColor(R.color.color_FFEAA22B))

        }

        else -> {
            try {
                if (defaultColor != 0) {
                    setTextColor(defaultColor)
                } else {
                    setTextColor(this.textColors)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }
}

fun TextView.setLevelNameColor(level: Int, defaultColor: Int = 0) {
    when (level) {
        3 -> {
            setTextColor(getCompatColor(R.color.color_FF217BEE))
        }

        4 -> {
            setTextColor(getCompatColor(R.color.color_FF8B3EFF))
        }

        5 -> {
            setTextColor(getCompatColor(R.color.color_FFE13A23))
        }

        6 -> {
            setTextColor(getCompatColor(R.color.color_FFEAA22B))

        }

        else -> {
            try {
                if (defaultColor != 0) {
                    setTextColor(defaultColor)
                } else {
                    setTextColor(this.textColors)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}

