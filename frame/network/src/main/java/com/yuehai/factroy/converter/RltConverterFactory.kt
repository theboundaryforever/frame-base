package com.yuehai.factroy.converter

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.yuehai.data.collection.path.Rlt
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


@Suppress("UNCHECKED_CAST")
class RltConverterFactory(private val gson: Gson) : Converter.Factory() {

    companion object {

        fun create(gson: Gson): RltConverterFactory {
            return RltConverterFactory(gson)
        }

    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        val typeToken: TypeToken<out Any> = if (getRawType(type) == Rlt::class.java) {
            TypeToken.get(getParameterUpperBound(0, type as ParameterizedType))
        } else {
            TypeToken.get(type)
        }
        val adapter: TypeAdapter<out Any> = gson.getAdapter(typeToken)
        return RltResponseBodyConverter(gson, adapter as TypeAdapter<Any>)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody> {
        val adapter: TypeAdapter<out Any> = gson.getAdapter(TypeToken.get(type))
        return RltRequestBodyConverter(gson, adapter as TypeAdapter<Any>)
    }

}