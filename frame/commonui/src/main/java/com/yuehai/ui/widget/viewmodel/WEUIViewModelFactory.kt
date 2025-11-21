package com.yuehai.ui.widget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yuehai.ui.widget.nestedscrolling.NestedScrollViewModel

@Suppress("UNCHECKED_CAST")
class WEUIViewModelFactory : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(NestedScrollViewModel::class.java) ->
                    NestedScrollViewModel()
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            } as T
        }
    }

}