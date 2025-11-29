package com.yuehai.data.collection.path

const val SERVICE_SUCCESS = 1

/**
 * 具体error继承自IError，命名规范：<模块><功能><具体错误>
 * msg: msg是提示给用户的信息，msg为null是不做提示
 * serverCode: 服务端返回的错误码
 * exception: 特定场景异常收集，方便打印日志排查问题
 * data: 附属数据
 * toastSecond: 错误提示需要展示的秒数
 */
open class IError(
    open val msg: String = "",
    open var serverCode: Int = 0,
    open val exception: Throwable? = null,
    open var data: Any? = null,
) {

    override fun toString(): String {
        return "${this.javaClass.simpleName}(msg='$msg', serverCode=$serverCode, exception=$exception, data=$data)"
    }

    fun getStatError() =
        "${this.javaClass.simpleName}_${serverCode}_${msg}_${exception?.message ?: ""}"

}

/**
 * 切换状态时相同
 */
class CommonSwitchStateSameError : IError()

/**
 * 网络结果解析为null错误
 */
class NetworkResultNullError : IError()

/**
 * 网络返回结果json解析错误
 */
class NetworkResJsonParseError : IError()


sealed class Rlt<out R> {

    data class Success<out T>(val data: T) : Rlt<T>()

    data class Failed(val error: IError) : Rlt<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Failed -> "Failed[error=$error]"
        }
    }

}