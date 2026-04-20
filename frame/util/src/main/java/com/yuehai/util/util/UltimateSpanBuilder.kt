package com.yuehai.util.util

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.Gravity
import android.view.View
import android.widget.TextView


class UltimateSpanBuilder private constructor(private val format: String) {

    private val argsList = mutableListOf<Pair<String, List<Any>>>()

    companion object {
        // 增强正则：捕获组 1 用于提取序号数字 (例如 %1$s 中的 1)
        private val placeholderRegex = "%(?:(\\d+)\\$)?s".toRegex()

        /** 开启 Builder 入口 */
        fun of(format: String): UltimateSpanBuilder = UltimateSpanBuilder(format)
    }

    /** 添加参数配置 */
    fun addArg(text: Any, block: ArgConfig.() -> Unit = {}): UltimateSpanBuilder {
        val config = ArgConfig(text.toString())
        config.block()
        argsList.add(config.text to config.spans)
        return this
    }

    /** 核心构建逻辑：支持序号解析，适配 RTL 语序调换 */
    fun build(context: Context): CharSequence {
        val matches = placeholderRegex.findAll(format).toList()
        val builder = SpannableStringBuilder()
        var lastIndex = 0

        // 自动索引：处理那些没有写数字编号的 %s
        var autoIndex = 0

        matches.forEach { match ->
            // 1. 拼接占位符之前的普通文本
            builder.append(format.substring(lastIndex, match.range.first))
            lastIndex = match.range.last + 1

            // 2. 确定该位置对应的参数索引
            // 获取正则第一个捕获组的内容 (即数字)
            val indexString = match.groups[1]?.value
            val targetIndex = if (indexString != null) {
                // 有编号：%1$s -> 索引 0
                indexString.toInt() - 1
            } else {
                // 无编号：%s -> 按出现顺序
                autoIndex++
            }

            if (targetIndex in argsList.indices) {
                val (argText, spans) = argsList[targetIndex]
                val start = builder.length
                builder.append(argText)
                val end = builder.length

                spans.forEach { span ->
                    builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                // 如果参数列表不够，保留原占位符
                builder.append(match.value)
            }
        }

        if (lastIndex < format.length) {
            builder.append(format.substring(lastIndex))
        }
        return builder
    }

    class ArgConfig(val text: String) {
        val spans = mutableListOf<Any>()
        fun color(color: Int) = apply { spans.add(ForegroundColorSpan(color)) }
        fun size(px: Int) = apply { spans.add(AbsoluteSizeSpan(px, false)) }

        fun bold() = apply {
            spans.add(StyleSpan(Typeface.BOLD))
            spans.add(object : CharacterStyle() {
                override fun updateDrawState(tp: TextPaint) { tp.isFakeBoldText = true }
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
            // 头像一般不建议 mirror，但图标可以
            drawable.isAutoMirrored = autoMirror
            spans.add(ImageSpan(drawable, verticalAlignment))
        }
    }
}

/** TextView 扩展函数：适配 RTL 对齐逻辑 */
fun TextView.setUltimateText(builder: UltimateSpanBuilder) {
    // 建议使用更现代的 API 判断 RTL
    val isRtl = TextUtils.getLayoutDirectionFromLocale(java.util.Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL

    this.text = builder.build(context)

    if (isRtl) {
        // RTL 下使用 VIEW_START 配合 TextDirection 效果最稳
        this.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        this.gravity = Gravity.CENTER_VERTICAL or Gravity.START
    } else {
        this.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        this.gravity = Gravity.CENTER_VERTICAL or Gravity.START
    }

    if (this.text is Spanned && (this.text as Spanned).getSpans(0, text.length, ClickableSpan::class.java).isNotEmpty()) {
        this.movementMethod = LinkMovementMethod.getInstance()
        this.highlightColor = android.graphics.Color.TRANSPARENT
    }
}