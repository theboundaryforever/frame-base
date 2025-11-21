package com.yuehai.ui.widget.ext

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.yuehai.ui.R


fun View.highlightMentions(input: String, context: Context): SpannableString {
    val spannable = SpannableString(input)
    val mentionPattern = Regex("@\\S+?(?=\\s|\$)") // 匹配以 @ 开头，以空格或结尾为结束的昵称

    mentionPattern.findAll(input).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_FFEB3B)), // 自定义颜色
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    return spannable
}



/**
 * 在 EditText 当前光标位置插入 @用户名+空格 的格式
 */
fun EditText.insertMention(username: String) {
    val mentionText = "@$username "
    val start = selectionStart.coerceAtLeast(0)
    val end = selectionEnd.coerceAtLeast(0)
    text?.replace(start, end, mentionText)
}


/**
 * 修正字符串中的 @用户名（@后紧跟中文或英文数字），自动添加空格（若没有）
 */
fun String.fixMentionsWithSpace(): String {
    val pattern = Regex("@([\\w\\u4e00-\\u9fa5]+)(?!\\s)") // 没有空格的 @xxx
    return pattern.replace(this) { match ->
        "${match.value} "
    }
}





