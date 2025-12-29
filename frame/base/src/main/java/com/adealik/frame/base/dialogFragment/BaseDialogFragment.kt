package com.adealik.frame.base.dialogFragment

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.adealik.frame.base.dialogqueue.DialogCallback
import com.adealik.frame.base.dialogqueue.DialogQueue
import com.adealik.frame.base.loading.DialogLoadingController

open class BaseDialogFragment(
    @LayoutRes open val layoutId: Int
) : DialogFragment() {

    companion object {
        private const val TAG = "BaseDialogFragment"
    }

    @Volatile
    private var isShow = false

    private var dialogCallbackHandler: DialogCallback? = null

    /** ⭐ loading 控制器 */
    protected val loadingController by lazy {
        DialogLoadingController(this)
    }

    open val fgTag: String? = null
    open val cancelable: Boolean = true
    open val canceledOnTouchOutside: Boolean = true

    /* ================= loading API ================= */

    fun showLoading(cancelable: Boolean = false) = loadingController.showLoading(cancelable)

    fun dismissLoading() = loadingController.dismissLoading()

    fun forceDismissLoading() = loadingController.forceDismissLoading()

    fun setAutoDismissLoading(auto: Boolean) {
        loadingController.autoDismissLoading = auto
    }

    /* ============================================== */

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(layoutId, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initComponents()
        observeViewModel()
        loadData()
    }

    open fun initViews() {}
    open fun initComponents() {}
    open fun observeViewModel() {}
    open fun loadData() {}

    override fun show(manager: FragmentManager, tag: String?) {
        if (!isShow && !isAdded) {
            isShow = true
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
        }
    }

    open fun show(manager: FragmentManager) {
        show(manager, fgTag)
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        return if (!isShow && !isAdded && !isVisible) {
            isShow = true
            try {
                super.show(transaction, tag)
            } catch (e: Exception) {
                e.printStackTrace()
                this.dismissAllowingStateLoss()
                -1
            }
        } else {
            -1
        }
    }

    override fun dismissAllowingStateLoss() {
        if (isAdded && !isStateSaved && !isDetached) {
            try {
                super.dismissAllowingStateLoss()
            } catch (e: Exception) {
                Log.e(TAG, "dismiss exception", e)
            }
        }
        loadingController.dismissLoading()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isShow = false
        dialogCallbackHandler?.pollDialog()
    }

    override fun onStart() {
        super.onStart()
        isCancelable = cancelable
        dialog?.setCanceledOnTouchOutside(canceledOnTouchOutside)
        // 调整窗口属性
        dialog?.window?.let { resetWindowAttributes(it) }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.let { resetWindowAttributes(it) }
    }

    open fun resetWindowAttributes(window: Window) {}

    open fun blockBackEvent() {
        dialog?.setOnKeyListener { _: DialogInterface?, keyCode: Int, _: KeyEvent? ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
    }

    fun setDialogCallback(callback: DialogQueue) {
        dialogCallbackHandler = callback
    }
}
