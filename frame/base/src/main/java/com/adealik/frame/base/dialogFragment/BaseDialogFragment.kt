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
import com.kaopiz.kprogresshud.KProgressHUD
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog

inline fun <reified F : BaseDialogFragment> showDialogFragment(
    fm: FragmentManager,
    args: Bundle? = null,
): F {
    val f = F::class.java.newInstance().apply {
        arguments = args
    }
    f.show(fm)
    return f
}

inline fun <reified F : BaseDialogFragment> showDialogFragment(
    ft: FragmentTransaction,
    args: Bundle? = null,
): F {
    val f = F::class.java.newInstance().apply {
        arguments = args
    }
    f.show(ft)
    return f
}

open class BaseDialogFragment(@LayoutRes open val layoutId: Int) : DialogFragment() {
    private var dialogCallbackHandler: DialogCallback? = null

    companion object {
        private const val TAG = "BaseDialogFragment"
    }

    @Volatile
    private var isShow = false

    open val fgTag: String? = null
    private var loadingDialog: KProgressHUD? = null
    open val cancelable: Boolean = true
    open val canceledOnTouchOutside: Boolean = true
    private var dismissCallback: (() -> Unit)? = null
    private val dismissCallbacks = mutableListOf<OnDismissCallback>()
    private var clickCallback: ((tag: Any?) -> Unit)? = null

    fun addOnDismissCallback(callback: OnDismissCallback) {
        dismissCallbacks.add(callback)
    }

    fun removeOnDismissCallback(callback: OnDismissCallback) {
        dismissCallbacks.remove(callback)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        return LayoutInflater.from(context).inflate(layoutId, container, false)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initComponents()
        observeViewModel()
        loadData()
    }

    fun isViewCreated(): Boolean {
        return isAdded && view != null
    }

    open fun initViews() {

    }

    open fun observeViewModel() {

    }

    open fun loadData() {

    }

    fun setCanceledOnTouchOutside(canceledOnTouchOutside: Boolean) {
        dialog?.setCanceledOnTouchOutside(canceledOnTouchOutside)
    }

    open fun show(transaction: FragmentTransaction): Int {
        return show(transaction, fgTag)
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        return if (!this.isShow && !this.isAdded && !this.isVisible) {
            this.isShow = true
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

    open fun show(manager: FragmentManager) {
        show(manager, fgTag)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (!this.isShow && !this.isAdded && !this.isVisible) {
            this.isShow = true
            val ft = manager.beginTransaction()
            val prev = manager.findFragmentByTag(tag)
            if (prev != null) {
                ft.remove(prev)
            }
            try {
                ft.add(this, tag)
                ft.commitAllowingStateLoss()
            } catch (var6: Exception) {
                var6.printStackTrace()
                this.dismissAllowingStateLoss()
            }

        }
    }

    private fun initLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
        }
    }

    fun showLoading() {
        initLoadingDialog()
        loadingDialog?.show()
    }

    fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    override fun dismiss() {
        this.dismissAllowingStateLoss()
        dismissLoading()
    }

    fun setDialogCallback(callback: DialogQueue) {
        dialogCallbackHandler = callback
    }


    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (this.dialog == null) {
            this.showsDialog = false
        }
        //必须设置为无标题栏，否则4.4.2会出现一条蓝线.这个方法必须在setContentView之前，所以在onActivityCreated之前
        //https://stackoverflow.com/questions/37738690/dialogfragment-has-blue-line-on-top-in-android-4-4-2
        val window = dialog?.window
        try {
            window?.requestFeature(Window.FEATURE_NO_TITLE)
        } catch (ex: Exception) {
            Log.e(TAG, "requestFeature exception : $ex")
        }
        super.onActivityCreated(savedInstanceState)
    }

    open fun initComponents() {}

    override fun dismissAllowingStateLoss() {
        // 只有当 Fragment 已经附加到 Activity 时才执行 dismiss
        if (isAdded && !isStateSaved && !isDetached && !isHidden) {
            try {
                super.dismissAllowingStateLoss()
            } catch (e: Exception) {
                // 捕获异常，但最好是修复根本问题而不是隐藏它
                e.printStackTrace()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        this.isShow = false
        dismissCallback?.invoke()
        dismissCallbacks.forEach { it.onDismiss() }
        dialogCallbackHandler?.pollDialog()
    }

    fun setDismissCallback(dismissCallback: (() -> Unit)?): BaseDialogFragment {
        this.dismissCallback = dismissCallback
        return this
    }

    fun setClickCallback(clickCallback: ((tag: Any?) -> Unit)?): BaseDialogFragment {
        this.clickCallback = clickCallback
        return this
    }

    fun getClickCallback(): ((tag: Any?) -> Unit)? {
        return clickCallback
    }

    override fun onDestroyView() {
        super.onDestroyView()
        this.isShow = false
        dismissCallbacks.clear()
    }

    @CallSuper
    override fun onStart() {
        try {
            super.onStart()
            isCancelable = cancelable
            setCanceledOnTouchOutside(canceledOnTouchOutside)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "onStart exception, exception message = ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val dialog = dialog ?: return
            val window = dialog.window ?: return
            resetWindowAttributes(window)
        }catch (e: Exception){
            e.printStackTrace()
            Log.d(TAG, "onStart exception, exception message = ${e.message}")
        }
    }

    open fun isShowing(): Boolean {
        return isShow
    }

    open fun resetWindowAttributes(window: Window) {

    }

    //点击返回键不消失
    open fun blockBackEvent() {
        dialog?.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true
                }
                return false
            }
        })
    }
}


interface OnDismissCallback {
    fun onDismiss()
}