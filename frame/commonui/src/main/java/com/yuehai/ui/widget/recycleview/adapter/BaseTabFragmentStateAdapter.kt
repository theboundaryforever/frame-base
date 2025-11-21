package com.yuehai.ui.widget.recycleview.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

abstract class BaseTabFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    abstract fun getTabName(pos: Int): String
}

abstract class BaseActivityTabFragmentStateAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    abstract fun getTabName(pos: Int): String
}