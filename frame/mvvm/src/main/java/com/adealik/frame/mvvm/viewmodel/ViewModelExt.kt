package com.adealik.frame.mvvm.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.adealik.frame.mvvm.view.ViewComponent
import kotlin.reflect.KClass

@MainThread
inline fun <reified VM : ViewModel> ViewComponent.viewModels(
    noinline ownerProducer: () -> ViewModelStoreOwner = { fragment ?: activity!! },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) = createViewModelLazy(VM::class, { ownerProducer().viewModelStore }, factoryProducer)


@MainThread
inline fun <reified VM : ViewModel> ViewComponent.activityViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) = createViewModelLazy(VM::class, { activity!!.viewModelStore }, factoryProducer)


@MainThread
fun <VM : ViewModel> ViewComponent.createViewModelLazy(
    viewModelClass: KClass<VM>,
    storeProducer: () -> ViewModelStore,
    factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        val application = activity?.application ?: throw IllegalStateException(
            "ViewModel can be accessed only when Fragment is attached"
        )
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
    return ViewModelLazy(viewModelClass, storeProducer, factoryPromise)
}