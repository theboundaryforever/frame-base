package com.yuehai.util.util

import android.graphics.Point
import android.os.SystemClock
import android.view.View
import com.yuehai.util.R


fun View.getLocationInWindow(): IntArray {
    val pos = IntArray(2)
    this.getLocationInWindow(pos)
    return pos
}

fun View.getCenterLocationInWindow(): IntArray {
    val pos = IntArray(2)
    this.getLocationInWindow(pos)
    pos[0] = (pos[0] + this.width / 2f).toInt()
    pos[1] = (pos[1] + this.height / 2f).toInt()
    return pos
}

fun isTouchPointInView(view: View, x: Int, y: Int): Boolean {
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    val left = location[0]
    val top = location[1]
    val right = left + view.measuredWidth
    val bottom = top + view.measuredHeight

    return y in top..bottom && x >= left && x <= right
}

fun setViewsVisibility(visibility: Int, vararg views: View?) {
    for (v in views) {
        v?.visibility = visibility
    }
}

fun View.getChildCenterInParent(
    childCenterInWindow: Point
): Point {
    val parentLocation = IntArray(2)
    this.getLocationInWindow(parentLocation)
    val xInParent = childCenterInWindow.x - parentLocation[0]
    val yInParent = childCenterInWindow.y - parentLocation[1]
    return Point(xInParent, yInParent)
}

fun View.getCenterLocationInWindow(widthOffsetRatio: Float, heightOffsetRatio: Float): IntArray {
    val pos = IntArray(2)
    this.getLocationInWindow(pos)
    pos[0] = (pos[0] + this.width / 2f + widthOffsetRatio * this.width).toInt()
    pos[1] = (pos[1] + this.height / 2f + heightOffsetRatio * this.height).toInt()
    return pos
}






class DebounceAction(val view: View, var block: ((View) -> Unit)) : Runnable {
    override fun run() {
        if (view.isAttachedToWindow) {
            block(view)
        }
    }
}

