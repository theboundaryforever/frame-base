package com.adealik.frame.mvvm.livedata

import androidx.lifecycle.LiveData

open class NonNullReadOnlyLiveData<T>(
    default: T
): LiveData<T>(default) {
    override fun getValue(): T {
        return super.getValue()!!
    }
}

open class NonNullLiveData<T>(
    default: T
): NonNullReadOnlyLiveData<T>(default) {

    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }
}

