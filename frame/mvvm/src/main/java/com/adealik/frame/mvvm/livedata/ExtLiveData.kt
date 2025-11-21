package com.adealik.frame.mvvm.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


open class ExtLiveData<T> : LiveData<T> {

    constructor(value: T) : super(value)

    constructor() : super()

    private val observerStatusMap = hashMapOf<Observer<in T>, ObserveStatus>()

    override fun setValue(value: T) {
        observerStatusMap.iterator().forEach { it.value.setValue = true }
        super.setValue(value)
    }

    fun observeWithoutCache(owner: LifecycleOwner, observer: Observer<in T>) {
        val proxyObserver = object : Observer<T> {

            override fun onChanged(t: T) {
                val observerStatus = observerStatusMap[this]
                if (observerStatus?.setValue == true) {
                    observer.onChanged(t)
                }
            }

        }
        observerStatusMap[proxyObserver] = ObserveStatus()
        super.observe(owner, proxyObserver)
    }

    override fun removeObserver(observer: Observer<in T>) {
        super.removeObserver(observer)
        observerStatusMap.remove(observer)
    }

}

data class ObserveStatus(var setValue: Boolean = false)

open class ExtMutableLiveData<T> : ExtLiveData<T> {

    constructor(value: T) : super(value)

    constructor() : super()

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }

}