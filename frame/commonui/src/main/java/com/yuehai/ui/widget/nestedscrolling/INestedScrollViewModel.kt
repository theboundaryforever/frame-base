package com.yuehai.ui.widget.nestedscrolling

import android.view.View
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView

interface INestedScrollViewModel {
    //嵌套滑动的子RecyclerView
    val childRecyclerViewLD: LiveData<RecyclerView>
    //根据此控件的位置判断吸顶
    val lastItemLD: LiveData<View>

    fun setChildRecyclerView(childRecyclerView: RecyclerView)
    fun setLastItemView(view: View)
}