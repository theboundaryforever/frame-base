import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.BidiFormatter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.*
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.text.TextUtils

class UltimateSpanBuilder private constructor(private val format: String) {

    private val argsList = mutableListOf<Pair<String, List<Any>>>()

    companion object {
        /** * 增强版正则表达式
         * 捕获组 1: 提取数字编号 (例如从 %1$s 中提取 1)
         */
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

    /** * 核心构建逻辑
     * 适配 RTL 强制方向、支持序号解析、兼容 LTR 混合排版
     */
    fun build(context: Context): CharSequence {
        val builder = SpannableStringBuilder()

        // 1. 获取当前是否为 RTL 布局
        val isRtl = context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        // 2. 在 RTL 环境下，首位插入 RLM (Right-to-Left Mark) 字符
        // 它的作用是强迫系统从右向左排版，即使句子以英文单词(LTR)开头或结尾
        if (isRtl) {
            builder.append("\u200F")
        }

        val matches = placeholderRegex.findAll(format).toList()
        var lastIndex = 0
        var autoIndex = 0

        matches.forEach { match ->
            // 拼接占位符之前的普通文本
            builder.append(format.substring(lastIndex, match.range.first))
            lastIndex = match.range.last + 1

            // 3. 提取占位符编号，实现参数精准投递
            val indexString = match.groups[1]?.value
            val targetIndex = if (indexString != null) {
                indexString.toInt() - 1 // %1$s 对应 argsList[0]
            } else {
                autoIndex++
            }

            // 4. 安全填充参数
            if (targetIndex in argsList.indices) {
                val (argText, spans) = argsList[targetIndex]
                val start = builder.length

                // 细节优化：如果是图片参数且 argText 为空，插入一个微小的空格占位，防止 Span 丢失
                val finalContent = if (argText.isEmpty() && spans.any { it is ImageSpan }) " " else argText
                builder.append(finalContent)

                val end = builder.length
                spans.forEach { span ->
                    builder.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                // 如果没有对应的参数，保留原始占位符
                builder.append(match.value)
            }
        }

        // 拼接剩余文本
        if (lastIndex < format.length) {
            builder.append(format.substring(lastIndex))
        }

        return builder
    }

    /** 配置项 DSL */
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

        /** * 插入图片
         * @param autoMirror 设为 true 时，图片会在 RTL 下自动水平翻转（适合图标，不适合头像）
         */
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

/** * TextView 扩展函数
 * 特别适配跑马灯/自定义 View 的对齐逻辑
 */
fun TextView.setUltimateText(builder: UltimateSpanBuilder) {
    val isRtl = TextUtils.getLayoutDirectionFromLocale(java.util.Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
    val content = builder.build(context)
    this.text = content

    if (isRtl) {
        // 使用 VIEW_START，系统会自动根据 RTL 环境选择右对齐
        this.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        this.gravity = Gravity.CENTER_VERTICAL or Gravity.START
    } else {
        this.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        this.gravity = Gravity.CENTER_VERTICAL or Gravity.START
    }
}