package com.yuehai.util.util.ext

fun extractFileName(url: String): String {
    return url.substringAfterLast("/")
}

fun extractFileNameWithoutSuffix(url: String): String {
    val nameWithExt = url.substringAfterLast("/")
    return nameWithExt.substringBeforeLast(".", nameWithExt)
}
