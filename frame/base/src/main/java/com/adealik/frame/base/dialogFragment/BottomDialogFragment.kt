package com.adealik.frame.base.dialogFragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import com.yuehai.base.R


abstract class BottomDialogFragment(@LayoutRes override val layoutId: Int) :
    BaseDialogFragment(layoutId) {

    companion object {
        private const val TAG = "BottomDialogFragment"
    }

    open val dimAmount: Float = 0.2F
    open val followActivityUIFlag: Boolean = false
    open val height = ViewGroup.LayoutParams.WRAP_CONTENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.BottomDialog)
    }

    override fun onStart() {
        super.onStart()
        setDialogAttributes(height)
    }

    open fun setDialogAttributes(height: Int) {
        try {
            val dialog: Dialog = dialog ?: return
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
            dialog.window!!.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
            val windowParams = dialog.window?.attributes
            windowParams?.dimAmount = dimAmount
            dialog.window?.attributes = windowParams
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        // Make the dialog non-focusable before showing it
        if (followActivityUIFlag && dialog.window != null) {
            dialog.window!!.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
        }
    }

}
