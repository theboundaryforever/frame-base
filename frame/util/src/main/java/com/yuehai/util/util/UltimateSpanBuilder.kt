package com.yuehai.util.util

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView

class UltimateSpanBuilder private constructor(private val format: String) {

    private val placeholderRegex = "%(\\d+\\$)?s".toRegex()

    /** 唯一数据源 */
    private val argsList = mutableListOf<Pair<String, List<Any>>>()

    companion object {
        fun of(format: String) = UltimateSpanBuilder(format)
    }

    /** =========================
     *  参数构建
     *  ========================= */
    class ArgBuilder(val text: String) {
        val spans = mutableListOf<Any>()

        fun color(color: Int) = apply {
            spans.add(ForegroundColorSpan(color))
        }

        fun bgColor(color: Int) = apply {
            spans.add(BackgroundColorSpan(color))
        }

        fun size(px: Int) = apply {
            spans.add(AbsoluteSizeSpan(px, false))
        }

        fun bold() = apply {
            spans.add(StyleSpan(Typeface.BOLD))
            spans.add(object : CharacterStyle() {
                override fun updateDrawState(tp: TextPaint) {
                    tp.isFakeBoldText = true
                }
            })
        }

        fun italic() = apply {
            spans.add(StyleSpan(Typeface.ITALIC))
        }

        fun underline() = apply {
            spans.add(UnderlineSpan())
        }

        fun strike() = apply {
            spans.add(StrikethroughSpan())
        }

        fun clickable(
            color: Int? = null,
            underline: Boolean = false,
            onClick: () -> Unit
        ) = apply {
            spans.add(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClick()
                }

                override fun updateDrawState(ds: TextPaint) {
                    color?.let { ds.color = it }
                    ds.isUnderlineText = underline
                }
            })
        }

        fun image(
            drawable: Drawable,
            width: Int? = null,
            height: Int? = null,
            autoMirror: Boolean = false,
            verticalAlignment: Int = ImageSpan.ALIGN_CENTER
        ) = apply {
            val w = width ?: drawable.intrinsicWidth
            val h = height ?: drawable.intrinsicHeight
            drawable.setBounds(0, 0, w, h)
            drawable.isAutoMirrored = autoMirror
            spans.add(ImageSpan(drawable, verticalAlignment))
        }
    }

    /** 添加参数 */
    fun addArg(text: Any, block: ArgBuilder.() -> Unit = {}): UltimateSpanBuilder {
        val builder = ArgBuilder(text.toString())
        builder.block()
        argsList.add(builder.text to builder.spans)
        return this
    }

    /** =========================
     *  核心 build（唯一入口）
     *  ========================= */
    fun build(context: Context): CharSequence {

        val matches = placeholderRegex.findAll(format).toList()
        val result = SpannableStringBuilder()

        var lastIndex = 0

        matches.forEachIndexed { index, match ->

            // 普通文本
            result.append(format.substring(lastIndex, match.range.first))
            lastIndex = match.range.last + 1

            // 参数替换
            if (index < argsList.size) {

                val (text, spans) = argsList[index]

                val start = result.length
                result.append(text)
                val end = result.length

                spans.forEach { span ->
                    result.setSpan(
                        span,
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

            } else {
                result.append(match.value)
            }
        }

        // 尾部文本
        if (lastIndex < format.length) {
            result.append(format.substring(lastIndex))
        }

        return result
    }
}

/** =========================
 *  TextView 扩展
 *  ========================= */
fun TextView.setUltimateText(builder: UltimateSpanBuilder) {

    val content = builder.build(context)
    text = content

    // 只处理点击 span
    if (content is Spanned &&
        content.getSpans(0, content.length, ClickableSpan::class.java).isNotEmpty()
    ) {
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = android.graphics.Color.TRANSPARENT
    }
}