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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
        //ImmersionBar.with(this).statusBarDarkFont(true).init()
        initViews()
        initComponents()
        observeViewModel()
        loadData()
    }

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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

    open fun initComponents() {

    }

    fun showLoading() {
        (activity as? NewBaseActivity)?.showLoading()
    }

    fun dismissLoading() {
        (activity as? NewBaseActivity)?.dismissLoading()
    }


    open fun onNewIntent(intent: Intent?) {

    }

    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        dismissLoading()
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
            dialogBuilder,
        )
    }
}