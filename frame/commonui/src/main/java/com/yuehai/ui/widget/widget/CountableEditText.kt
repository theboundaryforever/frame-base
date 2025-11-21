package com.yuehai.ui.widget.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.yuehai.ui.R
import androidx.core.content.withStyledAttributes

class CountableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr), TextWatcher {

    private var maxCount = 0


    private var countListener: OnCountChangeListener? = null

    init {

        context.withStyledAttributes(attrs, R.styleable.CountableEditText, defStyleAttr, 0) {
            maxCount = getInteger(R.styleable.CountableEditText_maxCount, 0)
        }


        addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    private var isTextChanging = false

    override fun afterTextChanged(s: Editable?) {
        if (isTextChanging) {
            return
        }

        s?.let {
            if (maxCount > 0 && it.length > maxCount) {
                isTextChanging = true

                // Use delete and insert to modify the Editable directly, which is more efficient
                it.delete(maxCount, it.length)

                // Setting the selection needs to happen after the deletion
                setSelection(it.length)

                isTextChanging = false
            }
        }

        countListener?.onCountChange(s?.length ?: 0)
    }


    fun setMaxCount(count: Int) {
        this.maxCount = count
    }


    fun getCurrentCount(): Int {
        return text?.length ?: 0
    }


    fun setOnCountChangeListener(listener: OnCountChangeListener) {
        this.countListener = listener
    }

    interface OnCountChangeListener {
        fun onCountChange(count: Int)
    }
}