package com.adealik.frame.base.loading

import com.kaopiz.kprogresshud.KProgressHUD

abstract class BaseLoadingController(
    protected val createHud: () -> KProgressHUD
) : LoadingController {

    protected var loadingDialog: KProgressHUD? = null

    /**
     * 是否在 dismissLoading() 时自动关闭
     */
    override var autoDismissLoading: Boolean = true

    /**
     * 显示 loading
     *
     * @param cancelable 是否允许返回键 / 点击外部取消
     */
    override fun showLoading(cancelable: Boolean) {
        if (loadingDialog == null) {
            loadingDialog = createHud()
        }

        loadingDialog?.setCancellable(cancelable)

        if (loadingDialog?.isShowing != true) {
            loadingDialog?.show()
        }
    }

    /**
     * 按规则关闭 loading（受 autoDismissLoading 控制）
     */
    override fun dismissLoading() {
        if (!autoDismissLoading) return

        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }

    /**
     * 强制关闭 loading（无视 autoDismissLoading）
     */
    override fun forceDismissLoading() {
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }
}
