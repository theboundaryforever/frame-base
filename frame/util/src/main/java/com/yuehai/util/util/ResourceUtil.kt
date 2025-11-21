package com.yuehai.util.util

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.yuehai.util.AppUtil
import java.io.File
import androidx.core.graphics.drawable.toDrawable


val TAG_AAB="resource"

fun installSplitCompat(context: Context?) {
    if (context == null) {
        return
    }


}

private fun getActivityContext(): Context? {
    return AppUtil.currentActivity
}

private fun getCompatResources(): Resources {
    return AppUtil.appContext.resources
}

fun getCompatDisplayMetrics(): DisplayMetrics {
    return getCompatResources().displayMetrics
}

fun getCompatString(@StringRes id: Int, vararg formatArgs: Any): String {
    var str = ""
    //try activity context first
    val activityContext: Context? = getActivityContext()
    if (activityContext != null) {
        str = try {
            getString(activityContext, id, *formatArgs)
        } catch (e: Exception) {
            Log.e("TAG_AAB",
                "Activity context getString failed. the resId is " + Integer.toHexString(id),
                e)
            ""
        }
    }

    if (TextUtils.isEmpty(str)) {
        try {
            //try application context last
            str = getString(AppUtil.appContext, id, *formatArgs)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Application context getString failed. the resId is " + Integer.toHexString(id),
                e)
        }
    }
    return str
}

private fun getString(context: Context, @StringRes resId: Int, vararg formatArgs: Any): String {
    var str = try {
        if (formatArgs.isNotEmpty()) {
            context.getString(resId, *formatArgs)
        } else {
            context.getString(resId)
        }
    } catch (e: Exception) {
        ""
    }
    if (TextUtils.isEmpty(str)) {
        installSplitCompat(context)
        str = if (formatArgs.isNotEmpty()) {
            context.getString(resId, *formatArgs)
        } else {
            context.getString(resId)
        }
    }
    return str
}

fun getCompatColor(@ColorRes id: Int): Int {
    var color = 0
    //try activity context first
    val activityContext: Context? = getActivityContext()
    if (activityContext != null) {
        color = try {
            getColor(activityContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Activity context getColor failed. the resId is ${Integer.toHexString(id)}",
                e)
            0
        }
    }
    if (color == 0) {
        try {
            //try application context last
            color = getColor(AppUtil.appContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Application context getColor failed. the resId is ${Integer.toHexString(id)}",
                e)
        }
    }
    return color
}

private fun getColor(context: Context, @ColorRes id: Int): Int {
    var color: Int = try {
        ContextCompat.getColor(context, id)
    } catch (e: Exception) {
        0
    }

    if (color == 0) {
        installSplitCompat(context)
        color = ContextCompat.getColor(context, id)
    }

    return color
}

fun getCompatColorStateList(@ColorRes id: Int): ColorStateList {
    var color: ColorStateList? = null
    //try activity context first
    val activityContext: Context? = getActivityContext()
    if (activityContext != null) {
        color = try {
            getColorStateList(activityContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Activity context getCompatColorStateList failed. the resId is ${
                    Integer.toHexString(id)
                }",
                e)
            null
        }
    }
    if (color == null) {
        color = try {
            //try application context last
            getColorStateList(AppUtil.appContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Application context getCompatColorStateList failed. the resId is ${
                    Integer.toHexString(id)
                }",
                e)
            ColorStateList.valueOf(getCompatColor(android.R.color.black))
        }
    }
    return color!!
}

private fun getColorStateList(context: Context, @ColorRes id: Int): ColorStateList? {
    var color: ColorStateList? = try {
        ContextCompat.getColorStateList(context, id)
    } catch (e: Exception) {
        null
    }

    if (color == null) {
        installSplitCompat(context)
        color = ContextCompat.getColorStateList(context, id)
    }

    return color
}

fun getCompatDimension(@DimenRes id: Int): Float {
    var dimension = 0f
    //try activity context first
    val activityContext: Context? = getActivityContext()
    if (activityContext != null) {
        dimension = try {
            getDimension(activityContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Activity context getDimension failed. the resId is " + Integer.toHexString(id),
                e)
            0f
        }
    }

    if (dimension == 0f) {
        try {
            //try application context last
            dimension = getDimension(AppUtil.appContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Application context getDimension failed. the resId is " + Integer.toHexString(id),
                e)
        }
    }
    return dimension
}

private fun getDimension(context: Context, @DimenRes id: Int): Float {
    var dimension = try {
        context.resources.getDimension(id)
    } catch (e: Exception) {
        0f
    }

    if (dimension == 0f) {
        installSplitCompat(context)
        dimension = context.resources.getDimension(id)
    }

    return dimension
}

fun getCompatDimensionPixelSize(@DimenRes id: Int): Int {
    var dimension = 0
    //try activity context first
    val activityContext: Context? = getActivityContext()
    if (activityContext != null) {
        dimension = try {
            getDimensionPixelSize(activityContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Activity context getDimensionPixelSize failed. the resId is " + Integer.toHexString(
                    id),
                e)
            0
        }
    }

    if (dimension == 0) {
        try {
            //try application context last
            dimension = getDimensionPixelSize(AppUtil.appContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Application context getDimensionPixelSize failed. the resId is " + Integer.toHexString(
                    id),
                e)
        }
    }
    return dimension
}

private fun getDimensionPixelSize(context: Context, @DimenRes id: Int): Int {
    var dimension = try {
        context.resources.getDimensionPixelSize(id)
    } catch (e: Exception) {
        0
    }

    if (dimension == 0) {
        installSplitCompat(context)
        dimension = context.resources.getDimensionPixelSize(id)
    }

    return dimension
}


fun getCompatDrawable(@DrawableRes id: Int): Drawable {
    var drawable: Drawable? = null
    //try activity context first
    val activityContext: Context? = getActivityContext()
    if (activityContext != null) {
        drawable = try {
            getDrawable(activityContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Activity context getDrawable failed. the resId is " + Integer.toHexString(id),
                e)
            null
        }
    }

    if (drawable == null) {
        drawable = try {
            //try application context last
            getDrawable(AppUtil.appContext, id)
        } catch (e: Exception) {
            Log.e(TAG_AAB,
                "Application context getDrawable failed. the resId is " + Integer.toHexString(id),
                e)
            getCompatColor(android.R.color.black).toDrawable()
        }
    }
    return drawable!!
}

private fun getDrawable(context: Context, @DrawableRes resId: Int): Drawable? {
    var drawable = try {
        ContextCompat.getDrawable(context, resId)
    } catch (e: Exception) {
        null
    }
    if (drawable == null) {
        installSplitCompat(context)
        drawable = ContextCompat.getDrawable(context, resId)
    }
    return drawable
}

fun getCompatFont(@FontRes id: Int): Typeface? {
    var typeface: Typeface? = null
    //try activity context first
    val activityContext: Context? = getActivityContext()
    if (activityContext != null) {
        typeface = try {
            getFont(activityContext, id)
        } catch (e: Exception) {
            Log.e(
                TAG_AAB,
                "Application context getCompatFont failed. the resId is " + Integer.toHexString(
                    id
                ),
                e
            )
            null
        }
    }

    if (typeface == null) {
        try {
            //try application context last
            typeface = getFont(AppUtil.appContext, id)
        } catch (e: Exception) {
            Log.e(
                TAG_AAB,
                "Application context getCompatFont failed. the resId is " + Integer.toHexString(
                    id
                ),
                e
            )
        }
    }
    return typeface
}

private fun getFont(context: Context, @FontRes id: Int): Typeface? {
    var typeFace: Typeface? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.resources.getFont(id)
        } else {
            Typeface.createFromFile(
                File(
                    context.resources.getResourceName(
                        id
                    )
                )
            )
        }
    } catch (e: Exception) {
        null
    }

    if (typeFace == null) {
        installSplitCompat(context)
        typeFace = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.resources.getFont(id)
        } else {
            Typeface.createFromFile(
                File(
                    context.resources.getResourceName(
                        id
                    )
                )
            )
        }
    }

    return typeFace
}