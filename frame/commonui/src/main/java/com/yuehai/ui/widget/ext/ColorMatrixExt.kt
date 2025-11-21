package com.yuehai.ui.widget.ext

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.View

/**
 * 扩展方法：将当前 View 及其所有子 View 置灰（黑白化）。
 * * 原理：通过 ColorMatrix 设置饱和度为 0，并将 View 绘制到硬件层 (LAYER_TYPE_HARDWARE)
 * 以确保滤镜作用于 View 的所有内容。
 */
fun View.setGrayscale() {
    // 1. 创建并应用颜色滤镜
    val matrix = ColorMatrix().apply {
        // 设置饱和度为 0，实现完全去色
        setSaturation(0f)
    }
    val filter = ColorMatrixColorFilter(matrix)

    // 2. 将 View 的绘制类型设置为 HARDWARE，并传入滤镜
    // 注意：这里的 null 参数是 Paint 对象，ColorMatrixColorFilter 已经包含了我们所需的 Paint 属性
    setLayerType(View.LAYER_TYPE_HARDWARE, null)

    // 3. 额外设置背景滤镜（如果需要，但 LAYER_TYPE_HARDWARE 已覆盖大部分情况）
    // 如果 View 没有内容，只是背景，这一步是必要的
    this.background?.colorFilter = filter
}

/**
 * 扩展方法：恢复 View 及其所有子 View 的原始颜色。
 * * 原理：将绘制类型恢复为 LAYER_TYPE_NONE，并移除背景上的 ColorFilter。
 */
fun View.restoreColor() {
    // 1. 恢复绘制类型为 NONE，移除滤镜效果
    setLayerType(View.LAYER_TYPE_NONE, null)

    // 2. 移除背景上的滤镜
    this.background?.colorFilter = null
}

// ------------------- 使用示例 -------------------

// 假设你在 Activity 或 Fragment 中获取了一个 ViewGroup 引用
// val container: ViewGroup = findViewById(R.id.my_layout)

// 对整个布局置灰
// container.setGrayscale()

// 恢复颜色
// container.restoreColor()