package com.adealik.frame.mvvm.viewmodel

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adealik.frame.mvvm.livedata.ExtMutableLiveData
import com.adealik.frame.mvvm.livedata.NonNullLiveData
import com.adealik.frame.mvvm.livedata.UnPeekLiveData
import com.adealik.frame.mvvm.util.isMainThread
import com.yuehai.coroutine.coroutine.dispatcher.Dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable
import java.io.IOException
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {

    private var cachedViewModelScope: CloseableCoroutineScope? = null

    @Volatile
    private var hasCleared = false

    val viewModelScope: CoroutineScope
        get() {
            val result = cachedViewModelScope ?: CloseableCoroutineScope(
                SupervisorJob() + Dispatcher.UI
            )
            cachedViewModelScope = result
            if (hasCleared) {
                closeWithRuntimeException(result)
            }
            return result
        }

    private fun closeWithRuntimeException(obj: Any) {
        if (obj is Closeable) {
            try {
                obj.close()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    protected fun <T> LiveData<T>.send(value: T, postCover: Boolean = false) {
        when (this) {
            is MutableLiveData<T> -> {
                if (isMainThread()) {
                    this.value = value
                } else if (postCover) {
                    this.postValue(value)
                } else {
                    ArchTaskExecutor.getInstance().postToMainThread {
                        this.value = value
                    }
                }
            }
            is ExtMutableLiveData<T> -> {
                if (isMainThread()) {
                    this.setValue(value)
                } else if (postCover) {
                    this.postValue(value)
                } else {
                    ArchTaskExecutor.getInstance().postToMainThread {
                        this.setValue(value)
                    }
                }
            }
            is NonNullLiveData<T> -> {
                if (isMainThread()) {
                    this.value = value
                } else if (postCover) {
                    this.postValue(value)
                } else {
                    ArchTaskExecutor.getInstance().postToMainThread {
                        this.value = value
                    }
                }
            }
            is UnPeekLiveData<T> ->{
                if (isMainThread()) {
                    this.value = value
                } else if (postCover) {
                    this.postValue(value)
                } else {
                    ArchTaskExecutor.getInstance().postToMainThread {
                        this.value = value
                    }
                }
            }
            else -> error("emit() only support MutableLiveData/NonNullLiveData/PublishData")
        }
    }

    internal class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
        override val coroutineContext: CoroutineContext = context

        override fun close() {
            coroutineContext.cancel()
        }
    }

    @CallSuper
    override fun onCleared() {
        hasCleared = true
        super.onCleared()
        cachedViewModelScope?.let {
            closeWithRuntimeException(it)
        }
    }

}