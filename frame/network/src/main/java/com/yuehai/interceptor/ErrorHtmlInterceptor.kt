package com.yuehai.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.net.SocketTimeoutException

class ErrorHtmlInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val response = chain.proceed(request)

            // 如果不是 200，则统一处理
            if (response.code != 200) {
                val responseBody = response.body
                val contentType = responseBody?.contentType()
                val responseStr = responseBody?.string() ?: ""

                // 判断是否是HTML页面
                val isHtml = contentType?.subtype?.contains("html", ignoreCase = true) == true ||
                        responseStr.contains("<html", ignoreCase = true)

                // 生成默认的空 JSON 响应体结构
                val defaultJson = """
                    {
                        "code": ${response.code},
                        "message": "请求失败，返回被拦截并替换",
                        "data": {}
                    }
                """.trimIndent()

                val newBody = if (isHtml) {
                    // HTML错误时，替换成提示JSON
                    """
                        {
                            "code": ${response.code},
                            "message": "服务器返回HTML错误，请求资源或服务器异常",
                            "data": {}
                        }
                    """.trimIndent()
                } else if (responseStr.isNotEmpty()) {
                    // 非HTML且有响应内容，尝试用原始响应内容包装成 JSON 格式（你可以根据业务调整）
                    // 这里示例用空data代替原始内容，避免格式不匹配
                    defaultJson
                } else {
                    // 无响应体时，使用默认空json
                    defaultJson
                }

                return response.newBuilder()
                    .code(200) // 强制修改成200
                    .body(newBody.toByteArray().toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }

            return response

        } catch (e: IOException) {
            val errorCode = if (e is SocketTimeoutException) 408 else 503
            val errorMessage = when (e) {
                is SocketTimeoutException -> "请求超时，请稍后再试"
                else -> "网络连接异常，请检查您的网络设置"
            }

            val errorJson = """
                {
                    "code": $errorCode,
                    "message": "$errorMessage",
                    "data": {}
                }
            """.trimIndent()

            return Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(200) // 同样改成200，业务层统一处理
                .message(errorMessage)
                .body(errorJson.toByteArray().toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
        }
    }
}
