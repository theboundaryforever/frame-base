package com.yuehai.ui.widget.nestedscrolling

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.adealik.frame.mvvm.viewmodel.BaseViewModel

class NestedScrollViewModel: BaseViewModel(), INestedScrollViewModel {
    override val childRecyclerViewLD: LiveData<RecyclerView> = MutableLiveData()
    override val lastItemLD: LiveData<View> = MutableLiveData()

    override fun setChildRecyclerView(childRecyclerView: RecyclerView) {
        childRecyclerViewLD.send(childRecyclerView)
    }

    override fun setLastItemView(view: View) {
        lastItemLD.send(view)
    }
}