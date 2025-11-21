package com.yuehai.ui.widget.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.*
import com.yuehai.ui.R
import com.yuehai.util.util.getCompatString

class CountDownButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs), LifecycleObserver {

    private var totalSeconds = 60
    private var normalText = getCompatString(R.string.common_get_verfiy_code)
    private var suffix = "秒后重试"

    private var normalColor = Color.WHITE
    private var disabledColor = Color.GRAY

    private var normalBackgroundColor = Color.parseColor("#FF6200EE")
    private var countingBackgroundColor = Color.LTGRAY
    private var cornerRadius = 8f.dp()

    private var normalBackgroundDrawable: Drawable? = null
    private var countingBackgroundDrawable: Drawable? = null

    private var isCounting = false
    private var viewModel: CountDownViewModel? = null
    private var lifecycleOwner: LifecycleOwner? = null

    init {
        isClickable = true
        isEnabled = true

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.CountDownButton) {
                normalColor = getColor(R.styleable.CountDownButton_normalTextColor, normalColor)
                disabledColor = getColor(R.styleable.CountDownButton_countingTextColor, disabledColor)
                normalBackgroundColor = getColor(R.styleable.CountDownButton_normalBackgroundColor, normalBackgroundColor)
                countingBackgroundColor = getColor(R.styleable.CountDownButton_countingBackgroundColor, countingBackgroundColor)
                cornerRadius = getDimension(R.styleable.CountDownButton_roundCornerRadius, cornerRadius)

                val normalBgResId = getResourceId(R.styleable.CountDownButton_normalBackgroundDrawable, 0)
                if (normalBgResId != 0) {
                    normalBackgroundDrawable = ContextCompat.getDrawable(context, normalBgResId)
                }
                val countingBgResId = getResourceId(R.styleable.CountDownButton_countingBackgroundDrawable, 0)
                if (countingBgResId != 0) {
                    countingBackgroundDrawable = ContextCompat.getDrawable(context, countingBgResId)
                }
            }
        }

        // 初始状态
        text = normalText
        updateStateAppearance(false)

        setOnClickListener {
            if (!isCounting) {
                onClickAction?.invoke() // 由外部控制是否 startCountDown
            }
        }
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density

    private fun createGradientDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = this@CountDownButton.cornerRadius
            setColor(color)
        }
    }

    private fun updateStateAppearance(isCounting: Boolean) {
        setTextColor(if (isCounting) disabledColor else normalColor)

        background = when {
            isCounting && countingBackgroundDrawable != null -> countingBackgroundDrawable
            !isCounting && normalBackgroundDrawable != null -> normalBackgroundDrawable
            isCounting -> createGradientDrawable(countingBackgroundColor)
            else -> createGradientDrawable(normalBackgroundColor)
        }
    }

    private var onClickAction: (() -> Unit)? = null
    fun setOnCountdownClickListener(action: () -> Unit) {
        onClickAction = action
    }

    fun bind(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner

        viewModel = ViewModelProvider(lifecycleOwner as ViewModelStoreOwner)[CountDownViewModel::class.java]

        viewModel?.countDownText?.observe(lifecycleOwner) {
            text = it
        }

        viewModel?.isCountingDown?.observe(lifecycleOwner) { counting ->
            isCounting = counting
            isEnabled = !counting
            updateStateAppearance(counting)
        }

        lifecycleOwner.lifecycle.addObserver(this)
    }

    fun startCountDown(seconds: Int = totalSeconds) {
        viewModel?.startCountDown(seconds)
    }

    fun cancelCountDown() {
        viewModel?.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanUp() {
        cancelCountDown()
    }

    class CountDownViewModel : ViewModel() {
        private val _countDownText = MutableLiveData<String>().apply {
            value = getCompatString(R.string.common_get_verfiy_code)
        }
        val countDownText: LiveData<String> = _countDownText

        private val _isCountingDown = MutableLiveData<Boolean>()
        val isCountingDown: LiveData<Boolean> = _isCountingDown

        private var timer: CountDownTimer? = null

        fun startCountDown(totalSeconds: Int = 60) {
            _isCountingDown.value = true
            timer?.cancel()
            timer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val sec = millisUntilFinished / 1000
                    _countDownText.postValue("$sec")
                }

                override fun onFinish() {
                    _countDownText.postValue(getCompatString(R.string.common_verfiy_resend_code))
                    _isCountingDown.postValue(false)
                }
            }.start()
        }

        fun cancel() {
            timer?.cancel()
            _isCountingDown.postValue(false)
            _countDownText.postValue(getCompatString(R.string.common_get_verfiy_code))
        }

        override fun onCleared() {
            super.onCleared()
            cancel()
        }
    }
}
