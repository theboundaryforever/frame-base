package com.yuehai.ui.widget.ext

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.Interpolator
import androidx.appcompat.app.AppCompatActivity

fun View.getActivity(): AppCompatActivity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.show(isShow: Boolean) {
    visibility = if (isShow) View.VISIBLE else View.GONE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.setVisible(show: Boolean) {
    if (show) show() else gone()
}

/**
 * 连续点击控件
 * 注意: 该方法与onClick方法冲突
 * 参考: https://cloud.tencent.com/developer/beta/article/1741434
 * @param maxClickCount 连点的次数, 达到该次数时回调callback
 * @param clickInterval 两次点击之间的时间间隔
 */
fun View.setContinueClicksListener(
    maxClickCount: Int = 10,
    clickInterval: Long = 300L,
    callback: (view: View) -> Unit
) {
    val clickDuration = maxClickCount * clickInterval
    val hits = LongArray(maxClickCount)

    setOnClickListener {
        /**
         * 实现双击方法
         * src 拷贝的源数组
         * srcPos 从源数组的那个位置开始拷贝.
         * dst 目标数组
         * dstPos 从目标数组的那个位子开始写数据
         * length 拷贝的元素的个数
         */
        System.arraycopy(hits, 1, hits, 0, hits.size - 1)
        //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
        hits[hits.size - 1] = SystemClock.uptimeMillis()
        if (hits[0] >= (SystemClock.uptimeMillis() - clickDuration)) {
            callback.invoke(this)
            //触发连点后就清空连点记录
            for (i in hits.indices) {
                hits[i] = 0
            }
        }
    }
}

/**
 * 摇晃
 * @param shakeDegrees 晃动角度
 * @param shakeCount duration时间内晃动的次数
 * @param repeatCount 重复次数
 * @param duration 晃动时间
 * @param idleDuration 晃动间隙
 */
fun View.shake(shakeDegrees: Float, shakeCount: Float, repeatCount: Int, duration: Long, idleDuration: Long = 0): ObjectAnimator {
    val rotationPvh = PropertyValuesHolder.ofFloat(View.ROTATION, -shakeDegrees, -shakeDegrees, 0f)
    val anim = ObjectAnimator.ofPropertyValuesHolder(this, rotationPvh)
    anim.duration = duration
    anim.repeatCount =repeatCount
    anim.repeatMode = ValueAnimator.REVERSE
    anim.interpolator = ShakeCycleInterpolator(shakeCount, duration, idleDuration)
    anim.start()
    return anim
}

class ShakeCycleInterpolator(cycles: Float, duration: Long, idleDuration: Long) : CycleInterpolator(cycles) {

    private val shakePercent = duration * 1.0f / (duration + idleDuration)
    override fun getInterpolation(input: Float): Float {
        return if (input < shakePercent) {
            super.getInterpolation(input * shakePercent)
        } else {
            1f
        }
    }
}

val View.layoutInflater: LayoutInflater
    get() = context.layoutInflater

val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun View.getRect(): Rect = Rect(left, top, right, bottom)

//脉搏动画(变大后变小)
fun View.pulse(scale: Float = 1.2f, duration: Long = 300, interpolator: Interpolator? = null) {
    val animatorSet = AnimatorSet()
    val scaleXOa = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, scale, 1f)
    val scaleYOa = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, scale, 1f)
    animatorSet.duration = duration
    interpolator?.let {
        animatorSet.interpolator = interpolator
    }
    animatorSet.playTogether(scaleXOa, scaleYOa)
    animatorSet.start()
}