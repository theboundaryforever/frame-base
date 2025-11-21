package com.yuehai.ui.widget.nestedscrolling

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.layout.SmartRefreshLayout
// 假设 com.yuehai.ui.widget.viewmodel.WEUIViewModelFactory 和 NestedScrollViewModel 存在
// 避免编译错误，将辅助类定义放在此处（如果它们不在外部文件中）

import kotlin.math.min

/**
 * 辅助类定义（如果它们不在外部文件中）
 */
private class WEUIViewModelFactory : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        // 请根据您的实际 ViewModel 实现进行调整
        if (modelClass.isAssignableFrom(NestedScrollViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NestedScrollViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


/**
 * 处理RecyclerView 套viewPager， viewPager内的fragment中 也有RecyclerView，处理外层、内层 RecyclerView的嵌套滑动问题
 * 类似淘宝、京东首页
 */
class NestedScrollingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) :
    FrameLayout(context, attrs, defStyleAttr), NestedScrollingParent3 {

    /** 嵌套滑动的父SmartRefreshLayout */
    private var parentSmartRefreshLayout: SmartRefreshLayout? = null

    /** 嵌套滑动的父RecyclerView */
    private var parentRecyclerView: RecyclerView? = null

    /** 嵌套滑动的子RecyclerView */
    private var childRecyclerView: RecyclerView? = null

    /** 外层RecyclerView的最后一个item，即：tab + viewPager，用于判断 滑动 临界位置 */
    private var lastItemView: View? = null

    /** 特殊场景，可能顶部会有内间距，默认为0即可 */
    private var topPadding: Int = 0

    private val nestedScrollingParentHelper = NestedScrollingParentHelper(this)
    private var nestedScrollViewModel: NestedScrollViewModel? = null

    // 辅助函数：获取 Target 的名称
    private fun getTargetName(target: View): String {
        return when (target) {
            parentRecyclerView -> "parentRv"
            childRecyclerView -> "childRv"
            parentSmartRefreshLayout -> "parentSrl"
            else -> target.javaClass.simpleName
        }
    }

    fun setTarget(target: LifecycleOwner?) {
        Log.d(TAG, "setTarget called. Target: ${target?.javaClass?.simpleName}")

        nestedScrollViewModel = when (target) {
            is FragmentActivity -> {
                ViewModelProvider(target, WEUIViewModelFactory())[NestedScrollViewModel::class.java]
            }
            is Fragment -> {
                ViewModelProvider(target, WEUIViewModelFactory())[NestedScrollViewModel::class.java]
            }
            else -> {
                Log.e(TAG, "Target is neither FragmentActivity nor Fragment. ViewModel might not work.")
                null
            }
        }

        // 使用完整的 Observer 接口对象
        nestedScrollViewModel?.childRecyclerViewLD?.observe(target!!, object : Observer<RecyclerView?> {
            override fun onChanged(t: RecyclerView?) {
                childRecyclerView = t
                Log.d(TAG, "ViewModel set childRecyclerView: $t")
            }
        })

        nestedScrollViewModel?.lastItemLD?.observe(target!!, object : Observer<View?> {
            override fun onChanged(t: View?) {
                lastItemView = t
                Log.d(TAG, "ViewModel set lastItemView: $t")
            }
        })
    }

    /**
     * 判断父 view 是否接受嵌套滑动。
     */
    override fun onStartNestedScroll(
        child: View,
        target: View,
        nestedScrollAxes: Int,
        type: Int,
    ): Boolean {
        // [核心修正]: 移除所有动态 setEnableRefresh 逻辑，解决二次刷新问题。
        // 允许 SRL 始终参与嵌套滑动，依赖 onNestedPreScroll 的消耗机制来阻止过早介入。
        val result = nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL

        // 确保 SRL 至少在手势开始时是启用的（如果它自己没有额外的限制）
        parentSmartRefreshLayout?.setEnableRefresh(true)

        Log.d(TAG, "onStartNestedScroll | SRL is NOT dynamically controlled here.")

        return result
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    /**
     * 父 view 是否优先于子 view 处理滑动 (预消耗)
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val lastItemTop = lastItemView?.top ?: run {
            Log.w(TAG, "onNestedPreScroll | lastItemView is null, returning.")
            return
        }

        val targetName = getTargetName(target)
        val scrollDirection = if (dy > 0) "UP" else "DOWN"

        Log.d(TAG, "--- PreScroll START | Target: $targetName, dy: $dy ($scrollDirection), LastItemTop: $lastItemTop, TopPadding: $topPadding ---")

        // -------------------------------------------------------------
        // ** 【统一优先级处理 Header 展开/收起】**
        // -------------------------------------------------------------
        if (lastItemTop > topPadding) {
            // Header 是可见的（未完全固定）

            if (dy > 0) {
                // 向上滑 (UP): 必须优先消耗事件来收起 Header (PIN)
                val consume = minOf(dy, lastItemTop - topPadding)
                parentRecyclerView?.scrollBy(0, consume)
                consumed[1] = consume
                Log.d(TAG, "PreScroll | FORCED UP CONSUMPTION (Header Visible). Consumed $consume to PIN Header.")
                return
            }

            if (dy < 0) {
                // 向下滑 (DOWN): 必须优先消耗事件来展开 Header

                if (parentRecyclerView?.canScrollVertically(dy) == true) {
                    // Parent RV 还能继续展开 Header
                    parentRecyclerView?.scrollBy(0, dy)
                    consumed[1] = dy // 消耗全部 dy
                    Log.d(TAG, "PreScroll | DOWN (Header Visible, Max Expand Not Reached). Consumed $dy.")
                } else {
                    // Parent RV 无法再向下滚动（已达到最大展开极限）
                    consumed[1] = 0 // 不消耗，释放给 SRL/下一级父 View
                    Log.d(TAG, "PreScroll | DOWN (Header Visible, Max Expand Reached). Consumed 0. Event released to SRL.")
                }
                return
            }
        }
        // -------------------------------------------------------------

        // 剩下的逻辑只处理 Header 已经固定（lastItemTop <= topPadding）的情况

        if (target === parentRecyclerView) {
            onParentScrolling(lastItemTop, dy, consumed)
        } else if (target === childRecyclerView) {
            onChildScrolling(lastItemTop, dy, consumed)
        } else if (target == parentSmartRefreshLayout) {
            onParentSmartRefreshLayoutScrolling(lastItemTop, dy, consumed)
        }

        Log.d(TAG, "--- PreScroll END | Target: $targetName, ConsumedY: ${consumed[1]} ---")
    }

    /**
     * 处理 Parent RV 触发的滚动 (现在只处理 Header 固定状态下的逻辑)
     */
    private fun onParentScrolling(lastItemTop: Int, dy: Int, consumed: IntArray) {
        // 逻辑简化：Header 未固定状态由 onNestedPreScroll 顶部的优先逻辑处理
        if (lastItemTop <= topPadding) {
            // 列表已置顶 (Header 已收起)
            if (dy > 0) {
                // 上滑：将事件传递给内层 RecyclerView
                childRecyclerView?.scrollBy(0, dy)
                consumed[1] = dy
            } else {
                // 下滑：如果内层能继续下滑，让内层继续
                if (childRecyclerView?.canScrollVertically(dy) == true) {
                    consumed[1] = dy
                    childRecyclerView?.scrollBy(0, dy)
                }
                // 否则（内层到顶），事件不消耗，让事件冒泡。
            }
        }
    }

    /**
     * 处理 Child RV 触发的滚动 (现在只处理 Header 固定状态下的逻辑)
     */
    private fun onChildScrolling(lastItemTop: Int, dy: Int, consumed: IntArray) {
        // 逻辑简化：Header 未固定状态由 onNestedPreScroll 顶部的优先逻辑处理
        if (lastItemTop <= topPadding) {
            // 列表已置顶 (Header 已收起)

            if (dy < 0) {
                // 向下滑（希望展开 Header 或触发 SRL 刷新）
                val innerCanScrollDown = childRecyclerView?.canScrollVertically(-1) ?: false

                if (!innerCanScrollDown) {
                    // 内层已到顶

                    if (parentRecyclerView?.canScrollVertically(dy) == true) {
                        // 1. Parent RV 还能拉动：让 Parent RV 消耗事件来展开 Header
                        parentRecyclerView?.scrollBy(0, dy)
                        consumed[1] = dy
                        Log.d(TAG, "onChildScrolling | DOWN: Child at top. Parent RV consuming $dy to expand Header.")
                    } else {
                        // 2. 内层和 Parent RV 都不能再下拉了。放行给 SRL
                        consumed[1] = 0
                        Log.d(TAG, "onChildScrolling | DOWN: Child/Parent at top, Consumed 0. Event **PASSED TO SRL**.")
                    }
                }
                // 否则（内层未到顶），让内层自己滚 (Parent 不预消耗)
            }
            // 否则（上滑），Header 已经收完，让 Child RV 自己滚 (Parent 不预消耗)
        }
    }

    /**
     * 处理 SmartRefreshLayout 作为 Target 的滚动 (现在只处理 Header 固定状态下的逻辑)
     */
    private fun onParentSmartRefreshLayoutScrolling(lastItemTop: Int, dy: Int, consumed: IntArray) {
        // 逻辑简化：Header 未固定状态由 onNestedPreScroll 顶部的优先逻辑处理
        if (dy > 0) {
            // 向上滑 (收起 Header)
            val consume = minOf(dy, lastItemTop - topPadding)
            if (consume > 0) {
                // Header 未收完，继续收起 Header
                parentRecyclerView?.scrollBy(0, consume)
                consumed[1] = consume
            } else {
                // Header Pinned，事件交给 Child RV
                childRecyclerView?.scrollBy(0, dy)
                consumed[1] = dy
            }
        } else {
            // 向下滑 (展开 Header 或触发刷新)
            val innerCanScrollDown = childRecyclerView?.canScrollVertically(-1) ?: false
            val parentCanScrollDown = parentRecyclerView?.canScrollVertically(dy) ?: false

            if (lastItemTop <= topPadding) {
                // Header 已收起/固定
                if (!innerCanScrollDown) {
                    // 内层已到顶
                    if (parentCanScrollDown) {
                        // 强制让 Parent RV 消耗事件来展开 Header
                        parentRecyclerView?.scrollBy(0, dy)
                        consumed[1] = dy
                    } else {
                        // 内层和 Parent RV 都不能再下拉了，不消耗，让事件留给 SRL
                        consumed[1] = 0
                    }
                } else {
                    // 内层未到顶，让内层消耗
                    childRecyclerView?.scrollBy(0, dy)
                    consumed[1] = dy
                }
            }
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray,
    ) {
        // ...
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
    ) {
        // ...
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean,
    ): Boolean {
        return false
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)
        // 恢复 SRL 启用状态，为下次拖动做准备
        parentSmartRefreshLayout?.setEnableRefresh(true)
        Log.d(TAG, "onStopNestedScroll | Target: ${getTargetName(target)}, Type: $type")
    }

    /**
     * 设置嵌套滑动的外部RecyclerView
     */
    fun setParentRecyclerView(
        parentRecyclerView: RecyclerView?,
        parentSmartRefreshLayout: SmartRefreshLayout? = null,
    ) {
        this.parentRecyclerView = parentRecyclerView
        this.parentSmartRefreshLayout = parentSmartRefreshLayout
        Log.d(TAG, "setParentRecyclerView set. Parent: $parentRecyclerView, SRL: $parentSmartRefreshLayout")
    }

    fun setTopPadding(paddingTop: Int) {
        this.topPadding = paddingTop
        Log.d(TAG, "setTopPadding: $paddingTop")
    }

    companion object {
        private const val TAG = "NestedScrollingLayout"
    }
}