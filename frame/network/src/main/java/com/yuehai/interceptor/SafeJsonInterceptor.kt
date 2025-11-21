package com.yuehai.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException

class SafeJsonInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val responseBody = response.body ?: return response
        val contentType = responseBody.contentType()

        if (!response.isSuccessful && contentType != null && contentType.subtype == "json") {
            val responseStr = try {
                responseBody.string()
            } catch (e: IOException) {
                e.printStackTrace()
                return response
            }

            val newResponseBody = responseStr.toResponseBody(contentType)

            try {
                JSONObject(responseStr)
            } catch (e: Exception) {
                val errorJson = """
                    {
                        "code": ${response.code},
                        "message": "JSON解析异常，降级处理",
                        "type": "json_parse_error"
                    }
                """.trimIndent()

                return response.newBuilder()
                    .body(errorJson.toByteArray().toResponseBody(contentType))
                    .build()
            }

            return response.newBuilder().body(newResponseBody).build()
        }

        return response
    }
}