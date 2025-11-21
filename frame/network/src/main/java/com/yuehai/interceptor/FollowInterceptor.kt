package com.yuehai.interceptor

import com.yuehai.data.collection.path.parse.Res
import com.yuehai.data.collection.path.json.froJsonErrorNull
import com.yuehai.util.eventbus.BaseEvent
import com.yuehai.util.eventbus.EVENT_MESSAGE_PERSON_FOLLOW
import com.yuehai.util.eventbus.EventBusHelper
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject

class FollowInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = chain.proceed(request)

        val url = request.url.toString()
        val method = request.method
        var toUid: String? = null
        val keyName = "to_uid"

        // 获取 to_uid 参数
        if (method == "GET") {
            toUid = request.url.queryParameter(keyName)
        } else if (method == "POST") {
            val requestBody = request.body
            if (requestBody is FormBody) {
                for (i in 0 until requestBody.size) {
                    if (requestBody.name(i) == keyName) {
                        toUid = requestBody.value(i)
                        break
                    }
                }
            } else {
                try {
                    val buffer = Buffer()
                    request.body?.writeTo(buffer)
                    val bodyStr = buffer.readUtf8()
                    if (bodyStr.isNotEmpty()) {
                        val json = JSONObject(bodyStr)
                        toUid = json.optString(keyName)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 只处理关注接口的返回
        if (url.contains("api/user/follow")) {
            val responseBody = response.body
            if (response.isSuccessful && responseBody != null) {
                val contentType = responseBody.contentType()
                val responseStr = try {
                    responseBody.string()
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }

                if (responseStr.isNotEmpty()) {
                    val followInfo = froJsonErrorNull<Res<Any>>(responseStr)
                    if (followInfo?.isSuccess() == true) {
                        EventBusHelper.postSticky(BaseEvent(EVENT_MESSAGE_PERSON_FOLLOW, toUid))
                    }

                    // 记得重建 body
                    return response.newBuilder()
                        .body(responseStr.toByteArray().toResponseBody(contentType))
                        .build()
                }
            }
        }

        return response
    }
}
