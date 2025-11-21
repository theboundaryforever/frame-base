package com.yuehai.ui.widget.widget

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.adealik.frame.base.dialogFragment.BottomDialogFragment
import com.yuehai.ui.widget.keyboard.KeyboardChangeListener
import com.yuehai.ui.widget.util.QMUIKeyboardHelper


open class KeyboardBottomDialogFragment(@LayoutRes override val layoutId: Int) :
    BottomDialogFragment(layoutId), KeyboardChangeListener.KeyBoardListener {

    private var keyboardChangeListener: KeyboardChangeListener? = null

    @CallSuper
    override fun setDialogAttributes(height: Int) {
        //置空，因为他会造成EditText的软键盘弹不出来
    }

    @CallSuper
    override fun onStart() {
        fixFocus()
        super.onStart()
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dismiss()
            }
            false
        }
    }

    private fun fixFocus() {
        val windowParams = dialog?.window?.attributes
        windowParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        windowParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        windowParams?.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        windowParams?.dimAmount = dimAmount
        windowParams?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        dialog?.window?.attributes = windowParams
    }

    @CallSuper
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        QMUIKeyboardHelper.hideKeyboard(view)
    }

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            if (keyboardChangeListener == null) {
                keyboardChangeListener = KeyboardChangeListener(context)
            }
            keyboardChangeListener?.setKeyBoardListener(this)
        }
    }

    @CallSuper
    override fun onDetach() {
        super.onDetach()
        keyboardChangeListener?.destroy()
        keyboardChangeListener = null
    }

    override fun onKeyboardChange(isShow: Boolean, keyboardHeight: Int) {
        if (!isShow && dialog?.isShowing == true) {
            dismiss()
        }
    }

}