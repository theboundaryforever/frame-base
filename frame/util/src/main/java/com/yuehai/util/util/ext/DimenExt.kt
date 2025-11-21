package com.yuehai.util.util.ext

import com.yuehai.util.util.DisplayUtil


fun Float.dp(): Int {
    return DisplayUtil.dp2px(this)
}

fun Float.dpf(): Float {
    return DisplayUtil.dp2px(this).toFloat()
}

fun Float.sp(): Int {
    return DisplayUtil.sp2px(this)
}

fun Float.spf(): Float {
    return DisplayUtil.sp2px(this).toFloat()
}

fun Int.dp(): Int {
    return DisplayUtil.dp2px(this.toFloat())
}

fun Int.dpf(): Float {
    return DisplayUtil.dp2px(this.toFloat()).toFloat()
}

fun Int.sp(): Int {
    return DisplayUtil.dp2px(this.toFloat())
}

fun Int.spf(): Float {
    return DisplayUtil.dp2px(this.toFloat()).toFloat()
}