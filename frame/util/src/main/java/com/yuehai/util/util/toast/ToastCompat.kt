package com.yuehai.util.util.toast

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import com.yuehai.util.ActivityLifecycleCallbacksExt
import com.yuehai.util.AppUtil
import com.yuehai.util.util.DisplayUtil
import com.yuehai.util.util.runOnUiThread
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier

object ToastCompat {

    private const val TAG = "ToastCompat"

    private const val LONG_DELAY = 3500
    private const val SHORT_DELAY = 2000 // 2 seconds

    private const val UNKNOWN = -1
    private const val DISABLE = 0
    private const val ENABLE = 1

    private const val CHECK_OP_NO_THROW = "checkOpNoThrow"
    private const val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"

    private var sToastEnable = UNKNOWN
    private var sUseView = false

    init {
        AppUtil.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacksExt {

            override fun onEnterBackGround() {
                sToastEnable = UNKNOWN
            }

        })
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun show(toast: Toast) {
        // API 30 之后纯 Text 的 Toast 中的 View 为空, 直接调用系统的方法(虽然应该是没有用的)
        if (toast.view == null) {
            toast.show()
            return
        }
        val context: Context = AppUtil.appContext
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 && !sUseView) {
            val windowManager: WindowManager? =
                AppUtil.getSystemService<WindowManager>(Context.WINDOW_SERVICE)
            // defined that sets up the layout params appropriately.
            val params = WindowManager.LayoutParams()
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.format = PixelFormat.TRANSLUCENT
            val animation = context.resources.getIdentifier("Animation_Toast", "style", "android")
            if (animation > 0) {
                params.windowAnimations = animation
            }
            params.type = WindowManager.LayoutParams.TYPE_TOAST
            params.title = "Toast"
            params.packageName = context.packageName
            params.flags =
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            params.horizontalMargin = toast.horizontalMargin
            params.verticalMargin = toast.verticalMargin
            val config = context.resources.configuration
            val gravity = Gravity.getAbsoluteGravity(toast.gravity, config.layoutDirection)
            params.gravity = gravity
            if (gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.FILL_HORIZONTAL) {
                params.horizontalWeight = 1.0f
            }
            if (gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.FILL_VERTICAL) {
                params.verticalWeight = 1.0f
            }
            params.x = toast.xOffset
            params.y = toast.yOffset
            try {
                windowManager?.addView(toast.view, params)
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
                sUseView = true
            }
            if (!sUseView) {
                runOnUiThread(
                    { windowManager?.removeView(toast.view) },
                    (if (toast.duration == Toast.LENGTH_LONG) LONG_DELAY else SHORT_DELAY).toLong()
                )
                return
            }
        }
        val activity: Activity? = AppUtil.currentActivity
        if (activity == null) {
            Log.e(TAG, "activity is null")
        } else {
            val root = activity.window.decorView
            val rect = Rect()
            root.getWindowVisibleDisplayFrame(rect)
            val statusBarHeight = rect.top
            if (root is FrameLayout) {
                val layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL or toast.gravity
                )
                val orientation: Int = DisplayUtil.getOrientation()
                //虚拟栏存在的情况下居中调整
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val navHeight: Int = DisplayUtil.getScreenRealHeight(activity) - rect.bottom
                    when {
                        toast.gravity and Gravity.BOTTOM == Gravity.BOTTOM -> {
                            layoutParams.bottomMargin = toast.yOffset + navHeight
                        }

                        toast.gravity and Gravity.TOP == Gravity.TOP -> {
                            layoutParams.topMargin = toast.yOffset + statusBarHeight
                        }

                        toast.gravity and Gravity.CENTER == Gravity.CENTER -> {
                            layoutParams.topMargin = toast.yOffset
                        }

                        else -> {
                            layoutParams.bottomMargin = toast.yOffset + navHeight
                        }
                    }
                    when {
                        toast.gravity and Gravity.START == Gravity.START -> {
                            layoutParams.marginStart = toast.xOffset
                        }

                        toast.gravity and Gravity.END == Gravity.END -> {
                            layoutParams.marginEnd = toast.xOffset
                        }

                        else -> {
                            layoutParams.leftMargin = toast.xOffset
                        }
                    }
                } else {
                    val config = context.resources.configuration
                    val gravity = Gravity.getAbsoluteGravity(toast.gravity, config.layoutDirection)
                    val navPositionRight = rect.left == 0
                    val navHeight =
                        if (navPositionRight) DisplayUtil.getScreenRealWidth(activity) - rect.right else rect.left
                    when {
                        gravity and Gravity.BOTTOM == Gravity.BOTTOM -> {
                            layoutParams.bottomMargin = toast.yOffset
                        }

                        gravity and Gravity.TOP == Gravity.TOP -> {
                            layoutParams.topMargin = toast.yOffset + statusBarHeight
                        }

                        else -> {
                            layoutParams.bottomMargin = toast.yOffset
                        }
                    }
                    when {
                        gravity and Gravity.LEFT == Gravity.LEFT -> {
                            layoutParams.leftMargin =
                                toast.xOffset + if (navPositionRight) 0 else navHeight
                        }

                        gravity and Gravity.RIGHT == Gravity.RIGHT -> {
                            layoutParams.rightMargin =
                                toast.xOffset + if (navPositionRight) navHeight else 0
                        }

                        else -> {
                            layoutParams.leftMargin =
                                toast.xOffset + if (navPositionRight) -navHeight / 2 else navHeight / 2
                        }
                    }
                }
                root.addView(toast.view, layoutParams)
                runOnUiThread(
                    { root.removeView(toast.view) },
                    (if (toast.duration == Toast.LENGTH_LONG) LONG_DELAY else SHORT_DELAY).toLong()
                )
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun show(content: CharSequence?, duration: Int) {
        show(Toast.makeText(AppUtil.appContext, content, duration))
    }

    fun isToastEnabled(): Boolean {
        if (sToastEnable == DISABLE) {
            return false
        }
        if (sToastEnable == ENABLE) {
            return true
        }
        val context: Context = AppUtil.appContext
        return when {
            Build.VERSION.SDK_INT >= 24 -> {
                val notificationManager: NotificationManager =
                    AppUtil.getSystemService<NotificationManager>(Context.NOTIFICATION_SERVICE)
                        ?: return false
                recordToastEnable(notificationManager.areNotificationsEnabled())
            }

            Build.VERSION.SDK_INT >= 19 -> {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val appInfo = context.applicationInfo
                val pkg = context.applicationContext.packageName
                val uid = appInfo.uid
                try {
                    val appOpsClass = Class.forName(AppOpsManager::class.java.name)
                    val checkOpNoThrowMethod =
                        appOpsClass.getMethod(
                            CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                            String::class.java
                        )
                    val opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
                    val value = opPostNotificationValue[Int::class.java] as Int
                    recordToastEnable(
                        checkOpNoThrowMethod.invoke(
                            appOps,
                            value,
                            uid,
                            pkg
                        ) as Int == AppOpsManager.MODE_ALLOWED
                    )
                } catch (e: ClassNotFoundException) {
                    recordToastEnable(true)
                } catch (e: NoSuchMethodException) {
                    recordToastEnable(true)
                } catch (e: NoSuchFieldException) {
                    recordToastEnable(true)
                } catch (e: InvocationTargetException) {
                    recordToastEnable(true)
                } catch (e: IllegalAccessException) {
                    recordToastEnable(true)
                } catch (e: RuntimeException) {
                    recordToastEnable(true)
                }
            }

            else -> {
                recordToastEnable(true)
            }
        }
    }

    private fun recordToastEnable(enable: Boolean): Boolean {
        sToastEnable = if (enable) ENABLE else DISABLE
        return enable
    }

    @SuppressLint("ShowToast")
    fun makeText(context: Context?, text: CharSequence?, duration: Int): Toast {
        val toast = Toast.makeText(context, text, duration)
        if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 19) {
            tryToHack(toast)
        }
        return toast
    }

    @SuppressLint("ShowToast")
    fun makeText(context: Context?, resId: Int, duration: Int): Toast {
        val toast = Toast.makeText(context, resId, duration)
        if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 19) {
            tryToHack(toast)
        }
        return toast
    }

    fun tryToHack(toast: Toast) {
        try {
            val mTN = getFieldValue(toast, "mTN")
            if (mTN != null) {
                var isSuccess = false
                val rawShowRunnable = getFieldValue(mTN, "mShow")
                if (rawShowRunnable is Runnable) {
                    isSuccess = setFieldValue(mTN, "mShow", InternalRunnable(rawShowRunnable))
                }
                if (!isSuccess) {
                    val rawHandler = getFieldValue(mTN, "mHandler")
                    if (rawHandler is Handler) {
                        isSuccess = setFieldValue(
                            rawHandler, "mCallback", InternalHandlerCallback(
                                rawHandler
                            )
                        )
                    }
                }
                if (!isSuccess) {
                    Log.e("ToastCompat", "tryToHack error.")
                }
            }
        } catch (var5: Throwable) {
            var5.printStackTrace()
        }
    }

    private fun setFieldValue(`object`: Any, fieldName: String, newFieldValue: Any): Boolean {
        val field = getDeclaredField(`object`, fieldName)
        if (field != null) {
            try {
                val accessFlags = field.modifiers
                if (Modifier.isFinal(accessFlags)) {
                    val modifiersField = Field::class.java.getDeclaredField("accessFlags")
                    modifiersField.isAccessible = true
                    modifiersField.setInt(field, field.modifiers and -17)
                }
                if (!field.isAccessible) {
                    field.isAccessible = true
                }
                field[`object`] = newFieldValue
                return true
            } catch (var6: Exception) {
                var6.printStackTrace()
            }
        }
        return false
    }

    private fun getFieldValue(obj: Any, fieldName: String): Any? {
        val field = getDeclaredField(obj, fieldName)
        return getFieldValue(obj, field)
    }

    private fun getFieldValue(obj: Any, field: Field?): Any? {
        if (field != null) {
            try {
                if (!field.isAccessible) {
                    field.isAccessible = true
                }
                return field[obj]
            } catch (var3: Exception) {
                var3.printStackTrace()
            }
        }
        return null
    }

    private fun getDeclaredField(obj: Any, fieldName: String): Field? {
        var superClass: Class<*> = obj.javaClass
        while (superClass != Any::class.java) {
            superClass = try {
                return superClass.getDeclaredField(fieldName)
            } catch (var4: NoSuchFieldException) {
                superClass.superclass
            }
        }
        return null
    }

    private class InternalHandlerCallback(private val mHandler: Handler) :
        Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            try {
                mHandler.handleMessage(msg)
            } catch (var3: Throwable) {
                Log.e("ToastCompat", "catch $var3")
                var3.printStackTrace()
            }
            return true
        }
    }

    private class InternalRunnable(private val mRunnable: Runnable) : Runnable {
        override fun run() {
            try {
                mRunnable.run()
            } catch (var2: Throwable) {
                Log.e("ToastCompat", "catch $var2")
                var2.printStackTrace()
            }
        }
    }

}