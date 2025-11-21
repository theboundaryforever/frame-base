package com.adealik.frame.mvvm.view

import androidx.lifecycle.LifecycleOwner


interface IViewComponent {

    val lifecycleOwner: LifecycleOwner

    fun attach(): ViewComponent

}