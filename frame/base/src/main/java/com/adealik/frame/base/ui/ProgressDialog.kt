package com.adealik.frame.base.ui

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.adealik.frame.base.dialogFragment.BaseDialogFragment
import com.adealik.frame.mvvm.view.viewBinding

import com.yuehai.base.R
import com.yuehai.base.databinding.LayoutProgressDialogBinding
import com.yuehai.util.util.ext.dp
import androidx.core.graphics.drawable.toDrawable


class ProgressDialog : BaseDialogFragment(R.layout.layout_progress_dialog) {

    companion object {
        const val TAG = "ProgressDialog"
    }

    private val binding by viewBinding(LayoutProgressDialogBinding::bind)

    var onCancelListener: DialogInterface.OnCancelListener? = null
    var onDismissListener: (() -> Unit)? = null
    var text: CharSequence? = null

    override val fgTag: String = TAG

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
           120.dp()
        )
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val windowParams = window?.attributes
//        windowParams?.dimAmount = 0.0F
        window?.attributes = windowParams
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnCancelListener(onCancelListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (text.isNullOrEmpty()) {
            binding.text.visibility = View.GONE
        } else {
            binding.text.visibility = View.VISIBLE
            binding.text.text = text
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    class Builder {

        private var text: CharSequence? = null
        private var onDismissListener: (() -> Unit)? = null
        private var onCancelListener: DialogInterface.OnCancelListener? = null

        fun text(text: CharSequence): Builder {
            this.text = text
            return this
        }

        fun dismissListener(onDismissListener: (() -> Unit)): Builder {
            this.onDismissListener = onDismissListener
            return this
        }

        fun cancelListener(onCancelListener: DialogInterface.OnCancelListener): Builder {
            this.onCancelListener = onCancelListener
            return this
        }

        fun build(): ProgressDialog {
            val dialog = ProgressDialog()
            dialog.text = text
            dialog.onDismissListener = onDismissListener
            dialog.onCancelListener = onCancelListener
            return dialog
        }

    }

}