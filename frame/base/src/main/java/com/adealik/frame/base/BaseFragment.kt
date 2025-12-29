package com.adealik.frame.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.adealik.frame.base.dialogqueue.data.Priority
import com.gyf.immersionbar.ImmersionBar
import com.yuehai.util.util.installSplitCompat


open class BaseFragment : Fragment {

    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        installSplitCompat(context)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initComponents()
        observeViewModel()
        loadData()
    }

    fun isViewCreated(): Boolean = isAdded && view != null

    open fun initViews() {}
    open fun observeViewModel() {}
    open fun loadData() {}
    open fun initComponents() {}

    /* ================= Loading ================= */

    fun showLoading(cancelable: Boolean = false) {
        (activity as? NewBaseActivity)?.showLoading(cancelable)
    }

    fun dismissLoading() {
        (activity as? NewBaseActivity)?.dismissLoading()
    }

    fun forceDismissLoading() {
        (activity as? NewBaseActivity)?.forceDismissLoading()
    }

    /* =========================================== */

    open fun onNewIntent(intent: Intent?) {}

    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = false

    /**
     * ❗不要在 Fragment 生命周期里自动关闭 loading
     */
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        // ❌ 不再 dismissLoading()
    }

    fun dialogOffer(
        tag: String,
        priority: Priority,
        fragmentManager: FragmentManager,
        dialogBuilder: Any?
    ) {
        (activity as? NewBaseActivity)?.dialogOffer(
            tag,
            priority,
            fragmentManager,
            dialogBuilder
        )
    }
}
