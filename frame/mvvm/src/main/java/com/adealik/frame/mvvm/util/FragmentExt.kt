package com.adealik.frame.mvvm.util

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle


fun Fragment.isUIValid(): Boolean {
    return isAdded && view != null && !isDetached && context != null && !isRemoving
}


fun Fragment.isViewBindingValid(): Boolean {
    if (!isUIValid()) {
        return false
    }

    return try {
        /**
         * 2. 状态门槛优化：
         * STARTED 确保 View 已创建、已布局且对用户可见，此时操作 UI 最为安全。
         */
        viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    } catch (e: Exception) {
        Log.e("FragmentExt", "isViewBindingValid: View lifecycle inaccessible - ${e.message}")
        false
    }
}