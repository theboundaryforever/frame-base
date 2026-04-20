package com.yuehai.util.util

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.BidiFormatter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.*
import android.view.Gravity
import android.view.View
import android.widget.TextView
import java.util.*

class UltimateSpanBuilder private constructor(private val format: String) {

    private val argsList = mutableListOf<Pair<String, List<Any>>>()

    companion object {
        /** 支持 %s / %1$s */
        private val placeholderRegex = "%(?:(\\d+)\\$)?s".toRegex()

        fun of(format: String): UltimateSpanBuilder = UltimateSpanBuilder(format)
    }

    fun addArg(text: Any, block: ArgConfig.() -> Unit = {}): UltimateSpanBuilder {
        val config = ArgConfig(text.toString())
        config.block()
        argsList.add(config.text to config.spans)
        return this
    }

    /**
     * ✅ 最终构建（RTL + Span + ImageSpan 完整兼容）
     */
    fun build(context: Context): CharSequence {

        val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        val rawBuilder = SpannableStringBuilder()

        val matches = placeholderRegex.findAll(format).toList()
        var lastIndex = 0
        var autoIndex = 0

        matches.forEach { match ->

            // 普通文本
            rawBuilder.append(format.substring(lastIndex, match.range.first))
            lastIndex = match.range.last + 1

            // 参数索引
            val indexString = match.groups[1]?.value
            val targetIndex = if (indexString != null) {
                indexString.toInt() - 1
            } else {
                autoIndex++
            }

            if (targetIndex in argsList.indices) {

                val (argText, spans) = argsList[targetIndex]
                val start = rawBuilder.length

                val isImage = spans.any { it is ImageSpan }


                val finalText = when {
                    isImage -> "\u200E "     // LTR mark + 占位
                    argText.isEmpty() -> " "
                    else -> argText
                }

                rawBuilder.append(finalText)

                val end = rawBuilder.length

                spans.forEach { span ->
                    rawBuilder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            } else {
                rawBuilder.append(match.value)
            }
        }

        if (lastIndex < format.length) {
            rawBuilder.append(format.substring(lastIndex))
        }


        return if (isRtl) {
            BidiFormatter.getInstance(true).unicodeWrap(rawBuilder)
        } else {
            rawBuilder
        }
    }

    class ArgConfig(val text: String) {

        val spans = mutableListOf<Any>()

        fun color(color: Int) = apply {
            spans.add(ForegroundColorSpan(color))
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

        fun clickable(
            color: Int? = null,
            underline: Boolean = false,
            onClick: () -> Unit
        ) = apply {
            spans.add(object : ClickableSpan() {
                override fun onClick(widget: View) = onClick()
                override fun updateDrawState(ds: TextPaint) {
                    color?.let { ds.color = it }
                    ds.isUnderlineText = underline
                }
            })
        }
    }
}

/**
 * ✅ TextView 扩展（适配跑马灯 + RTL）
 */
fun TextView.setUltimateText(builder: UltimateSpanBuilder) {

    val content = builder.build(context)
    this.text = content

    val isRtl = layoutDirection == View.LAYOUT_DIRECTION_RTL

    // ⭐ 统一用 START（系统自动适配 RTL）
    this.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    this.gravity = Gravity.CENTER_VERTICAL or Gravity.START

    // clickable 支持
    if (content is Spanned &&
        content.getSpans(0, content.length, ClickableSpan::class.java).isNotEmpty()
    ) {
        this.movementMethod = android.text.method.LinkMovementMethod.getInstance()
        this.highlightColor = android.graphics.Color.TRANSPARENT
    }
}