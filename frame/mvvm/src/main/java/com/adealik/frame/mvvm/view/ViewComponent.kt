package com.adealik.frame.mvvm.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.adealik.frame.mvvm.util.isActivityInValid
import com.adealik.frame.mvvm.util.isViewBindingValid

abstract class ViewComponent(
    override val lifecycleOwner: LifecycleOwner,
) : LifecycleOwner, LifecycleObserver, IViewComponent {

    override val lifecycle: Lifecycle
        get() = lifecycleOwner.lifecycle

    var activity: FragmentActivity?
        get() {
            return field ?: fragment?.activity
        }

    fun requireActivity(): FragmentActivity {
        return activity
            ?: throw IllegalStateException("$this not attached to an activity.")
    }

    protected val isFinished
        get() = activity?.isFinishing ?: true

    var fragment: Fragment?

    var isAttached: Boolean = false
        private set

    val viewLifecycleOwner: LifecycleOwner
        get() {
            return fragment?.viewLifecycleOwner ?: lifecycleOwner
        }

    val context: Context?
        get() = fragment?.context ?: activity

    val viewModelStoreOwner: ViewModelStoreOwner
        get() = fragment ?: activity!!

    val fragmentManager: FragmentManager
        get() = fragment?.childFragmentManager ?: activity!!.supportFragmentManager

    val isValid: Boolean
        get() {
            val f = fragment
            if (f != null) {
                return f.isViewBindingValid()
            }

            return !isActivityInValid(activity)
        }

    init {
        when (val owner = lifecycleOwner) {
            is FragmentActivity -> {
                activity = owner
                fragment = null
            }
            is Fragment -> {
                fragment = owner
                activity = owner.activity
            }
            else -> {
                throw IllegalArgumentException("ViewComponent must attach to `ComponentActivity` or `Fragment`")
            }
        }
    }

    override fun attach(): ViewComponent {
        this.lifecycle.addObserver(this)
        isAttached = true
        return this
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected open fun onCreate() {
        try {
            activity?.savedStateRegistry?.registerSavedStateProvider(savedStateKey) {
                Bundle().apply {
                    onSaveInstanceState(this)
                }
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    @CallSuper
    open fun onSaveInstanceState(outState: Bundle) {

    }

    open fun onNewIntent(intent: Intent?) {

    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected open fun onStart() {

    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected open fun onResume() {

    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected open fun onPause() {

    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected open fun onStop() {

    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected open fun onDestroy() {
        this.lifecycle.removeObserver(this)
        activity?.savedStateRegistry?.unregisterSavedStateProvider(savedStateKey)
        fragment = null
        activity = null
    }

    open val savedStateKey by lazy(LazyThreadSafetyMode.NONE) {
        javaClass.canonicalName ?: ""
    }

    protected val restoredState: Bundle?
        get() {
            return activity?.savedStateRegistry?.run {
                if (isRestored) {
                    consumeRestoredStateForKey(savedStateKey)
                } else null
            }
        }

}
