package com.yuehai.factroy.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonToken
import com.yuehai.data.collection.path.IError
import com.yuehai.data.collection.path.NetworkResJsonParseError
import com.yuehai.data.collection.path.NetworkResultNullError
import com.yuehai.data.collection.path.Rlt
import com.yuehai.data.collection.path.SERVICE_SUCCESS
import com.yuehai.data.collection.path.parse.Res
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

internal class RltResponseBodyConverter<T>(
    private val gson: Gson,
    private val adapter: TypeAdapter<T>,
) : Converter<ResponseBody, Rlt<T>> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): Rlt<T> {
        val jsonReader = gson.newJsonReader(value.charStream())
        value.use {
            val result: T? = adapter.read(jsonReader)
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                return Rlt.Failed(NetworkResJsonParseError())
            }

            if (result == null) {
                return Rlt.Failed(NetworkResultNullError())
            }

            if (result !is Res<*>) {
                return Rlt.Failed(NetworkResJsonParseError())
            }

            if (result.code == SERVICE_SUCCESS) {
                return Rlt.Success(result)
            }


            //业务侧需要增加一层serverCode到具体IError转换
            return Rlt.Failed(
                IError(
                    result.msg ?: "",
                    result.code,
                    Exception(),
                    result.data,
                )
            )
        }
    }

}