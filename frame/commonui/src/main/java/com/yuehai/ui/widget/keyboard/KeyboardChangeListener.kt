package com.yuehai.ui.widget.keyboard

import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener


class KeyboardChangeListener(contextObj: Activity) : OnGlobalLayoutListener {

    private val mContentView: View?
    private var rootViewVisibleHeight = 0
    private var mKeyBoardListen: KeyBoardListener? = null

    companion object {
        private const val TAG = "ListenerHandler"
    }

    init {
        mContentView = findContentView(contextObj)
        addContentTreeObserver()
    }

    interface KeyBoardListener {
        /**
         * call back
         *
         * @param isShow         true is show else hidden
         * @param keyboardHeight keyboard height
         */
        fun onKeyboardChange(isShow: Boolean, keyboardHeight: Int)
    }

    fun setKeyBoardListener(keyBoardListen: KeyBoardListener?) {
        mKeyBoardListen = keyBoardListen
    }

    private fun findContentView(contextObj: Activity?): View {
        return contextObj!!.window.decorView
    }

    private fun addContentTreeObserver() {
        mContentView!!.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        //获取当前根视图在屏幕上显示的大小
        val r = Rect()
        mContentView!!.getWindowVisibleDisplayFrame(r)
        val visibleHeight = r.height()
        Log.d(TAG, "visibleHeight:${visibleHeight}, rootViewVisibleHeight:${rootViewVisibleHeight}")
        if (rootViewVisibleHeight == 0) {
            rootViewVisibleHeight = visibleHeight
            return
        }
        //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
        if (rootViewVisibleHeight == visibleHeight) {
            Log.d(TAG, "rootViewVisibleHeight == visibleHeight")
            return
        }
        //根视图显示高度变小超过200，可以看作软键盘显示了
        if (rootViewVisibleHeight - visibleHeight > 200) {
            Log.d(TAG, "rootViewVisibleHeight - visibleHeight > 200")
            if (mKeyBoardListen != null) {
                mKeyBoardListen!!.onKeyboardChange(true, rootViewVisibleHeight - visibleHeight)
            }
            rootViewVisibleHeight = visibleHeight
            return
        }
        //根视图显示高度变大超过200，可以看作软键盘隐藏了
        if (visibleHeight - rootViewVisibleHeight > 200) {
            Log.d(TAG, "visibleHeight - rootViewVisibleHeight > 200")
            if (mKeyBoardListen != null) {
                mKeyBoardListen!!.onKeyboardChange(false, visibleHeight - rootViewVisibleHeight)
            }
            rootViewVisibleHeight = visibleHeight
            return
        }
    }

    fun destroy() {
        mContentView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
    }


}