package com.adealik.frame.base

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
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
import com.gyf.immersionbar.ImmersionBar
import com.kaopiz.kprogresshud.KProgressHUD
import com.yuehai.util.util.getCompatColor


open class NewBaseActivity : AppCompatActivity() {
    private val dialogQueue: DialogQueue by fastLazy {
        DialogQueue(this)
    }

    private var loadingDialog: KProgressHUD? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        onBeforeCreate()
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        val initAfter = onAfterSuperCreate()
        if (initAfter) {
            ImmersionBar.with(this).statusBarDarkFont(true).init()
            ImmersionBar.with(this).navigationBarColor(android.R.color.black)
                .navigationBarDarkIcon(true).init()
            initViews()
            if(!shouldHideNavigationBar()){
                applyImmersiveNavigation(hideNavigationBar = shouldHideNavigationBar())
            }
            initComponents()
            observeViewModel()
            loadData()
            initOthers()
        }
    }

    /**
     * 子类通过 override 控制是否隐藏底部导航栏（默认 false，即显示）
     */
    protected open fun shouldHideNavigationBar(): Boolean = false

    /**
     * 控制导航栏显示/隐藏
     */
    protected fun applyImmersiveNavigation(hideNavigationBar: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (hideNavigationBar) {
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }

        // 自动获取根布局（整个内容区域）
        val rootView = findViewById<View>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            // 仅设置 paddingBottom，使内容不被底部导航栏遮挡；不处理顶部
            view.setPadding(
                view.paddingLeft,
                0, // 不抬高状态栏
                view.paddingRight,
                if (hideNavigationBar) 0 else navBarInsets.bottom
            )

            insets
        }
    }



    open fun immersionBarDark() {
        ImmersionBar.with(this).statusBarDarkFont(true).init()
        ImmersionBar.with(this).navigationBarColor(android.R.color.black)
            .navigationBarDarkIcon(true).init()
    }

    open fun immersionBarWhite() {
        ImmersionBar.with(this).statusBarDarkFont(true).init()
        ImmersionBar.with(this).navigationBarColor(android.R.color.black)
            .navigationBarDarkIcon(true).init()
    }


    open fun onBeforeCreate() {

    }

    open fun onAfterSuperCreate(): Boolean {
        return true
    }

    open fun initViews() {

    }

    open fun initComponents() {

    }

    open fun observeViewModel() {

    }

    open fun loadData() {

    }

    /**
     * 加载数据之后初始化一些不紧急的内容
     */
    open fun initOthers() {

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNewIntent(intent)
    }

    open fun handleNewIntent(intent: Intent?) {

    }


    private fun initLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
        }
    }

    fun showLoading() {
        initLoadingDialog()
        loadingDialog?.show()
    }

    fun dismissLoading() {
        loadingDialog?.dismiss()
    }


    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
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
                if (containsViewPager2(view.getChildAt(i))) {
                    return true
                }
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}
