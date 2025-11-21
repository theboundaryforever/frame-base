package com.yuehai.interceptor

import com.yuehai.network.R
import com.yuehai.util.util.NetworkUtil
import com.yuehai.util.util.getCompatString
import com.yuehai.util.util.toast.showToast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorCatchingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val response = chain.proceed(chain.request())

            if (response.code == 502 || response.code == 503 || response.code == 504) {
                if (!NetworkUtil.isNetworkAvailable()) {
                    showToast(getCompatString(R.string.network_network_error))
                }
                return makeFakeResponse(chain, getCompatString(R.string.network_network_error))
            }

            response
        } catch (e: Exception) {
            e.printStackTrace()

            makeFakeResponse(chain, getErrorMessage(e))
        }
    }

    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is ConnectException, is UnknownHostException -> getCompatString(R.string.network_network_error)
            is SocketTimeoutException -> getCompatString(R.string.network_timeout_error)
            else -> "${getCompatString(R.string.network_request_error)} ${
                e.localizedMessage ?: getCompatString(R.string.network_unknow_error)
            }"
        }
    }

    private fun makeFakeResponse(chain: Interceptor.Chain, errorMessage: String): Response {
        val safeMsg = errorMessage.replace("\"", "")
            .ifBlank { getCompatString(R.string.network_network_error) }

        val fakeJson = """
            {
                "code": -1,
                "msg": "$safeMsg",
                "data": null
            }
        """.trimIndent()

        return Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200) // Retrofit不会抛异常
            .message("Fake Error Response")
            .body(fakeJson.toResponseBody("application/json".toMediaTypeOrNull()))
            .addHeader("X-Fake-Response", "true")
            .build()
    }
}
