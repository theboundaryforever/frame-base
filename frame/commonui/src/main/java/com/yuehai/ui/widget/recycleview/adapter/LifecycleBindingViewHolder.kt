package com.yuehai.ui.widget.recycleview.adapter

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


/**
 * 自定义ViewHolder的生命周期
 * Created by XiaoDongLin.
 * Date: 2024/9/10
 */
open class LifecycleBindingViewHolder<T : ViewBinding>(val binding: T) :
    RecyclerView.ViewHolder(binding.root), LifecycleOwner {

    init {
        val itemView = binding.root
        itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(p0: View) {
                onCreate()
            }

            override fun onViewDetachedFromWindow(p0: View) {
                itemView.removeOnAttachStateChangeListener(this)
                onDestroy()
            }
        })
    }

    private val lifecycleRegistry: LifecycleRegistry by lazy { LifecycleRegistry(this) }

    fun onCreate() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry


}