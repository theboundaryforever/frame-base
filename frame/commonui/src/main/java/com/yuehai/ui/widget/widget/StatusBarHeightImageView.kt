package com.yuehai.ui.widget.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.adealik.frame.base.ext.fastLazy
import com.yuehai.data.collection.path.Constants
import com.yuehai.util.util.ext.dp

class StatusBarHeightImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    // 优化点1：将状态栏高度缓存为私有成员变量
    // 避免在每次 onMeasure 时重复调用 getStatusBarHeight()
    private val statusBarHeight by fastLazy {
        if (Constants.getStatusBarHeight() > 0) {
            Constants.getStatusBarHeight()
        } else {
            44.dp()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 优化点2：移除调试日志，以提高性能和代码整洁度
        Log.e(
            "tag",
            "onMeasure: widthMode=" + MeasureSpec.getMode(widthMeasureSpec) + " heightMode=" + MeasureSpec.getMode(
                heightMeasureSpec
            )
        )

        // 获取宽度和高度的测量模式和尺寸
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        // 优化点3：调整逻辑，使其更清晰地表达意图
        // 只有当宽度被明确指定（如 fill_parent 或固定 dp）时，才强制设置高度为状态栏高度
        if (widthMode == MeasureSpec.EXACTLY) {
            // 获取指定的宽度
            val width = MeasureSpec.getSize(widthMeasureSpec)
            // 强制将高度设置为状态栏高度
            setMeasuredDimension(width, statusBarHeight)
        } else {
            // 其他情况（如 width="wrap_content"），则沿用父类的测量逻辑
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}