import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration2(
    private val spanCount: Int,
    private val spacingH: Int,
    private val spacingV: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        // 计算左右间距，确保每列均匀分布
        outRect.left = spacingH - (column + 1) * spacingH / spanCount
        outRect.right = (column) * spacingH / spanCount

        // 只有第一行下面的项目才应用顶部间距
        if (position >= spanCount) {
            outRect.top = spacingV
        }
    }
}