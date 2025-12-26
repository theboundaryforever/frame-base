package com.yuehai.util.util

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.Gravity
import android.view.View
import android.widget.TextView

/**
 * 脑部减压版 UltimateSpanBuilder
 * 适配：RTL阿语、Builder模式、加粗、下划线、斜体、删除线、多图插入、点击事件
 */
class UltimateSpanBuilder private constructor(private val format: String) {

    private val argsList = mutableListOf<Pair<String, List<Any>>>()

    companion object {
        private val placeholderRegex = "%(\\d+\\$)?s".toRegex()

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

    /** 核心构建逻辑 */
    fun build(context: Context): CharSequence {
        val matches = placeholderRegex.findAll(format).toList()
        val builder = SpannableStringBuilder()
        var lastIndex = 0

        matches.forEachIndexed { index, match ->
            builder.append(format.substring(lastIndex, match.range.first))
            lastIndex = match.range.last + 1

            if (index < argsList.size) {
                val (argText, spans) = argsList[index]
                val start = builder.length
                builder.append(argText)
                val end = builder.length

                spans.forEach { span ->
                    builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                builder.append(match.value)
            }
        }

        if (lastIndex < format.length) {
            builder.append(format.substring(lastIndex))
        }
        return builder
    }

    /** 具体的配置项（在这里面加样式） */
    class ArgConfig(val text: String) {
        val spans = mutableListOf<Any>()

        fun color(color: Int) = apply { spans.add(ForegroundColorSpan(color)) }
        fun size(px: Int) = apply { spans.add(AbsoluteSizeSpan(px, false)) }
        fun bgColor(color: Int) = apply { spans.add(BackgroundColorSpan(color)) }

        /** 加粗：双重保险 */
        fun bold() = apply {
            spans.add(StyleSpan(Typeface.BOLD))
            spans.add(object : CharacterStyle() {
                override fun updateDrawState(tp: TextPaint) { tp.isFakeBoldText = true }
            })
        }

        fun underline() = apply { spans.add(UnderlineSpan()) }
        fun italic() = apply { spans.add(StyleSpan(Typeface.ITALIC)) }
        fun strike() = apply { spans.add(StrikethroughSpan()) }

        /** 插入图片（适配阿语镜像） */
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

        /** 点击事件 */
        fun clickable(color: Int? = null, underline: Boolean = false, onClick: () -> Unit) = apply {
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

/** TextView 扩展函数：一键完成设置 + RTL适配 */
fun TextView.setUltimateText(builder: UltimateSpanBuilder) {
    val context = this.context
    val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

    this.text = builder.build(context)

    // 适配 RTL 的对齐
    if (isRtl) {
        this.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        this.gravity = (this.gravity and Gravity.VERTICAL_GRAVITY_MASK) or Gravity.END
    }

    // 自动判断点击
    if (this.text is Spanned && (this.text as Spanned).getSpans(0, text.length, ClickableSpan::class.java).isNotEmpty()) {
        this.movementMethod = LinkMovementMethod.getInstance()
        this.highlightColor = android.graphics.Color.TRANSPARENT
    }
}