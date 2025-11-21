package com.adealik.frame.mvvm.disposable

import java.util.concurrent.atomic.AtomicReference

class RunnableDisposable(runnable: Runnable): AtomicReference<Runnable>(runnable), Disposable {

    constructor(action: () -> Unit): this(Runnable { action() })

    override val disposed: Boolean
        get() = super.get() == null

    override fun dispose() {
        var r = get()
        if (r != null) {
            r = getAndSet(null)
            if (r != null) {
                onDisposed(r)
            }
        }
    }

    private fun onDisposed(r: Runnable) {
        r.run()
    }
}