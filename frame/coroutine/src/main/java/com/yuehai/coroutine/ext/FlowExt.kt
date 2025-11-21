package com.yuehai.coroutine.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.throttleFirst(duration: Long): Flow<T> = flow {
    var startTime = System.currentTimeMillis()
    var emitted = false
    collect { value ->
        val currentTime = System.currentTimeMillis()
        val delta = currentTime - startTime
        if (delta >= duration) {
            startTime = currentTime
            emitted = false
        }
        if (!emitted) {
            emit(value)
            emitted = true
        }
    }
}
