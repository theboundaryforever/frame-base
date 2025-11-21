package com.yuehai.ui.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.graphics.withSave
import com.yuehai.ui.R

class PasswordEditTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var passwordLength: Int = 6
        set(value) {
            field = value
            inputList.clear()
            filters = arrayOf(InputFilter.LengthFilter(field))
            requestLayout()
            invalidate()
        }

    private var borderColor: Int = Color.GRAY
    private var borderWidth: Float = 2f
    private var borderRadius: Float = 12f
    private var borderSpacing: Float = 12f
    private var passwordChar: Char = '*'
    private var textColor: Int = Color.BLACK
    private var textSizePx: Float = 48f
    private var boxHeight: Float = 80f
    private var boxWidth = 0f
    private var isPasswordVisible: Boolean = false

    private val inputList = mutableListOf<Char>()

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    var onPasswordComplete: ((String) -> Unit)? = null
    var onPasswordContent: ((String) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.PasswordEditTextView, 0, 0).apply {
            try {
                passwordLength =
                    getInteger(R.styleable.PasswordEditTextView_passwordLength, passwordLength)
                borderColor = getColor(R.styleable.PasswordEditTextView_border_color, borderColor)
                borderWidth =
                    getDimension(R.styleable.PasswordEditTextView_border_width, borderWidth)
                borderRadius =
                    getDimension(R.styleable.PasswordEditTextView_borderRadius, borderRadius)
                borderSpacing =
                    getDimension(R.styleable.PasswordEditTextView_borderSpacing, borderSpacing)
                textColor = getColor(R.styleable.PasswordEditTextView_textColor, textColor)
                textSizePx = getDimension(R.styleable.PasswordEditTextView_textSize, textSizePx)
                boxHeight = getDimension(R.styleable.PasswordEditTextView_boxHeight, boxHeight)
                isPasswordVisible =
                    getBoolean(R.styleable.PasswordEditTextView_isPasswordVisible, false)
                getString(R.styleable.PasswordEditTextView_passwordChar)?.firstOrNull()?.let {
                    passwordChar = it
                }
            } finally {
                recycle()
            }
        }

        filters = arrayOf(InputFilter.LengthFilter(passwordLength))

        boxPaint.color = borderColor
        boxPaint.strokeWidth = borderWidth

        textPaint.color = textColor
        textPaint.textSize = textSizePx

        isCursorVisible = false
        inputType =
            android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        background = null
        gravity = Gravity.CENTER_VERTICAL
        isFocusableInTouchMode = true

        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                inputList.clear()
                s?.forEachIndexed { _, c -> inputList.add(c) }
                if (inputList.size == passwordLength) {
                    onPasswordComplete?.invoke(getPassword())
                }
                if (getPassword().isNotEmpty()) {
                    onPasswordContent?.invoke(getPassword())
                }
                invalidate()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        post {
            requestFocus()
            showKeyboard()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val totalSpacing = borderSpacing * (passwordLength - 1)
        boxWidth = (width - totalSpacing) / passwordLength.toFloat()

        val desiredHeight = boxHeight.toInt()
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            else -> desiredHeight
        }

        boxHeight = height.toFloat()
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            height + paddingTop + paddingBottom
        )
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val leftPadding = paddingLeft.toFloat()
        val topPadding = paddingTop.toFloat()
        val fontMetricsInt = textPaint.fontMetricsInt
        val baseline =
            (boxHeight - fontMetricsInt.bottom + fontMetricsInt.top) / 2 - fontMetricsInt.top

        for (i in 0 until passwordLength) {
            val left = leftPadding + i * (boxWidth + borderSpacing)
            val rect = RectF(
                left + borderWidth / 2,
                topPadding + borderWidth / 2,
                left + boxWidth - borderWidth / 2,
                topPadding + boxHeight - borderWidth / 2
            )

            canvas.drawRoundRect(rect, borderRadius, borderRadius, boxPaint)

            if (i < inputList.size) {
                val displayChar =
                    if (isPasswordVisible) inputList[i].toString() else passwordChar.toString()
                canvas.withSave {
                    drawText(
                        displayChar,
                        left + boxWidth / 2,
                        topPadding + baseline,
                        textPaint
                    )
                }
            }
        }
    }

    fun getPassword(): String = inputList.joinToString("")

    fun clearInput() {
        setText("")
        inputList.clear()
        invalidate()
    }

    fun setPasswordVisible(visible: Boolean) {
        isPasswordVisible = visible
        invalidate()
    }

    private fun showKeyboard() {
        postDelayed({
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        requestFocus()
        showKeyboard()
        return super.onTouchEvent(event)
    }
}
