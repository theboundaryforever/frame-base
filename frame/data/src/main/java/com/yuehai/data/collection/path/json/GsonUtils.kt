package com.yuehai.data.collection.path.json

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Gson 工具类，封装常用的 JSON 解析和转换操作。
 * 使用单例模式，确保全局只有一个 Gson 实例。
 */
object GsonUtils {

    private val gson = Gson()

    /**
     * 将 JSON 字符串解析为单个实体类对象
     * @param json JSON 字符串
     * @param clazz 目标实体类的 Class 对象
     * @return 解析后的实体类对象
     */
    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    /**
     * 将 JSON 字符串解析为实体类数组
     * @param json JSON 字符串
     * @param clazz 目标实体类的 Class 对象
     * @return 解析后的实体类数组
     */
    fun <T> fromJsonArray(json: String, clazz: Class<T>): List<T> {
        val type = TypeToken.getParameterized(List::class.java, clazz).type
        return gson.fromJson(json, type)
    }

    /**
     * 将实体类对象转换为 JSON 字符串
     * @param obj 实体类对象
     * @return 转换后的 JSON 字符串
     */
    fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }

    /**
     * 将实体类数组转换为 JSON 字符串
     * @param list 实体类数组
     * @return 转换后的 JSON 字符串
     */
    fun <T> toJsonArray(list: List<T>): String {
        return gson.toJson(list)
    }


    /**
     * 使用方法
     * val json = """{"name": "John", "age": 25}"""
     *             val user: User = GsonUtils.fromJson(json, User::class.java)
     *             println("User数据: ${user.name}, ${user.age}") // 输出: User: John, 25
     *
     *             val json1 = """[{"name": "John", "age": 25}, {"name": "Alice", "age": 30}]"""
     *             val userList: List<User> = GsonUtils.fromJsonArray(json1, User::class.java)
     *             userList.forEach { user1 ->
     *                 println("User数据: ${user1.name}, ${user1.age}")
     *             }
     */
}