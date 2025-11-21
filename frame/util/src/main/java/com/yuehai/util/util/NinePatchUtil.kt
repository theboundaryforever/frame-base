import android.graphics.BitmapFactory
import android.graphics.NinePatch
import android.graphics.Rect
import android.graphics.drawable.NinePatchDrawable
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileInputStream

/**
 * 扩展函数：加载网络 NinePatch 图并设置为背景（支持 .9.png）
 *
 * @param url 网络图片地址（需为 .9.png 格式）
 * @param placeholderRes 占位图资源（可选）
 * @param fallback 加载失败时的回调（可选）
 */
fun View.loadNinePatchFromNetwork(
    url: String,
    placeholderRes: Int? = null,
    fallback: (() -> Unit)? = null
) {


    Glide.with(context)
        .asFile()
        .load(url)
        .into(object : CustomTarget<File>() {
            override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                try {
                    FileInputStream(resource).use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val chunk = bitmap?.ninePatchChunk
                        if (bitmap != null && chunk != null && NinePatch.isNinePatchChunk(chunk)) {
                            val padding = extractNinePatchPadding(chunk)
                            val drawable = NinePatchDrawable(context.resources, bitmap, chunk, padding, null)
                            this@loadNinePatchFromNetwork.background = drawable
                        } else {
                            fallback?.invoke()
                        }
                    }
                } catch (e: Exception) {
                    fallback?.invoke()
                }
            }

            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                // No-op
            }
        })
}

// 工具函数：提取 NinePatch padding 区域（left, top, right, bottom）
private fun extractNinePatchPadding(chunk: ByteArray?): Rect {
    return try {
        if (chunk != null && chunk.size >= 32) {
            val left = toInt(chunk, 12)
            val right = toInt(chunk, 16)
            val top = toInt(chunk, 20)
            val bottom = toInt(chunk, 24)
            Rect(left, top, right, bottom)
        } else {
            Rect(0, 0, 0, 0)
        }
    } catch (e: Exception) {
        Rect(0, 0, 0, 0)
    }
}

// 工具函数：byte[] 转 int
private fun toInt(bytes: ByteArray, offset: Int): Int {
    return (bytes[offset].toInt() shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)
}
