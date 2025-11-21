package com.yuehai.ui.widget.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val fragments: List<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    /**
     * 保证每个 fragment 的唯一 ID，避免 ViewPager2 页面复用导致的错乱。
     * 这里使用 fragment 的 hashCode 或你可以使用 position + 自定义 key 作为唯一值。
     */
    override fun getItemId(position: Int): Long {
        return fragments[position].hashCode().toLong()
    }

    /**
     * 用于判断当前的 itemId 是否还在 adapter 中，避免删除 item 后 ViewPager2 复用旧页面。
     */
    override fun containsItem(itemId: Long): Boolean {
        return fragments.any { it.hashCode().toLong() == itemId }
    }

    /**
     * 类型安全地获取 Fragment
     */
    fun <T : Fragment> getFragment(position: Int, clazz: Class<T>): T? {
        val fragment = fragments[position]
        return if (clazz.isInstance(fragment)) {
            clazz.cast(fragment)
        } else {
            null
        }
    }

    @Deprecated("请使用 getFragment(position: Int, clazz: Class<T>)")
    fun <T : Fragment> getFragment(position: Int): T? {
        return fragments[position] as? T
    }
}
