package com.yuehai.ui.widget.recycleview.layoutmanager

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FullyLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        val height = View.MeasureSpec.getSize(heightSpec)
        super.onMeasure(recycler, state, widthSpec, View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
    }
}
