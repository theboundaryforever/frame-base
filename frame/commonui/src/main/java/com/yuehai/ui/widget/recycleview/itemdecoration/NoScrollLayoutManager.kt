package com.yuehai.ui.widget.recycleview.itemdecoration

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NoScrollLayoutManager : LinearLayoutManager {

    // 适配各种构造函数
    constructor(context: Context) : super(context)
    constructor(context: Context, orientation: Int, reverseLayout: Boolean) :
            super(context, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    // 【核心代码】禁止垂直滑动
    override fun canScrollVertically(): Boolean {
        return false
    }

    // 【核心代码】禁止水平滑动
    override fun canScrollHorizontally(): Boolean {
        return false
    }
}