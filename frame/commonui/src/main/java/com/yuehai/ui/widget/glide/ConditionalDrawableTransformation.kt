//package com.yuehai.ui.widget.glide
//
//
//import android.content.Context
//import android.graphics.drawable.BitmapDrawable
//import android.graphics.drawable.Drawable
//import android.util.Log // 导入 Log
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.Transformation
//import com.bumptech.glide.load.engine.Resource
//import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
//import com.bumptech.glide.load.resource.drawable.DrawableResource
//import java.security.MessageDigest
//import androidx.core.graphics.drawable.toDrawable
//
///**
// * 封装 BitmapTransformation，实现对静态图应用变换，对动态图保持原样.
// *
// * @param bitmapTransform 您的 BottomUpCropWithBottomCropTransformation 实例
// */
//class ConditionalDrawableTransformation(
//    // 假设 DynamicBottomUpCropWithBottomCropTransformation 等同于 BottomUpCropWithBottomCropTransformation
//    private val bitmapTransform: DynamicBottomUpCropWithBottomCropTransformation
//) : Transformation<Drawable> {
//
//    private val TAG = "GlideTransformLog" // 定义日志标签
//
//    private fun getBitmapPool(context: Context): BitmapPool {
//        return Glide.get(context).bitmapPool
//    }
//
//    override fun transform(
//        context: Context,
//        resource: Resource<Drawable>,
//        outWidth: Int,
//        outHeight: Int
//    ): Resource<Drawable> {
//
//        val drawable = resource.get()
//
//        if (drawable is BitmapDrawable) {
//            Log.d(TAG, "Drawable is BitmapDrawable. Applying Bitmap transformation.")
//
//            val originalBitmap = drawable.bitmap
//            val pool = getBitmapPool(context)
//
//            // 记录原始 Bitmap 信息
//            Log.d(TAG, "Original Bitmap: ${originalBitmap.width}x${originalBitmap.height}, Hash: ${originalBitmap.hashCode()}")
//
//
//            // 1. 应用您的 Bitmap 变换 (包含裁剪、模糊、圆角)
//            val transformedBitmap = bitmapTransform.transform(
//                pool,
//                originalBitmap,
//                outWidth,
//                outHeight
//            )
//
//            // 记录变换后 Bitmap 信息
//            Log.d(TAG, "Transformed Bitmap: ${transformedBitmap.width}x${transformedBitmap.height}, Hash: ${transformedBitmap.hashCode()}")
//
//
//            // 2. 如果变换成功且 Bitmap 发生变化
//            if (transformedBitmap != originalBitmap) {
//                Log.d(TAG, "Transformation successful. Bitmap changed (Hash differ).")
//
//                // 2.1 创建新的 BitmapDrawable
//                val newDrawable = transformedBitmap.toDrawable(context.resources)
//
//                // 2.2 返回一个新的 Resource 包装器
//                return object : DrawableResource<Drawable>(newDrawable) {
//                    override fun getResourceClass(): Class<Drawable> = Drawable::class.java
//                    override fun getSize(): Int = transformedBitmap.byteCount
//                    override fun recycle() {
//                        // 回收变换后的 Bitmap 到 Pool 中
//                        pool.put(transformedBitmap)
//                        Log.d(TAG, "Recycled transformed Bitmap (Hash: ${transformedBitmap.hashCode()}) to Pool.")
//                    }
//                }
//            } else {
//                Log.d(TAG, "Transformation returned original Bitmap reference (Hash same). No change applied.")
//            }
//        } else {
//            Log.d(TAG, "Drawable is NOT BitmapDrawable (${drawable.javaClass.simpleName}). Skipping transformation to preserve animation.")
//        }
//
//        // 动态图片（如 WebP 动画）或未变化，返回原始资源，保持动画效果
//        return resource
//    }
//
//    // ... (其他方法保持不变) ...
//    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
//        bitmapTransform.updateDiskCacheKey(messageDigest)
//        messageDigest.update(ConditionalDrawableTransformation::class.java.name.toByteArray())
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//        other as ConditionalDrawableTransformation
//        return bitmapTransform == other.bitmapTransform
//    }
//
//    override fun hashCode(): Int {
//        return bitmapTransform.hashCode()
//    }
//}