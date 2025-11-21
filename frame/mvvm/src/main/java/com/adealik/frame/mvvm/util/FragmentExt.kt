package com.adealik.frame.mvvm.util

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle


fun Fragment.isUIValid(): Boolean {
    return isAdded && view != null && !isDetached && context != null
}

fun Fragment.isViewBindingValid(): Boolean {
    if (!isUIValid() && view != null) {
        return false
    }
    return try {
        viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)
    } catch (e: Exception) {
        Log.e("Fragment", "isViewBindingValid return false, for ${e.message}")
        false
    }
}