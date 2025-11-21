package com.yuehai.ui.widget.widget

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class StickyHeaderItemDecoration(
    private val getHeaderPositionForItem: (Int) -> Int,
    private val bindHeaderView: (View, Int) -> Unit,
    private val createHeaderView: () -> View
) : RecyclerView.ItemDecoration() {

    private var currentHeader: View? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val topChild = parent.getChildAt(0) ?: return
        val topPosition = parent.getChildAdapterPosition(topChild)
        if (topPosition == RecyclerView.NO_POSITION) return

        val headerPosition = getHeaderPositionForItem(topPosition)
        val header = createHeaderView().also {
            bindHeaderView(it, headerPosition)
        }

        measureAndLayoutHeader(parent, header)

        val contactPoint = header.bottom
        val childInContact = getChildInContact(parent, contactPoint)

        if (childInContact != null) {
            val nextPosition = parent.getChildAdapterPosition(childInContact)
            if (nextPosition != RecyclerView.NO_POSITION && getHeaderPositionForItem(nextPosition) != headerPosition) {
                val offset = childInContact.top - header.height
                c.translate(0f, offset.toFloat())
            }
        }

        header.draw(c)
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.top <= contactPoint && child.bottom >= contactPoint) {
                return child
            }
        }
        return null
    }

    private fun measureAndLayoutHeader(parent: RecyclerView, header: View) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        header.measure(widthSpec, heightSpec)
        header.layout(0, 0, header.measuredWidth, header.measuredHeight)
    }
}
