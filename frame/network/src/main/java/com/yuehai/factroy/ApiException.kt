package com.yuehai.factroy

import com.yuehai.data.collection.path.IError
import java.io.IOException

class ApiException(
    val code: Int,
    override val message: String
) : RuntimeException(message)

class NoNetworkException(
) : IError()

class ServerErrorException(
    val code: Int,
    override val message: String,
    val rawHtml: String
) : IOException(message)

