import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class InterceptInnerRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private var lastY = 0f
    private var isDraggingDown = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastY = ev.rawY
                // 请求不拦截，内部自己先处理
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = ev.rawY - lastY
                isDraggingDown = dy > 0
                lastY = ev.rawY

                if (isDraggingDown && !canScrollVertically(-1)) {
                    // 向下滑 & 内层到顶：释放给外层
                    parent.requestDisallowInterceptTouchEvent(false)

                    // 手动取消当前事件
                    val cancelEvent = MotionEvent.obtain(ev)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL
                    super.dispatchTouchEvent(cancelEvent)

                    return false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 重置标志
                isDraggingDown = false
            }
        }

        return super.dispatchTouchEvent(ev)
    }
}
