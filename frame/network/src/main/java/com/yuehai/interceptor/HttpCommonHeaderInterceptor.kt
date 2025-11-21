package com.yuehai.interceptor


import com.yuehai.data.collection.path.Constants
import com.yuehai.util.util.SignatureNewUtil
import okhttp3.*
import java.io.IOException

fun getSign(ts: Long): String {
    val uid = Constants.getUserId()
    val token = Constants.getToken()
    var sign = ""
    if (token.isNotEmpty()) {
        val parameters = hashMapOf<String, Any?>(
            "uid" to uid,
            "token" to token,
            "ts" to ts
        )
        sign = SignatureNewUtil.getSign(parameters) + "密钥"
        sign = SignatureNewUtil.md5(sign)
    }
    return sign
}

class HttpCommonHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val ts = System.currentTimeMillis()
        val sign = getSign(ts)
        val uid = Constants.getUserId()
        val token = Constants.getToken()

        val newRequest = try {
            when (originalRequest.method.uppercase()) {
                "GET" -> {
                    val newUrl = originalRequest.url.newBuilder()
                        .setQueryParameter("token", token)
                        .setQueryParameter("sign", sign)
                        .setQueryParameter("ts", ts.toString())
                        .setQueryParameter("uid", uid.toString())
                        .build()
                    originalRequest.newBuilder().url(newUrl).build()
                }
                "POST" -> {
                    val originalBody = originalRequest.body
                    if (originalBody is FormBody) {
                        val newBodyBuilder = FormBody.Builder()
                        for (i in 0 until originalBody.size) {
                            newBodyBuilder.add(originalBody.name(i), originalBody.value(i))
                        }
                        newBodyBuilder
                            .add("uid", uid.toString())
                            .add("token", token)
                            .add("sign", sign)
                            .add("ts", ts.toString())
                        originalRequest.newBuilder().post(newBodyBuilder.build()).build()
                    } else originalRequest
                }
                else -> originalRequest
            }
        } catch (e: Exception) {
            e.printStackTrace()
            originalRequest
        }

        return try {
            chain.proceed(newRequest)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }
}
