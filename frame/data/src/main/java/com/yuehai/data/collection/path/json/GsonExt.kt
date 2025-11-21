package com.yuehai.data.collection.path.json

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


const val TAG_GSON = "tag_gson"

val gson by lazy {
   Gson()
}

fun toJsonErrorNull(obj: Any?): String? =
    try {
        gson.toJson(obj)
    } catch (e: Exception) {
        Log.e(TAG_GSON, "toJsonErrorNull, e:$e")
        null
    }

inline fun <reified T> froJsonErrorNull(json: String?): T? =
    try {
        gson.fromJson(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        Log.e(TAG_GSON, "froJsonErrorNull, e:$e")
        null
    }

fun <T> froJsonErrorNull(json: String?, clazz: Class<T>): T? =
    try {
        gson.fromJson(json, clazz)
    } catch (e: Exception) {
        Log.e(TAG_GSON, "froJsonErrorNull, e:$e")
        null
    }

inline fun <reified T> froJsonErrorNullWithStr(json: String?): T? {
    if (json.isNullOrBlank()) return null
    return try {
        val trimmed = json.trim()

        // 如果是以引号包裹的字符串 -> 解开再解析
        val realJson = if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            // 去掉外层引号，并反序列化里面的转义 JSON
            JsonParser.parseString(trimmed).asString
        } else {
            trimmed
        }

        // 解析真正的 JSON
        val element = JsonParser.parseString(realJson)
        gson.fromJson<T>(element, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        Log.e(TAG_GSON, "froJsonErrorNull error: $e\njson=$json")
        null
    }
}

