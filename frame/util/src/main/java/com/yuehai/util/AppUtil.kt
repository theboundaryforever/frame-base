package com.yuehai.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.yuehai.util.language.collection.ConcurrentList
import java.lang.ref.WeakReference
import java.util.*
import kotlin.system.exitProcess
import java.util.concurrent.ConcurrentHashMap


@SuppressLint("StaticFieldLeak")
object AppUtil {

    var ctx: Context? = null
    lateinit var appContext: Context
    lateinit var application: Application
    lateinit var mediaService: Any

    var currentActivity: Activity? = null
        get() {
            var activity: Activity? = null
            synchronized(activities) {
                if (activities.isNotEmpty()) {
                    activity = activities[activities.size - 1]
                }
            }
            return if (activity == null) (if (field == null) lastCreateActivity?.get() else field) else activity
        }
    val currentActivityName: String
        get() {
            return currentActivity?.componentName?.className ?: ""
        }

    /**
     * 不需要Activity及其精准的场景使用该字段，无锁性能更高
     */
    var cacheCurrentActivity: Activity? = null

    private val activities: ArrayList<Activity> = arrayListOf()
    val runningActivities: ArrayList<String> = arrayListOf()
    private var lastCreateActivity: WeakReference<Activity>? = null
    private var activityCount = 0
    private val flags: HashSet<String> = hashSetOf()
    var background = true

    private val activityLifecycleCallbacks = ConcurrentList<ActivityLifecycleCallbacksExt>()

    fun init(application: Application, mediaService: Any) {
        ctx = application
        appContext = application
        this.application = application
        this.mediaService=mediaService

        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                lastCreateActivity = WeakReference(activity)
                activityCount++
                activityLifecycleCallbacks.dispatch {
                    it.onActivityCreated(activity, savedInstanceState)
                }
                runningActivities.add(activity.componentName.className)
            }

            override fun onActivityStarted(activity: Activity) {
                flags.add(getIdentity(activity))
                if (background) {
                    background = false
                    activityLifecycleCallbacks.dispatch {
                        it.onEnterForeGround()
                    }
                }
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
                cacheCurrentActivity = activity
                activityLifecycleCallbacks.dispatch {
                    it.onActivityResumed(activity)
                }
                synchronized(activities) {
                    activities.add(activity)
                }
            }

            override fun onActivityPaused(activity: Activity) {
                activityLifecycleCallbacks.dispatch {
                    it.onActivityPaused(activity)
                }
                synchronized(activities) {
                    activities.remove(activity)
                }
            }

            override fun onActivityStopped(activity: Activity) {
                activityLifecycleCallbacks.dispatch {
                    it.onActivityStopped(activity)
                }
                flags.remove(getIdentity(activity))
                if (flags.isEmpty()) {
                    background = true
                    activityLifecycleCallbacks.dispatch {
                        it.onEnterBackGround()
                    }
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                activityLifecycleCallbacks.dispatch {
                    it.onActivitySaveInstanceState(activity, outState)
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                activityCount--
                activityLifecycleCallbacks.dispatch {
                    it.onActivityDestroyed(activity)
                }
                if (activities.isEmpty()) {
                    currentActivity = null
                    lastCreateActivity = null
                }
                runningActivities.remove(activity.componentName.className)
            }

        })
    }

    fun getCurrentActivityCount(): Int {
        return activityCount
    }

    fun getAllActivities(): List<Activity> {
        synchronized(activities) {
            return ArrayList(activities)
        }
    }


    fun <T> getSystemService(serviceName: String): T? {
        return appContext.getSystemService(serviceName) as? T
    }

    private fun getIdentity(activity: Activity?): String {
        return if (activity == null) {
            ""
        } else {
            activity.javaClass.name + "@" + Integer.toHexString(activity.hashCode())
        }
    }

    fun registerActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacksExt) {
        activityLifecycleCallbacks.add(callback)
    }

    fun unregisterActivityLifecycleCallbacks(callback: ActivityLifecycleCallbacksExt) {
        activityLifecycleCallbacks.remove(callback)
    }

    /**
     * 重启APP
     * 参考 1。https://stackoverflow.com/questions/6609414/how-do-i-programmatically-restart-an-android-app
     * 参考 2。https://github.com/JakeWharton/ProcessPhoenix
     */
    fun rebootApp() {
        val reStartIntent = getRestartIntent()
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            reStartIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mgr = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 500] = pendingIntent
        exitProcess(0)
    }

    private fun getRestartIntent(): Intent {
        val packageName = appContext.packageName
        val defaultIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)
        if (defaultIntent != null) {
            return defaultIntent
        }
        throw IllegalStateException(
            "Unable to determine default activity for "
                    + packageName
                    + ". Does an activity specify the DEFAULT category in its intent filter?"
        )
    }

    private val sExceptionHandlers =
        Collections.newSetFromMap(ConcurrentHashMap<Thread.UncaughtExceptionHandler, Boolean>())

    fun addUncaughtExceptionHandler(uncaughtExceptionHandler: Thread.UncaughtExceptionHandler) {
        sExceptionHandlers.add(uncaughtExceptionHandler)
    }

    fun removeUncaughtExceptionHandler(uncaughtExceptionHandler: Thread.UncaughtExceptionHandler) {
        sExceptionHandlers.remove(uncaughtExceptionHandler)
    }


    fun isActivityRunning(activityClassName: String): Boolean {
        return runningActivities.contains(activityClassName)
    }
    fun getPreviousActivity(): Activity? {
        synchronized(activities) {
            return if (activities.size >= 2) activities[activities.size - 2] else null
        }
    }

}

interface ActivityLifecycleCallbacksExt : ActivityLifecycleCallbacks {

    fun onEnterBackGround() {}

    fun onEnterForeGround() {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

}