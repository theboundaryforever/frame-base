package com.adealik.frame.mvvm.disposable

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Represents a disposable resource
 */
interface Disposable {
    /**
     * True if this resource has been disposed
     */
    val disposed: Boolean

    /**
     * Dispose the resource
     */
    fun dispose()
}

fun Disposable.bind(lifecycle: Lifecycle) {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                dispose()
            }
        }
    })
}
