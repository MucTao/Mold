@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import java.lang.reflect.Field
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2020/03/19
 * desc  :
 * </pre> *
 */
@SuppressLint("StaticFieldLeak")
internal object UtilsActivityLifecycleImpl : Application.ActivityLifecycleCallbacks {

    private val STUB: Activity = Activity()

    private val mActivityList: LinkedList<Activity> = LinkedList()

    private val mStatusListeners = CopyOnWriteArrayList<Utils.OnAppStatusChangedListener>()
    private val mActivityLifecycleCallbacksMap =
        ConcurrentHashMap<Activity, MutableList<Utils.ActivityLifecycleCallbacks>>()

    private var mForegroundCount = 0
    private var mConfigCount = 0
    private var mIsBackground = false

    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(this)
    }

    fun unInit(app: Application) {
        mActivityList.clear()
        app.unregisterActivityLifecycleCallbacks(this)
    }

    val activityList: List<Activity>
        get() {
            val list = mActivityList
            if (!list.isEmpty()) {
                return list
            }
            val reflectActivities: List<Activity> = this.activitiesByReflect
            list.addAll(reflectActivities)
            return list
        }

    val topActivity: Activity?
        get() {
            val activityList: List<Activity?> = this.activityList
            for (activity in activityList) {
                if (!ActivityUtils.isActivityAlive(activity)) {
                    continue
                }
                return activity
            }
            return null
        }

    fun addOnAppStatusChangedListener(listener: Utils.OnAppStatusChangedListener) {
        mStatusListeners.add(listener)
    }

    fun removeOnAppStatusChangedListener(listener: Utils.OnAppStatusChangedListener) {
        mStatusListeners.remove(listener)
    }

    fun addActivityLifecycleCallbacks(listener: Utils.ActivityLifecycleCallbacks) {
        addActivityLifecycleCallbacks(STUB, listener)
    }

    fun addActivityLifecycleCallbacks(
        activity: Activity,
        listener: Utils.ActivityLifecycleCallbacks,
    ) {
        ThreadUtils.runOnUiThread { addActivityLifecycleCallbacksInner(activity, listener) }
    }

    val isAppForeground: Boolean
        get() = !mIsBackground

    private fun addActivityLifecycleCallbacksInner(
        activity: Activity,
        callbacks: Utils.ActivityLifecycleCallbacks,
    ) {
        var callbacksList = mActivityLifecycleCallbacksMap[activity]
        if (callbacksList == null) {
            callbacksList = CopyOnWriteArrayList()
            mActivityLifecycleCallbacksMap[activity] = callbacksList
        } else {
            if (callbacksList.contains(callbacks)) return
        }
        callbacksList.add(callbacks)
    }

    fun removeActivityLifecycleCallbacks(callbacks: Utils.ActivityLifecycleCallbacks?) {
        removeActivityLifecycleCallbacks(STUB, callbacks)
    }

    fun removeActivityLifecycleCallbacks(activity: Activity?) {
        if (activity == null) return
        ThreadUtils.runOnUiThread { mActivityLifecycleCallbacksMap.remove(activity) }
    }

    fun removeActivityLifecycleCallbacks(
        activity: Activity?,
        callbacks: Utils.ActivityLifecycleCallbacks?,
    ) {
        if (activity == null || callbacks == null) return
        ThreadUtils.runOnUiThread { removeActivityLifecycleCallbacksInner(activity, callbacks) }
    }

    private fun removeActivityLifecycleCallbacksInner(
        activity: Activity?,
        callbacks: Utils.ActivityLifecycleCallbacks?,
    ) {
        val callbacksList = mActivityLifecycleCallbacksMap[activity]
        if (!callbacksList.isNullOrEmpty()) {
            callbacksList.remove(callbacks)
        }
    }

    private fun consumeActivityLifecycleCallbacks(activity: Activity?, event: Lifecycle.Event) {
        consumeLifecycle(activity, event, mActivityLifecycleCallbacksMap[activity])
        consumeLifecycle(activity, event, mActivityLifecycleCallbacksMap[STUB])
    }

    private fun consumeLifecycle(
        activity: Activity?,
        event: Lifecycle.Event,
        listeners: List<Utils.ActivityLifecycleCallbacks>?,
    ) {
        if (activity == null) return
        if (listeners == null) return
        for (listener in listeners) {
            listener.onLifecycleChanged(activity, event)
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    listener.onActivityCreated(activity)
                }

                Lifecycle.Event.ON_START -> {
                    listener.onActivityStarted(activity)
                }

                Lifecycle.Event.ON_RESUME -> {
                    listener.onActivityResumed(activity)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    listener.onActivityPaused(activity)
                }

                Lifecycle.Event.ON_STOP -> {
                    listener.onActivityStopped(activity)
                }

                Lifecycle.Event.ON_DESTROY -> {
                    listener.onActivityDestroyed(activity)
                }

                else -> {}
            }
        }
        if (event == Lifecycle.Event.ON_DESTROY) {
            mActivityLifecycleCallbacksMap.remove(activity)
        }
    }

    val applicationByReflect: Application?
        @SuppressLint("PrivateApi")
        get() {
            runCatching {
                val activityThreadClass: Class<*> = Class.forName("android.app.ActivityThread")
                val thread: Any = this.activityThread ?: return null
                val app: Any =
                    activityThreadClass.getMethod("getApplication").invoke(thread) ?: return null
                return app as Application
            }
                .getOrElse { e ->
                    if (e !is Exception) throw e
                    e.printStackTrace()
                }
            return null
        }

    /**/
    /////////////////////////////////////////////////////////////////////// */ // lifecycle start
    /**/
    /////////////////////////////////////////////////////////////////////// */
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        /**/
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (mActivityList.isEmpty()) {
            postStatus(activity, true)
        }
        LanguageUtils.applyLanguage(activity)
        setAnimatorsEnabled()
        setTopActivity(activity)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_CREATE)
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        /**/
    }

    override fun onActivityPreStarted(activity: Activity) {
        /**/
    }

    override fun onActivityStarted(activity: Activity) {
        if (!mIsBackground) {
            setTopActivity(activity)
        }
        if (mConfigCount < 0) {
            ++mConfigCount
        } else {
            ++mForegroundCount
        }
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_START)
    }

    override fun onActivityPostStarted(activity: Activity) {
        /**/
    }

    override fun onActivityPreResumed(activity: Activity) {
        /**/
    }

    override fun onActivityResumed(activity: Activity) {
        setTopActivity(activity)
        if (mIsBackground) {
            mIsBackground = false
            postStatus(activity, true)
        }
        processHideSoftInputOnActivityDestroy(activity, false)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_RESUME)
    }

    override fun onActivityPostResumed(activity: Activity) {
        /**/
    }

    override fun onActivityPrePaused(activity: Activity) {
        /**/
    }

    override fun onActivityPaused(activity: Activity) {
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_PAUSE)
    }

    override fun onActivityPostPaused(activity: Activity) {
        /**/
    }

    override fun onActivityPreStopped(activity: Activity) {
        /**/
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.isChangingConfigurations) {
            --mConfigCount
        } else {
            --mForegroundCount
            if (mForegroundCount <= 0) {
                mIsBackground = true
                postStatus(activity, false)
            }
        }
        processHideSoftInputOnActivityDestroy(activity, true)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_STOP)
    }

    override fun onActivityPostStopped(activity: Activity) {
        /**/
    }

    override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
        /**/
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        /**/
    }

    override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
        /**/
    }

    override fun onActivityPreDestroyed(activity: Activity) {
        /**/
    }

    override fun onActivityDestroyed(activity: Activity) {
        mActivityList.remove(activity)
        KeyboardUtils.fixSoftInputLeaks(activity)
        consumeActivityLifecycleCallbacks(activity, Lifecycle.Event.ON_DESTROY)
    }

    override fun onActivityPostDestroyed(activity: Activity) {
        /**/
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ // lifecycle end
    /**
     * To solve close keyboard when activity onDestroy. The preActivity set windowSoftInputMode will
     * prevent the keyboard from closing when curActivity onDestroy.
     */
    private fun processHideSoftInputOnActivityDestroy(activity: Activity, isSave: Boolean) {
        runCatching {
            if (isSave) {
                val window: Window = activity.window
                val attrs: WindowManager.LayoutParams = window.attributes
                val softInputMode: Int = attrs.softInputMode
                window.decorView.setTag(-123, softInputMode)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            } else {
                val tag = activity.window.decorView.getTag(-123)
                if (tag is Int)
                    ThreadUtils.runOnUiThreadDelayed(
                        { runCatching { activity.window?.setSoftInputMode(tag) } },
                        100,
                    )
            }
        }
            .getOrElse { ignore ->
                if (ignore !is Exception) throw ignore
            }
    }

    private fun postStatus(activity: Activity?, isForeground: Boolean) {
        if (mStatusListeners.isEmpty()) return
        for (statusListener in mStatusListeners) {
            if (isForeground) {
                statusListener?.onForeground(activity)
            } else {
                statusListener?.onBackground(activity)
            }
        }
    }

    private fun setTopActivity(activity: Activity) {
        if (mActivityList.contains(activity)) {
            if (!mActivityList.getFirst().equals(activity)) {
                mActivityList.remove(activity)
                mActivityList.addFirst(activity)
            }
        } else {
            mActivityList.addFirst(activity)
        }
    }

    private val activitiesByReflect: List<Activity>
        /** @return the activities which topActivity is first position */
        get() {
            val list = LinkedList<Activity>()
            var topActivity: Activity? = null
            runCatching {
                val activityThread: Any = this.activityThread ?: return list
                val mActivitiesField: Field =
                    activityThread::class.java.getDeclaredField("mActivities")
                mActivitiesField.isAccessible = true
                val mActivities: Any? = mActivitiesField.get(activityThread)
                if (mActivities !is Map<*, *>) {
                    return list
                }
                for (activityRecord in mActivities.values) {
                    val activityField: Field? =
                        activityRecord?.javaClass?.getDeclaredField("activity")
                    activityField?.isAccessible = true
                    val activity: Activity =
                        activityField?.get(activityRecord) as Activity? ?: continue
                    if (topActivity == null) {
                        val pausedField: Field? =
                            activityRecord.javaClass.getDeclaredField("paused")
                        pausedField?.isAccessible = true
                        pausedField?.getBoolean(activityRecord)?.let {
                            if (!it) {
                                topActivity = activity
                            } else {
                                list.addFirst(activity)
                            }
                        }
                    } else {
                        list.addFirst(activity)
                    }
                }
            }
                .getOrElse { e ->
                    if (e !is Exception) throw e
                    Log.e("UtilsActivityLifecycle", "getActivitiesByReflect: " + e.message)
                }
            if (topActivity != null) {
                list.addFirst(topActivity)
            }
            return list
        }

    private val activityThread: Any?
        get() {
            val activityThread: Any? = this.activityThreadInActivityThreadStaticField
            if (activityThread != null) return activityThread
            return this.activityThreadInActivityThreadStaticMethod
        }

    private val activityThreadInActivityThreadStaticField: Any?
        @SuppressLint("PrivateApi")
        get() {
            return runCatching {
                    val activityThreadClass: Class<*> = Class.forName("android.app.ActivityThread")
                    val sCurrentActivityThreadField: Field =
                        activityThreadClass.getDeclaredField("sCurrentActivityThread")
                    sCurrentActivityThreadField.isAccessible = true
                    return@runCatching sCurrentActivityThreadField.get(null)
                }
                .onFailure { e ->
                    if (e !is Exception) throw e
                    Log.e(
                        "UtilsActivityLifecycle",
                        "getActivityThreadInActivityThreadStaticField: " + e.message,
                    )
                }
                .getOrDefault(null)
        }

    private val activityThreadInActivityThreadStaticMethod: Any?
        @SuppressLint("PrivateApi")
        get() {
            return runCatching {
                    val activityThreadClass: Class<*> = Class.forName("android.app.ActivityThread")
                    return@runCatching activityThreadClass
                        .getMethod("currentActivityThread")
                        .invoke(null)
                }
                .onFailure { e ->
                    if (e !is Exception) throw e
                    Log.e(
                        "UtilsActivityLifecycle",
                        "getActivityThreadInActivityThreadStaticMethod: " + e.message,
                    )
                }
                .getOrDefault(null)
        }

    /** Set animators enabled. */
    @SuppressLint("PrivateApi")
    private fun setAnimatorsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ValueAnimator.areAnimatorsEnabled()) {
            return
        }
        runCatching {
            val sDurationScaleField: Field =
                ValueAnimator::class.java.getDeclaredField("sDurationScale")
            sDurationScaleField.isAccessible = true
            val sDurationScale = sDurationScaleField.get(null) as Float
            if (sDurationScale == 0f) {
                sDurationScaleField.set(null, 1f)
                Log.i("UtilsActivityLifecycle", "setAnimatorsEnabled: Animators are enabled now!")
            }
        }
    }
}
