package com.yuehai.util.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.reflect.KProperty


abstract class TypeDelegationPrefs(
    val prefs: () -> SharedPreferences,
    val userId: (() -> String)? = null
) {

    open fun clearAll() {
        prefs().edit {
            clear()
        }
    }

    open inner class PrefKey<T : Any>(
        protected open val key: String,
        private val default: T
    ) {

        @Suppress("UNCHECKED_CAST")
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            with(prefs()) {
                return when (default) {
                    is String -> getString(key, default) as T
                    is Int -> getInt(key, default) as T
                    is Long -> getLong(key, default) as T
                    is Boolean -> getBoolean(key, default) as T
                    is Float -> getFloat(key, default) as T
                    is Set<*> -> getStringSet(key, default as Set<String>) as T
                    else -> error("not support type ${default::class.java}")
                }
            }
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            prefs().edit {
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Set<*> -> putStringSet(key, value as Set<String>)
                    else -> error("not support type ${default::class.java}")
                }
            }
        }
    }

    inner class PrefUserKey<T : Any>(
        key: String,
        default: T
    ) : PrefKey<T>(key, default) {

        override val key: String = key
            get() {
                return field + requireNotNull(
                    userId,
                    { "[PrefUserKey] should use after [TypeDelegationPrefs.userId] imps" }).invoke()
            }
    }
}