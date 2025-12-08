package com.yuehai.util.util

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView

object UltimateSpanBuilder {

    private val placeholderRegex = "%(\\d+\\$)?s".toRegex()

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

        /** ✅ 修复加粗无效：使用 StyleSpan + FakeBold 双保险 */
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
    }

    fun args(text: Any, block: ArgBuilder.() -> Unit = {}): Pair<String, List<Any>> {
        val builder = ArgBuilder(text.toString())
        builder.block()
        return builder.text to builder.spans
    }

    fun build(format: String, vararg args: Pair<String, List<Any>>): CharSequence {
        val matches = placeholderRegex.findAll(format).toList()
        val builder = SpannableStringBuilder()

        var lastIndex = 0

        matches.forEachIndexed { index, match ->
            builder.append(format.substring(lastIndex, match.range.first))
            lastIndex = match.range.last + 1

            val (argText, spans) = args[index]

            val start = builder.length
            builder.append(argText)
            val end = builder.length

            spans.forEach { span ->
                builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        if (lastIndex < format.length) {
            builder.append(format.substring(lastIndex))
        }

        return builder
    }
}

/**
 * TextView 扩展函数
 */
fun TextView.setSpanText(format: String, vararg args: Pair<String, List<Any>>) {
    val result = UltimateSpanBuilder.build(format, *args)
    this.text = result
    this.movementMethod = LinkMovementMethod.getInstance() // 支持点击 span
}
