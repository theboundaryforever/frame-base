package com.yuehai.interceptor

import android.content.Intent
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import android.util.Log
import com.didi.drouter.api.DRouter
import com.didi.drouter.api.Extend
import com.yuehai.data.collection.path.Constants
import com.yuehai.data.collection.path.router.Login
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.net.SocketTimeoutException

class CodeCheckInterceptor : Interceptor {

    companion object {
        @Volatile
        var hasRedirected = false
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val response = chain.proceed(request)

            val responseBody = response.body
            val contentType = responseBody?.contentType()
            val responseBodyString = responseBody?.string() ?: ""

            // 如果响应不成功，且是 HTML 错误页面，则转换为 JSON
            if (!response.isSuccessful) {
                if (contentType?.subtype?.contains("html", ignoreCase = true) == true ||
                    responseBodyString.contains("<html", ignoreCase = true)) {

                    val errorJson = """
                        {
                            "code": ${response.code},
                            "message": "服务器返回HTML错误，请求资源或服务器异常",
                            "type": "html_error"
                        }
                    """.trimIndent()

                    return response.newBuilder()
                        .body(errorJson.toByteArray().toResponseBody("application/json".toMediaTypeOrNull()))
                        .build()
                }
            }

            // 重建响应体，以便后续拦截器或 Retrofit 可以读取
            val newResponseBody = ResponseBody.create(contentType, responseBodyString)
            val newResponse = response.newBuilder().body(newResponseBody).build()

            // 以下是您原有的业务逻辑，处理格式正确的 JSON
            if (responseBodyString.isNotBlank()) {
                val trimmedBody = responseBodyString.trim()

                if (trimmedBody.startsWith("{") || trimmedBody.startsWith("[")) {
                    try {
                        val jsonObject = JSONObject(trimmedBody)
                        val code = jsonObject.optInt("code", -1)

                        if (code == 10001 && !hasRedirected) {
                            hasRedirected = true
                            Constants.logout()

                            DRouter.build(Login.Login.PATH).putExtra(
                                Extend.START_ACTIVITY_FLAGS,
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            ).start()
                            Log.e("CodeCheckInterceptor", "检测到 code=100001，触发登录跳转")
                        }

                    } catch (e: Exception) {
                        Log.e("CodeCheckInterceptor", "JSON解析异常: ${e.message}")
                    }
                } else {
                    Log.w("CodeCheckInterceptor", "响应体非JSON，内容前几字符: ${trimmedBody.take(100)}")
                }
            } else {
                Log.w("CodeCheckInterceptor", "响应体为空")
            }

            return newResponse

        } catch (e: IOException) {
            // 捕获网络异常（如超时、无网络连接），并返回一个自定义的 JSON 响应
            val errorCode = 200
            val errorMessage = when (e) {
                is SocketTimeoutException -> "请求超时，请稍后再试"
                else -> "网络连接异常，请检查您的网络设置"
            }

            val errorJson = """
                {
                    "code": $errorCode,
                    "message": "$errorMessage",
                    "type": "request_exception"
                }
            """.trimIndent()

            return Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(errorCode)
                .message(errorMessage)
                .body(errorJson.toByteArray().toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
        }
    }
}