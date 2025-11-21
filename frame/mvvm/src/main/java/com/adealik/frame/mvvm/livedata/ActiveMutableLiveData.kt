package com.adealik.frame.mvvm.livedata

import androidx.lifecycle.MutableLiveData

/**
 * 非活跃状态不分发数据的LiveData
 * 注意：使用该LiveData要先observe后触发liveData分发value
 */
open class ActiveMutableLiveData<T> : MutableLiveData<T>() {

    private var active = false

    override fun setValue(value: T) {
        if (active.not()) {
            return
        }

        super.setValue(value)
    }

    override fun onActive() {
        active = true
        super.onActive()
    }

    override fun onInactive() {
        active = false
        super.onInactive()
    }

}