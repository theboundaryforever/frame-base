import android.app.Activity
import android.content.Context
import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.yuehai.util.util.DisplayUtil
import java.security.MessageDigest

class BottomUpCropTransformation(
    private val context: Context,
    private val ratio: Float,       // 高度占屏幕比例
    private val activity: Activity
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        // 获取屏幕真实宽高
        val screenWidth = DisplayUtil.getScreenRealWidth(context) - DisplayUtil.getStatusBarHeight(activity)
        val screenHeight = DisplayUtil.getScreenRealHeight(context)

        val targetHeight = (screenHeight * ratio).toInt()
        if (targetHeight <= 0 || screenWidth <= 0) return toTransform

        // 创建裁剪后的 Bitmap
        val result = pool.get(screenWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val matrix = Matrix()

        // 按宽度缩放，保持左右完整
        val scale = screenWidth.toFloat() / toTransform.width
        val scaledHeight = toTransform.height * scale
        val dy = targetHeight - scaledHeight // 底部对齐
        matrix.postScale(scale, scale)
        matrix.postTranslate(0f, dy)

        // 绘制原图到裁剪 Bitmap
        canvas.drawBitmap(toTransform, matrix, null)

        return result
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("BottomUpCrop.$ratio".toByteArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BottomUpCropTransformation
        return ratio == other.ratio
    }

    override fun hashCode(): Int {
        return ratio.hashCode()
    }
}
