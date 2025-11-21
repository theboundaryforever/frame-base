package com.yuehai.ui.widget.widget.dialog

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import com.adealik.frame.base.dialogFragment.BaseDialogFragment
import com.adealik.frame.mvvm.view.viewBinding
import com.yuehai.ui.R
import com.yuehai.ui.databinding.RoomDialogOperateTipsBinding

import com.yuehai.util.util.DisplayUtil
import com.yuehai.util.util.ext.gone
import com.yuehai.util.util.ext.setSafeClickListener
import com.yuehai.util.util.ext.show

class CustomTipDialog : BaseDialogFragment(R.layout.room_dialog_operate_tips) {
    private val binding by viewBinding(RoomDialogOperateTipsBinding::bind)
    private var tipText: String? = null
    private var contentText: String? = null
    private var blockContentBlock: String? = null
    private var showJoinBlock: Boolean = false
    private var onCancel: (() -> Unit)? = null
    private var onConfirm: ((Boolean) -> Unit)? = null

    override fun initViews() {
        if (tipText.isNullOrEmpty().not()) {
            binding.tvTips.text = tipText
        }

        if (contentText.isNullOrEmpty().not()) {
            binding.tvContent.text = contentText
        }

        if (blockContentBlock.isNullOrEmpty().not()) {
            binding.clJoinBlock.show()
            binding.tvJoinBlockTips.text = blockContentBlock
        } else {
            binding.clJoinBlock.gone()
        }

        binding.tvCancel.setSafeClickListener {
            onCancel?.invoke()
            dismiss()
        }

        binding.tvConfirm.setSafeClickListener {
            onConfirm?.invoke(binding.tvJoinBlockTips.isChecked)
            dismiss()
        }

    }

    class Builder {
        private val dialog = CustomTipDialog()

        fun setTip(text: String): Builder {
            dialog.tipText = text
            return this
        }

        fun setContent(text: String?): Builder {
            dialog.contentText = text
            return this
        }

        fun setBlockContent(text: String?): Builder {
            dialog.blockContentBlock = text
            return this
        }

        fun showJoinBlock(show: Boolean): Builder {
            dialog.showJoinBlock = show
            return this
        }

        fun onCancel(action: () -> Unit): Builder {
            dialog.onCancel = action
            return this
        }

        fun onConfirm(action: (Boolean) -> Unit): Builder {
            dialog.onConfirm = action
            return this
        }

        fun build(): CustomTipDialog = dialog
    }

    override fun onStart() {
        super.onStart()
        resetDialogAttributes()
    }

    private fun resetDialogAttributes() {
        val dialog = dialog ?: return
        val window = dialog.window ?: return

        window.setLayout(
            (DisplayUtil.getScreenWidth() * 0.746f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val attr = window.attributes
        attr.gravity = Gravity.CENTER
        attr.dimAmount = 0.2f
        window.attributes = attr
    }
}
