package com.yuehai.data.collection.path

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

class YQSPUtils private constructor() {

    companion object {
        @Volatile
        private var instance: YQSPUtils? = null
        val mmkv = MMKV.defaultMMKV()

        // 获取单例实例
        fun getInstance(): YQSPUtils {
            return instance ?: synchronized(this) {
                instance ?: YQSPUtils().also { instance = it }
            }
        }

        // private const val SP_NAME = Constants.spLoginUser// SharedPreferences 文件名
    }

    //备注 Application 用法 start
//    class BaseApplication : Application() {
//
//        override fun onCreate() {
//            super.onCreate()
//            SPUtils.getInstance().init(this) // 初始化 SharedPreferences 工具类
//        }
//    }

    // 保存数据
//    SPUtils.getInstance().putString("username", "JohnDoe")
//    SPUtils.getInstance().putInt("age", 25)

    // 获取数据
//    val username = SPUtils.getInstance().getString("username")
//    val age = SPUtils.getInstance().getInt("age")

    // 打印数据
//    Log.d("MainActivity", "Username: $username, Age: $age")
    //备注 Application 用法 end

    // 保存字符串
    fun putString(key: String?, value: String) {
        if (key.isNullOrEmpty()) {
            return
        }
        mmkv.encode(key, value)
    }

    // 获取字符串
    fun getString(key: String?, defaultValue: String = ""): String {
        if (key.isNullOrEmpty()) {
            return ""
        }
        return mmkv.decodeString(key) ?: defaultValue
    }

    // 保存整数
    fun putInt(key: String, value: Int) {
        mmkv.encode(key, value)
    }

    // 获取整数
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return mmkv.decodeInt(key, defaultValue)
    }

    // 保存布尔值
    fun putBoolean(key: String, value: Boolean) {
        mmkv.encode(key, value)
    }

    // 获取布尔值
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return mmkv.decodeBool(key, defaultValue)
    }

    // 保存长整型
    fun putLong(key: String, value: Long) {
        mmkv.encode(key, value)
    }

    // 获取长整型
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return mmkv.decodeLong(key, defaultValue)
    }

    // 保存浮点数
    fun putFloat(key: String, value: Float) {
        mmkv.encode(key, value)
    }

    // 获取浮点数
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return mmkv.decodeFloat(key, defaultValue)
    }

    private val gson = Gson()

    // 保存字符串列表（历史搜索记录）
    fun putStringList(key: String, list: List<String>) {
        val json = gson.toJson(list)
        mmkv.encode(key, json)
    }

    // 获取字符串列表
    fun getStringList(key: String): MutableList<String> {
        val json = mmkv.decodeString(key)
        if (json.isNullOrEmpty()) return mutableListOf()

        return try {
            val type = object : TypeToken<MutableList<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    // 删除指定键的值
    fun remove(key: String) {
        mmkv.remove(key)
    }

    // 清除所有数据
    fun clear() {
        mmkv.clearAll()
    }

    // 清除指定数据数据
    fun clear(key: String) {
        mmkv.remove(key)
    }
}