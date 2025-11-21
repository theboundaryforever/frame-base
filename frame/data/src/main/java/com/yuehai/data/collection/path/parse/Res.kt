package com.yuehai.data.collection.path.parse

import com.google.gson.annotations.SerializedName

open class Res<T> {

    @SerializedName("code")
    open var code: Int = 0

    @SerializedName("msg")
    open var msg: String? = null

    @SerializedName("data")
    val data: T? = null

    override fun toString(): String {
        return "code:$code, message:$msg, data:$data"
    }

    fun isSuccess(): Boolean {
        return code == 1
    }

    data class Exception(val exception: Throwable) : Res<Nothing>()
}