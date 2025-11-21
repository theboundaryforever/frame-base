package com.yuehai.ui.widget.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yuehai.ui.widget.ext.safeSetSpan
import androidx.core.graphics.drawable.toDrawable
import com.yuehai.util.util.ext.dp
import androidx.core.graphics.withSave

class CenterImageSpan(drawable: Drawable) :
    ImageSpan(drawable) {
    override fun getSize(
        paint: Paint, text: CharSequence, start: Int, end: Int,
        fm: Paint.FontMetricsInt?,
    ): Int {
        val d = drawable
        val rect = d.bounds
        if (fm != null) {
            val fmPaint = paint.fontMetricsInt
            val fontHeight = fmPaint.bottom - fmPaint.top
            val drHeight = rect.bottom - rect.top
            val top = drHeight / 2 - fontHeight / 4
            val bottom = drHeight / 2 + fontHeight / 4
            fm.ascent = -bottom
            fm.top = -bottom
            fm.bottom = top
            fm.descent = top
        }
        return rect.right
    }

    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint,
    ) {
        val b = drawable
        canvas.withSave {
            val transY: Int = (bottom - top - b.bounds.bottom) / 2 + top
            translate(x, transY.toFloat())
            b.draw(this)
        }
    }
}

fun setTextWithNetworkImageSpan(
    textView: AppCompatTextView,
    startIndex: Int,
    endIndex: Int,
    imageUrl: String,
    spannableString: SpannableString
) {
    val context = textView.context
    Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val drawable = resource.toDrawable(context.resources)
                // 可自定义宽高
                val size = textView.lineHeight
                drawable.setBounds(0, 0, size, size)
                val imageSpan = CenterImageSpan(drawable)

                // 设置 imageSpan 替换中间的空格
                spannableString.safeSetSpan(
                    imageSpan,
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                textView.text = spannableString
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}










