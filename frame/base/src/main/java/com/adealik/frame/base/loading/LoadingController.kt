package com.adealik.frame.base.loading

interface LoadingController {

    /** 是否在宿主销毁 / dismiss 时自动关闭 loading */
    var autoDismissLoading: Boolean

    /** 显示 loading */
    fun showLoading(cancelable: Boolean = false)

    /** 按规则关闭 loading（受 autoDismissLoading 控制） */
    fun dismissLoading()

    /** 强制关闭 loading（无视 autoDismissLoading） */
    fun forceDismissLoading()
}