package com.yuehai.ui.widget.ext

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle

fun Fragment.runWhenChildViewReadyById(
    fragmentContainerViewId: Int,
    minState: Lifecycle.State = Lifecycle.State.STARTED,
    action: (fragment: Fragment, view: View) -> Unit
) {
    val childFragment = childFragmentManager.findFragmentById(fragmentContainerViewId)
    if (childFragment?.view != null &&
        childFragment.viewLifecycleOwner.lifecycle.currentState.isAtLeast(minState)
    ) {
        action(childFragment, childFragment.requireView())
        return
    }

    childFragment?.viewLifecycleOwnerLiveData?.observe(viewLifecycleOwner) { owner ->
        if (owner.lifecycle.currentState.isAtLeast(minState)) {
            action(childFragment, childFragment.requireView())
        }
    }
}
