package com.yuehai.ui.widget.viewpager

import android.annotation.SuppressLint
import android.util.Log
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

data class Tab(val titleResId: Int, val instance: () -> Fragment)

class TabsPagerAdapter : FragmentStateAdapter {

    private var tabs: List<Tab> = arrayListOf()
    // 保持手动缓存的定义，但实际获取时不再依赖它
    private val fragments = SparseArray<Fragment>()

    // 宿主 FragmentActivity 或 Fragment，用于获取 FragmentManager
    private var host: Any? = null

    constructor(fragment: Fragment) : super(fragment) {
        this.host = fragment
    }

    constructor(fragment: Fragment, tabs: List<Tab>) : super(fragment) {
        this.tabs = tabs
        this.host = fragment
    }

    constructor(fragmentActivity: FragmentActivity, tabs: List<Tab>) : super(fragmentActivity) {
        this.tabs = tabs
        this.host = fragmentActivity
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(newTabs: List<Tab>) {
        this.tabs = newTabs
        // 保持清除缓存，尽管我们不再依赖它，但这是为了在数据结构变化时清除旧状态
        this.fragments.clear()
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return tabs[position].titleResId.toLong()
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    // ⚠️ 核心修改 1：移除 createFragment 中的手动缓存
    override fun createFragment(position: Int): Fragment {
        // 移除 .apply { fragments.put(position, this) }
        // FragmentStateAdapter 会通过 FragmentManager 自动处理 Fragment 实例
        return tabs[position].let { it.instance() }
    }

    /**
     * 【已修改】可靠地获取 ViewPager2 中的 Fragment 实例。
     * * @param position 要获取的 Fragment 的位置。
     * @param clazz 期望返回的 Fragment 类型。
     * @return 找到的 Fragment 实例，或 null。
     */
    fun <T : Fragment> getFragment(position: Int, clazz: Class<T>): T? {
        // 保持日志输出，但获取逻辑不再依赖 fragments 数组

        // 查找 Fragment 的 Tag
        if (position < 0 || position >= tabs.size) return null

        val itemId = getItemId(position)
        // FragmentStateAdapter 使用 "f" + itemId 作为 Fragment 在 FragmentManager 中的 Tag
        val tag = "f$itemId"

        // 1. 获取 FragmentManager
        val fm = when (host) {
            is FragmentActivity -> (host as FragmentActivity).supportFragmentManager
            is Fragment -> (host as Fragment).childFragmentManager
            else -> {
                Log.e("TabsPagerAdapter", "无法获取宿主 FragmentManager")
                return null
            }
        }

        // 2. 通过 Tag 从 FragmentManager 中查找 Fragment
        val fragment = fm.findFragmentByTag(tag)

        // 3. 校验并返回
        return if (clazz.isInstance(fragment)) {
            clazz.cast(fragment)
        } else {
            null
        }
    }

    @Deprecated("还是可能抛出ClassCastException，使用getFragment(position: Int, clazz: Class<T>)替代")
    fun <T : Fragment> getFragment(position: Int): T? {
        // 由于我们不再依赖 SparseArray，这个旧方法也无法工作，
        // 但为了兼容旧代码，可以调用新的可靠方法：
        return getFragment(position, Fragment::class.java) as? T
    }

    fun getTab(position: Int): Tab {
        return tabs[position]
    }

}