package com.adealik.frame.mvvm.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


class OnceMutableLiveData<T> : MutableLiveData<T>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, object : Observer<T> {

            override fun onChanged(t: T) {
                removeObserver(this)
                observer.onChanged(t)
            }

        })
    }

}