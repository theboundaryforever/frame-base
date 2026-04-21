package com.yuehai.util.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withSave
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlin.apply

class CenterImageSpan(drawable: Drawable) : ImageSpan(drawable) {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val rect = drawable.bounds
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
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
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
    val targetSize = textView.lineHeight

    Glide.with(context)
        .asBitmap()
        .load(imageUrl)
        .override(targetSize, targetSize)
        .centerInside()
        .into(object : CustomTarget<Bitmap>() {

            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                val drawable = BitmapDrawable(context.resources, resource).apply {
                    setBounds(0, 0, targetSize, targetSize)
                }

                val imageSpan = CenterImageSpan(drawable)

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
