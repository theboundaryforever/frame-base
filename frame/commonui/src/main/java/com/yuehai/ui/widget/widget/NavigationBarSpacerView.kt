package com.yuehai.ui.widget.widget

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NavigationBarSpacerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var navigationBarHeight = 0
    private var lastVisible = false
    private var lastHeight = -1

    /** 是否可见回调，参数：isVisible 是否显示导航栏，height 实际设置的高度 */
    var onNavigationBarChanged: ((isVisible: Boolean, height: Int) -> Unit)? = null

    init {
        setBackgroundColor(Color.TRANSPARENT) // 可自定义颜色

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->

                // 获取导航栏高度
                val rawHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                } else {
                    insets.systemWindowInsetBottom
                }
                Log.d("NavigationBarSpacerView,rawHeight:","$rawHeight")
                // 判断是否可见
                val isVisible = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.isVisible(WindowInsetsCompat.Type.navigationBars())
                } else {
                    rawHeight > 0
                }


                // 🧠 节流优化：只有在值变化时才执行逻辑
                if (rawHeight != lastHeight || isVisible != lastVisible) {
                    lastHeight = rawHeight
                    lastVisible = isVisible
                    navigationBarHeight = rawHeight

                    visibility = if (isVisible && rawHeight > 0) View.VISIBLE else View.GONE

                    layoutParams = layoutParams?.apply {
                        height = rawHeight
                    } ?: LayoutParams(LayoutParams.MATCH_PARENT, rawHeight)

                    requestLayout()

                    // 回调通知
                    onNavigationBarChanged?.invoke(isVisible, rawHeight)
                }

                insets
            }

            // 首次 attach 时触发 Insets 分发
            if (isAttachedToWindow) {
                ViewCompat.requestApplyInsets(this)
            } else {
                addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        ViewCompat.requestApplyInsets(v)
                        removeOnAttachStateChangeListener(this)
                    }

                    override fun onViewDetachedFromWindow(v: View) {}
                })
            }
        } else {
            // API < 20：无导航栏
            visibility = View.GONE
            layoutParams = layoutParams?.apply {
                height = 0
            } ?: LayoutParams(LayoutParams.MATCH_PARENT, 0)

            onNavigationBarChanged?.invoke(false, 0)
        }
    }

    /** 用于主动获取当前 Spacer 是否显示 */
    fun isSpacerVisible(): Boolean {
        return visibility == View.VISIBLE && navigationBarHeight > 0
    }
}
