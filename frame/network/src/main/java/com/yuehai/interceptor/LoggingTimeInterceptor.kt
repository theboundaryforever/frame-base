package com.yuehai.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class LoggingTimeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val startNs = System.nanoTime()

        val response = chain.proceed(request)

        val endNs = System.nanoTime()
        val durationMs = (endNs - startNs) / 1_000_000.0 // 转为毫秒

        val url = request.url.toString()
        Log.d("HttpRequestTiming", "Request to $url took ${"%.2f".format(durationMs)} ms")

        return response
    }
}
