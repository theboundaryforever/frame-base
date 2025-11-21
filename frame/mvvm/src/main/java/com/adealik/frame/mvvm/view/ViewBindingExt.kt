package com.adealik.frame.mvvm.view

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// Activity 委托保持不变，因为它在 Activity 生命周期内是安全的
inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}

/**
 * Fragment ViewBinding 委托，确保在视图生命周期内安全访问。
 */
class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    // 使用 @Volatile 确保多线程环境下的可见性，尽管通常只在主线程操作
    @Volatile
    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        // 1. 如果已存在，直接返回
        val binding = binding
        if (binding != null) {
            return binding
        }

        // 2. 检查 Fragment 视图状态
        // 必须确保 fragment.getView() 在调用时非空
        val view = thisRef.view
        if (view == null) {
            // 如果视图为空，抛出与 AndroidX Fragment 相同的错误，提供清晰的上下文
            throw IllegalStateException(
                "Can't access the Fragment View's LifecycleOwner for ${thisRef.javaClass.simpleName} " +
                        "when getView() is null i.e., before onCreateView() or after onDestroyView()."
            )
        }

        // 3. 获取视图生命周期并检查状态
        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            // 视图已被销毁，但绑定对象被持有，这不应该发生。
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed (onDestroyView() has been called).")
        }

        // 4. 创建新的绑定并注册生命周期观察者
        return viewBindingFactory(view).also { newBinding ->
            this.binding = newBinding

            // 确保只在绑定被创建时才注册观察者
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    // 当视图销毁时（即 onDestroyView()），清空引用
                    this@FragmentViewBindingDelegate.binding = null
                }
            })
        }
    }
}

/**
 * 扩展函数，用于在 Fragment 中使用 ViewBinding 委托。
 */
fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)
