package com.yuehai.factroy

import com.yuehai.data.collection.path.parse.Res
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiCallWrapper<T>(
    private val delegate: Call<T>
) : Call<T> {

    override fun enqueue(callback: Callback<T>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body is Res<*>) {
                        if (body.isSuccess()) {
                            @Suppress("UNCHECKED_CAST")
                            callback.onResponse(this@ApiCallWrapper, Response.success(body as T))
                        } else {
                            callback.onFailure(this@ApiCallWrapper, ApiException(body.code, body.msg ?: "Unknown error"))
                        }
                    } else {
                        // 不是BaseResponse的直接返回
                        callback.onResponse(this@ApiCallWrapper, Response.success(body))
                    }
                } else {
                    callback.onFailure(this@ApiCallWrapper, ApiException(response.code(), response.message()))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onFailure(this@ApiCallWrapper, t)
            }
        })
    }

    override fun clone(): Call<T> = ApiCallWrapper(delegate.clone())

    override fun execute(): Response<T> = throw UnsupportedOperationException("ApiCallAdapter doesn't support execute")

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request() = delegate.request()

    override fun timeout() = delegate.timeout()
}
