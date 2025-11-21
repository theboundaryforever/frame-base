package com.yuehai.interceptor

import android.os.SystemClock
import android.util.Log
import com.yuehai.data.TAG_HTTP_LOG
import okhttp3.*
import okio.Buffer
import java.nio.charset.Charset

class HttpLoggingInterceptor : Interceptor {

    companion object {
        private val UTF8: Charset = Charset.forName("UTF-8")
        private const val LOG_MAX_LENGTH = 5000
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var bodyString = ""

        val body: RequestBody? = request.body
        if (body != null) {
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset(UTF8) ?: UTF8
            bodyString = buffer.readString(charset)
        }

        val startTime = SystemClock.elapsedRealtime()

        val response = try {
            chain.proceed(request)
        } catch (e: Throwable) {
            Log.e(TAG_HTTP_LOG, "HttpLoggingInterceptor error:\n request:$request,\n e:$e")
            throw e
        }

        var responseBodyString = ""
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer = source.buffer
            val charset = responseBody.contentType()?.charset(UTF8) ?: UTF8
            responseBodyString = buffer.clone().readString(charset)
        }

        val message = buildString {
            appendLine("success,")
            appendLine("    time:${SystemClock.elapsedRealtime() - startTime},")
            appendLine("    request:$request,")
            appendLine("    requestBody:$bodyString,")
            appendLine("    protocol:${response.protocol},")
            appendLine("    tlsVersion:${response.handshake?.tlsVersion}, cipherSuite:${response.handshake?.cipherSuite},")
            appendLine("    cacheResponse:${response.cacheResponse != null}, networkResponse:${response.networkResponse != null},")
            appendLine("    response:$response,")
            appendLine("    responseBody:$responseBodyString")
        }

        log(message)
        return response
    }


    private fun log(message: String) {
        var content = message
        while (content.length > LOG_MAX_LENGTH) {
            val chunk = content.substring(0, LOG_MAX_LENGTH)
            content = content.substring(LOG_MAX_LENGTH)
            Log.d(TAG_HTTP_LOG, chunk)
        }
        if (content.isNotEmpty()) {
            Log.d(TAG_HTTP_LOG, content)
        }
    }
}
