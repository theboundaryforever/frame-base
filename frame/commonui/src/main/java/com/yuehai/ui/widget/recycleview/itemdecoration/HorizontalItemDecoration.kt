package com.yuehai.ui.widget.recycleview.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.yuehai.util.util.DisplayUtil


class HorizontalItemDecoration(
    spaceDp: Float,
    private val startPaddingDp: Float,
    private val endPaddingDp: Float,
) :
    RecyclerView.ItemDecoration() {
    private val space //定义2个Item之间的距离
            : Int = DisplayUtil.dp2px(spaceDp)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        val totalCount = parent.adapter!!.itemCount
        if (DisplayUtil.isRtlLayout()) {
            when (position) {
                0 -> { //第一个
                    outRect.right = DisplayUtil.dp2px(startPaddingDp)
                    outRect.left = space / 2
                }
                totalCount - 1 -> { //最后一个
                    outRect.right = space / 2
                    outRect.left = DisplayUtil.dp2px(endPaddingDp)
                }
                else -> { //中间其它的
                    outRect.right = space / 2
                    outRect.left = space / 2
                }
            }
        } else {
            when (position) {
                0 -> { //第一个
                    outRect.left = DisplayUtil.dp2px(startPaddingDp)
                    outRect.right = space / 2
                }
                totalCount - 1 -> { //最后一个
                    outRect.left = space / 2
                    outRect.right = DisplayUtil.dp2px(endPaddingDp)
                }
                else -> { //中间其它的
                    outRect.left = space / 2
                    outRect.right = space / 2
                }
            }
        }

    }
}
