package com.yuehai.util.util.ext

import android.view.View


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
