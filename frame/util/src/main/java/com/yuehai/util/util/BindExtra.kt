package com.yuehai.util.util


@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class BindExtra(
    val name: String = "",
    val desc: String = "",
    val must: Boolean = true
)
