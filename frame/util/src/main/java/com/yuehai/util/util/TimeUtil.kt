package com.yuehai.util.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt


const val ONE_DAY = 24 * 60 * 60 * 1000L
const val ONE_DAY_SECOND = 24 * 60 * 60
const val ONE_HOUR = 60 * 60 * 1000L
const val ONE_MINUTE = 60 * 1000L
const val ONE_SECOND = 1000L
const val ONE_MONTH = 30 * ONE_DAY

enum class FormatPattern(val pattern: String) {
    YYYYMM("yyyyMM"),
    YYYY_MM("yyyy-MM"),
}

fun getRemainDay(timestamp: Long): Int {
    return ceil(timestamp.toFloat() / ONE_DAY).toInt()
}

fun getRemainHours(timestamp: Long): Int {
    return ceil(timestamp.toFloat() / ONE_HOUR).toInt()
}

fun getRemainMinute(timestamp: Long): Int {
    return ceil(timestamp.toFloat() / ONE_MINUTE).toInt()
}

@SuppressLint("SimpleDateFormat")
fun timeToYMDHMS(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToYMDHM(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToYMD(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd").format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToYMD2(timestamp: Long): String {
    return SimpleDateFormat("yyyy/MM/dd").format(Date(timestamp))
}
@SuppressLint("SimpleDateFormat")
fun timeToYMD3(timestamp: Long): String {
    return SimpleDateFormat("yyyy.MM.dd").format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToMD(timestamp: Long): String {
    return SimpleDateFormat("MM-dd").format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToHM(timestamp: Long): String {
    return SimpleDateFormat("HH:mm").format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToMS(timestamp: Long): String {
    return SimpleDateFormat("mm:ss").format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToEnYMDHMS(timestamp: Long): String {
    return SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.ENGLISH).format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun dateToEnYMDHMS(date: Date): String {
    return SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.ENGLISH).format(date)
}

@SuppressLint("SimpleDateFormat")
fun timeToEnHM(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToEnMDHM(timestamp: Long): String {
    return SimpleDateFormat("MM.dd HH:mm", Locale.ENGLISH).format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToEnMDHM2(timestamp: Long): String {
    return SimpleDateFormat("MMddHHmm", Locale.ENGLISH).format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToEnYMD(timestamp: Long): String {
    return SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH).format(Date(timestamp))
}

@SuppressLint("SimpleDateFormat")
fun timeToEnYYMMDD(timestamp: Long): String {
    return SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).format(Date(timestamp))
}

fun timeToHMSUnitS(timeS: Long): String {
    return timeToHMS(timeS * 1000)
}

@SuppressLint("SimpleDateFormat")
fun timeToHMS(timestamp: Long): String {
    val hours = timestamp / ONE_HOUR
    val leftMS = timestamp % ONE_HOUR
    val minutes = leftMS / ONE_MINUTE
    val seconds = (leftMS % ONE_MINUTE) / ONE_SECOND

    fun formatTime(time: Long): String {
        return when (time < 10) {
            true -> "0$time"
            else -> "$time"
        }
    }
    return "${formatTime(hours)}:${formatTime(minutes)}:${formatTime(seconds)}"
}

@SuppressLint("SimpleDateFormat")
fun timeToShortHMS(timestamp: Long): String {
    val hours = timestamp / ONE_HOUR
    val leftMS = timestamp % ONE_HOUR
    val minutes = leftMS / ONE_MINUTE
    val seconds = (leftMS % ONE_MINUTE) / ONE_SECOND

    fun formatTime(time: Long): String {
        return when (time < 10) {
            true -> "0$time"
            else -> "$time"
        }
    }

    val sb = StringBuilder()
    if (hours > 0) {
        sb.append(formatTime(hours)).append(":")
    }
    sb.append(formatTime(minutes)).append(":").append(formatTime(seconds))
    return sb.toString()
}

fun getMonthNum(timestamp: Long): Int {
    return (timestamp / ONE_MONTH).toInt()
}

fun getTodayStartTimestamp(): Long {
    val startDate = getDayStartTime(Date())
    return startDate.time
}

/**
 * 获取一天的开始时间
 *
 * @return 开始时间
 */
fun getDayStartTime(day: Date): Date {
    val dateStart = Calendar.getInstance()
    dateStart.time = day
    dateStart[Calendar.HOUR_OF_DAY] = 0
    dateStart[Calendar.MINUTE] = 0
    dateStart[Calendar.SECOND] = 0
    dateStart[Calendar.MILLISECOND] = 0
    return dateStart.time
}

/**
 * 获取一天的结束时间
 *
 * @return 结束时间
 */
fun getDayEndTime(day: Date, timeZone: TimeZone): Date {
    val dateEnd = Calendar.getInstance()
    dateEnd.timeZone = timeZone
    dateEnd.time = day
    dateEnd[Calendar.HOUR_OF_DAY] = 23
    dateEnd[Calendar.MINUTE] = 59
    dateEnd[Calendar.SECOND] = 59
    dateEnd[Calendar.MILLISECOND] = 999
    return dateEnd.time
}

/**
 * 获取未来7天内的日期数组,包含startDay(yyy-MM-dd)
 * @param intervals   intervals天内
 * @return            日期数组
 */
fun getFutureDays(startTimestamp: Long, intervals: Int): ArrayList<String> {
    val futureDaysList: ArrayList<String> = ArrayList()
    for (i in 0 until intervals) {
        futureDaysList.add(getFutureDate(startTimestamp, i))
    }
    return futureDaysList
}

/**
 * 获取未来第几天的日期(yyy-MM-dd)
 * @param future
 * @return
 */
@SuppressLint("SimpleDateFormat")
fun getFutureDate(startTimestamp: Long, future: Int): String {
    val calendar = Calendar.getInstance()
    calendar.time = Date(startTimestamp)
    calendar[Calendar.DAY_OF_YEAR] = calendar[Calendar.DAY_OF_YEAR] + future
    val today = calendar.time
    val format = SimpleDateFormat("yyyy-MM-dd")
    return format.format(today)
}

fun differentDays(startDate: Date, endDate: Date): Int {
    val startCalender = Calendar.getInstance()
    startCalender.time = startDate
    val endCalender = Calendar.getInstance()
    endCalender.time = endDate
    val startDay = startCalender[Calendar.DAY_OF_YEAR]
    val endDay = endCalender[Calendar.DAY_OF_YEAR]
    val startYear = startCalender[Calendar.YEAR]
    val endYear = endCalender[Calendar.YEAR]
    return if (startYear != endYear) {
        var timeDistance = 0
        for (i in startYear until endYear) {
            timeDistance += if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
                366
            } else {
                365
            }
        }
        timeDistance + (endDay - startDay)
    } else {
        endDay - startDay
    }
}

@SuppressLint("SimpleDateFormat")
fun timeToRemainEnYMDHMS(timestamp: Long): String {
    val day = timestamp / ONE_DAY
    val hours = if (day > 0) {
        (timestamp % (day * ONE_DAY) / ONE_HOUR)
    } else {
        (timestamp / ONE_HOUR)
    }
    val leftMS = if (day > 0) {
        timestamp % (day * ONE_DAY) % ONE_HOUR
    } else {
        timestamp % ONE_HOUR
    }
    val minutes = leftMS / ONE_MINUTE
    val seconds = (leftMS % ONE_MINUTE) / ONE_SECOND

    fun formatTime(time: Long): String {
        return when (time < 10) {
            true -> "0$time"
            else -> "$time"
        }
    }

    val sb = StringBuilder()
    sb.append(day).append(":")
    sb.append(formatTime(hours)).append(":")
    sb.append(formatTime(minutes)).append(":").append(formatTime(seconds))
    return sb.toString()

}

fun isWithinTwoDays(registerTime: Long): Boolean {
    val twoDaysInMillis = 2 * 24 * 60 * 60 * 1000 // 两天的毫秒数
    return System.currentTimeMillis() - registerTime <= twoDaysInMillis
}

fun formatTime(ts: Long, pattern: FormatPattern): String {
    return SimpleDateFormat(pattern.pattern, Locale.ENGLISH).format(Date(ts))
}

@SuppressLint("SimpleDateFormat")
fun timeToEnYM(timestamp: Long): String {
    return SimpleDateFormat("yyyy.MM", Locale.ENGLISH).format(Date(timestamp))
}

fun formatRemainingTime(millis: Long): String {
    if (millis <= 0) return "0d 0h"
    val totalHours = millis / (1000 * 60 * 60)
    val days = totalHours / 24
    val hours = totalHours % 24
    return "${days}d ${hours}h"
}

fun notificationTimeConvert(timeMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timeMillis }

    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val sdfFull = SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault())

    return when {
        isSameDay(now, target) -> {
            sdfTime.format(target.time)
        }

        isYesterday(now, target) -> {
            "yesterday ${sdfTime.format(target.time)}"
        }

        else -> {
            sdfFull.format(target.time)
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
            && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(now: Calendar, target: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        timeInMillis = now.timeInMillis
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, target)
}

fun formatDurationCompact(totalSeconds: Long): String {
    var remainder = totalSeconds
    val days = remainder / (24 * 3600)
    remainder %= (24 * 3600)
    val hours = remainder / 3600
    remainder %= 3600
    val minutes = remainder / 60
    val seconds = remainder % 60

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours.toString().padStart(2, '0')}h ")
        append("${minutes.toString().padStart(2, '0')}m ")
        append("${seconds.toString().padStart(2, '0')}s")
    }.trim()
}

fun formatAudioDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}


fun formatMicDuration(timeMillis: Long, isMillis: Boolean = true): MicDurationResult {
    val totalSeconds = if (isMillis) timeMillis / 1000 else timeMillis
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60

    val result = buildString {
        if (days > 0) append("${days}")
        if (hours > 0) append("${hours}:")
        if (minutes > 0) append("${minutes}")
        if (isEmpty()) append("0") // 全部为0的情况
    }

    val lessThan60Minutes = days == 0L && hours == 0L && minutes < 60

    return MicDurationResult(
        formatted = result,
        lessThan60Minutes = lessThan60Minutes
    )
}

fun secondsToMinutesAndSeconds(seconds: Long): Pair<Int, Int> {
    // 计算总分钟数
    val minutes = (seconds / 60).toInt()
    // 计算剩余的秒数
    val remainingSeconds = (seconds % 60).toInt()

    return Pair(minutes, remainingSeconds)
}

data class MicDurationResult(
    val formatted: String,
    val lessThan60Minutes: Boolean
)

/**
 * 将秒数转换为 "mm:ss" 格式的字符串
 */
@SuppressLint("DefaultLocale")
fun formatSecondsToMinutesWithSecond(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

fun formatSecondsToMinutes(seconds: Long): Int {
    return (seconds / 60.0).roundToInt()
}
@SuppressLint("DefaultLocale")
fun formatSecondsToHMS(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}






