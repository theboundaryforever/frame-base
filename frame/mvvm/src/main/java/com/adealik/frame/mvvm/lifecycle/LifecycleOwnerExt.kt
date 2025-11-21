package com.adealik.frame.mvvm.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

inline fun LifecycleOwner.observeOnDestroy(crossinline action: () -> Unit) {
    lifecycle.addObserver(object: LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(owner: LifecycleOwner) {
            action()
        }
    })
}