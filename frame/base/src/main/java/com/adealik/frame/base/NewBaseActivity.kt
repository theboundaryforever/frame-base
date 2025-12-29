package com.adealik.frame.base

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.adealik.frame.base.dialogqueue.DialogQueue
import com.adealik.frame.base.dialogqueue.data.Priority
import com.adealik.frame.base.ext.fastLazy
import com.adealik.frame.base.loading.ActivityLoadingController
import com.gyf.immersionbar.ImmersionBar

open class NewBaseActivity : AppCompatActivity() {

    private val dialogQueue: DialogQueue by fastLazy {
        DialogQueue(this)
    }

    /** ⭐ 统一 loading 控制器 */
    protected val loadingController by lazy {
        ActivityLoadingController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        onBeforeCreate()
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        if (onAfterSuperCreate()) {
            immersionBarDark()

            initViews()
            if (!shouldHideNavigationBar()) {
                applyImmersiveNavigation(shouldHideNavigationBar())
            }
            initComponents()
            observeViewModel()
            loadData()
            initOthers()
        }
    }

    /* ================= loading 对外 API ================= */

    fun showLoading(cancelable: Boolean = false) = loadingController.showLoading(cancelable)

    fun dismissLoading() = loadingController.dismissLoading()

    fun forceDismissLoading() = loadingController.forceDismissLoading()

    fun setAutoDismissLoading(auto: Boolean) {
        loadingController.autoDismissLoading = auto
    }

    /* =================================================== */

    /** ⭐ ImmersionBar 方法保留 */
    open fun immersionBarDark() {
        ImmersionBar.with(this).statusBarDarkFont(true).init()
        ImmersionBar.with(this).navigationBarColor(android.R.color.black)
            .navigationBarDarkIcon(true).init()
    }

    open fun immersionBarWhite() {
        ImmersionBar.with(this).statusBarDarkFont(true).init()
        ImmersionBar.with(this).navigationBarColor(android.R.color.white)
            .navigationBarDarkIcon(false).init()
    }

    protected open fun shouldHideNavigationBar(): Boolean = false

    protected fun applyImmersiveNavigation(hide: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (hide) controller.hide(WindowInsetsCompat.Type.navigationBars())
        else controller.show(WindowInsetsCompat.Type.navigationBars())

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(v.paddingLeft, 0, v.paddingRight, if (hide) 0 else nav.bottom)
            insets
        }
    }

    open fun onBeforeCreate() {}
    open fun onAfterSuperCreate(): Boolean = true
    open fun initViews() {}
    open fun initComponents() {}
    open fun observeViewModel() {}
    open fun loadData() {}
    open fun initOthers() {}

    override fun onDestroy() {
        super.onDestroy()
        loadingController.dismissLoading()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (!containsViewPager2(window.decorView)) {
            super.onSaveInstanceState(outState)
        }
    }

    private fun containsViewPager2(view: View): Boolean {
        if (view is ViewPager2) return true
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                if (containsViewPager2(view.getChildAt(i))) return true
            }
        }
        return false
    }

    fun dialogOffer(
        tag: String,
        priority: Priority,
        fragmentManager: FragmentManager,
        dialogBuilder: Any?,
    ) {
        dialogQueue.offer(tag, priority, fragmentManager, dialogBuilder)
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = res.configuration
        if (config.fontScale != 1f) {
            config.fontScale = 1f
            res.updateConfiguration(config, res.displayMetrics)
        }
        return res
    }

    override fun finish() {
        super.finish()
        loadingController.dismissLoading()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
