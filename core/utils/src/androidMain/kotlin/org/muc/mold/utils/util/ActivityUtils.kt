@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.AnimRes
import androidx.core.app.ActivityOptionsCompat
import org.muc.mold.utils.util.Utils.ActivityLifecycleCallbacks
import java.lang.ref.WeakReference
import java.lang.reflect.Field

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/09/23
 * desc  : utils about activity
 * </pre> *
 */
object ActivityUtils {
    /**
     * Add callbacks of activity lifecycle.
     *
     * @param callbacks The callbacks.
     */
    fun addActivityLifecycleCallbacks(callbacks: ActivityLifecycleCallbacks) {
        UtilsActivityLifecycleImpl.addActivityLifecycleCallbacks(callbacks)
    }

    /**
     * Add callbacks of activity lifecycle.
     *
     * @param activity The activity.
     * @param callbacks The callbacks.
     */
    fun addActivityLifecycleCallbacks(activity: Activity, callbacks: ActivityLifecycleCallbacks) {
        UtilsActivityLifecycleImpl.addActivityLifecycleCallbacks(activity, callbacks)
    }

    /**
     * Remove callbacks of activity lifecycle.
     *
     * @param callbacks The callbacks.
     */
    fun removeActivityLifecycleCallbacks(callbacks: ActivityLifecycleCallbacks) {
        UtilsActivityLifecycleImpl.removeActivityLifecycleCallbacks(callbacks)
    }

    /**
     * Remove callbacks of activity lifecycle.
     *
     * @param activity The activity.
     */
    fun removeActivityLifecycleCallbacks(activity: Activity) {
        UtilsActivityLifecycleImpl.removeActivityLifecycleCallbacks(activity)
    }

    /**
     * Remove callbacks of activity lifecycle.
     *
     * @param activity The activity.
     * @param callbacks The callbacks.
     */
    fun removeActivityLifecycleCallbacks(
        activity: Activity,
        callbacks: ActivityLifecycleCallbacks,
    ) {
        UtilsActivityLifecycleImpl.removeActivityLifecycleCallbacks(activity, callbacks)
    }

    /**
     * Return the activity by context.
     *
     * @param context The context.
     * @return the activity by context.
     */
    fun getActivityByContext(context: Context?): Activity? {
        if (context == null) return null
        val activity: Activity? = getActivityByContextInner(context)
        if (!isActivityAlive(activity)) return null
        return activity
    }

    private fun getActivityByContextInner(context: Context?): Activity? {
        var context: Context? = context ?: return null
        val list = ArrayList<Context>()
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            val activity: Activity? = getActivityFromDecorContext(context)
            if (activity != null) return activity
            list.add(context)
            context = context.baseContext
            if (context == null) {
                return null
            }
            if (list.contains(context)) {
                return null
            }
        }
        return null
    }

    @SuppressLint("PrivateApi")
    private fun getActivityFromDecorContext(context: Context?): Activity? {
        if (context == null) return null
        if (context.javaClass.getName().equals("com.android.internal.policy.DecorContext")) {
            runCatching {
                val mActivityContextField: Field =
                    context.javaClass.getDeclaredField("mActivityContext")
                mActivityContextField.isAccessible = true
                return (mActivityContextField.get(context) as WeakReference<*>).get() as Activity?
            }
        }
        return null
    }

    /**
     * Return whether the activity exists.
     *
     * @param pkg The name of the package.
     * @param cls The name of the class.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isActivityExists(pkg: String, cls: String): Boolean {
        val intent = Intent()
        intent.setClassName(pkg, cls)
        val pm: PackageManager = Utils.app.packageManager
        return !(pm.resolveActivity(intent, 0) == null ||
                intent.resolveActivity(pm) == null ||
                pm.queryIntentActivities(intent, 0).isEmpty())
    }

    /**
     * Start the activity.
     *
     * @param clz The activity class.
     * @param context The context.
     * @param pkg The packageName.
     * @param extras The Bundle of extras to add to this intent.
     * @param options Additional options for how the Activity should be started.
     */
    fun startActivity(
        clz: Class<out Activity>,
        context: Context = topActivityOrApp,
        pkg: String = context.packageName,
        extras: Bundle? = null,
        options: Bundle? = null,
    ) {
        startActivity(clz.getName(), context, pkg, extras, options)
    }

    /**
     * Start the activity.
     *
     * @param cls The activity className.
     * @param context The context.
     * @param pkg The packageName.
     * @param extras The Bundle of extras to add to this intent.
     * @param options Additional options for how the Activity should be started.
     */
    fun startActivity(
        cls: String,
        context: Context = topActivityOrApp,
        pkg: String = context.packageName,
        extras: Bundle? = null,
        options: Bundle? = null,
    ) {
        val intent = Intent()
        if (extras != null) intent.putExtras(extras)
        intent.setComponent(ComponentName(pkg, cls))
        startActivity(intent, context, options)
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param clz The activity class.
     * @param sharedElements The names of the shared elements to transfer to the called
     * @param pkg The packageName.
     * @param extras The Bundle of extras to add to this intent.
     */
    fun startActivity(
        activity: Activity,
        clz: Class<out Activity>,
        sharedElements: Array<View>?,
        pkg: String = activity.packageName,
        extras: Bundle? = null,
    ) {
        startActivity(
            clz.getName(),
            activity,
            pkg,
            extras,
            getOptionsBundle(activity, sharedElements),
        )
    }

    /**
     * Start the activity.
     *
     * @param activity The activity.
     * @param cls The activity className.
     * @param sharedElements The names of the shared elements to transfer to the called
     * @param pkg The packageName.
     * @param extras The Bundle of extras to add to this intent.
     */
    fun startActivity(
        activity: Activity,
        cls: String,
        sharedElements: Array<View>?,
        pkg: String = activity.packageName,
        extras: Bundle? = null,
    ) {
        startActivity(cls, activity, pkg, extras, getOptionsBundle(activity, sharedElements))
    }

    /**
     * Start the activity.
     *
     * @param clz The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     * @param context The context.
     * @param extras The Bundle of extras to add to this intent.
     */
    fun startActivity(
        clz: Class<out Activity>,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
        context: Context = topActivityOrApp,
        pkg: String = context.packageName,
        extras: Bundle? = null,
    ) {
        startActivity(
            clz.getName(),
            context,
            pkg,
            extras,
            getOptionsBundle(context, enterAnim, exitAnim),
        )
    }

    /**
     * Start the activity.
     *
     * @param cls The activity className.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     * @param context The context.
     * @param extras The Bundle of extras to add to this intent.
     */
    fun startActivity(
        cls: String,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
        context: Context = topActivityOrApp,
        pkg: String = context.packageName,
        extras: Bundle? = null,
    ) {
        startActivity(cls, context, pkg, extras, getOptionsBundle(context, enterAnim, exitAnim))
    }

    /**
     * Start the activity.
     *
     * @param intent The description of the activity to start.
     * @param options Additional options for how the Activity should be started.
     * @param options Additional options for how the Activity should be started.
     * @return `true`: success<br></br>`false`: fail
     */
    fun startActivity(
        intent: Intent,
        context: Context = topActivityOrApp,
        options: Bundle? = null,
    ): Boolean {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            if (options != null) {
                context.startActivity(intent, options)
            } else {
                context.startActivity(intent)
            }
            true
        }
            .getOrDefault(false)
    }

    /**
     * Start the activity.
     *
     * @param intent The description of the activity to start.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     * @return `true`: success<br></br>`false`: fail
     */
    fun startActivity(
        intent: Intent,
        context: Context = topActivityOrApp,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ): Boolean = startActivity(intent, context, getOptionsBundle(context, enterAnim, exitAnim))

    /**
     * Start the activity.
     *
     * @param intent The description of the activity to start.
     * @param sharedElements The names of the shared elements to transfer to the called
     * @return `true`: success<br></br>`false`: fail
     */
    fun startActivity(
        intent: Intent,
        activity: Activity,
        sharedElements: Array<View>?,
    ): Boolean = startActivity(intent, activity, getOptionsBundle(activity, sharedElements))

    /**
     * Start activities.
     *
     * @param intents The descriptions of the activities to start.
     * @param options Additional options for how the Activity should be started.
     */
    fun startActivities(
        intents: Array<Intent>,
        context: Context = topActivityOrApp,
        options: Bundle? = null,
    ) {
        if (context !is Activity) {
            for (intent in intents) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        if (options != null) {
            context.startActivities(intents, options)
        } else {
            context.startActivities(intents)
        }
    }

    /**
     * Start activities.
     *
     * @param intents The descriptions of the activities to start.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun startActivities(
        intents: Array<Intent>,
        context: Context = topActivityOrApp,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ) {
        startActivities(intents, context, getOptionsBundle(context, enterAnim, exitAnim))
    }

    /** Start home activity. */
    fun startHomeActivity() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(homeIntent)
    }

    /**
     * Start the launcher activity.
     *
     * @param pkg The name of the package.
     */
    @JvmOverloads
    fun startLauncherActivity(pkg: String = Utils.app.packageName) {
        val launcherActivity = getLauncherActivity(pkg)
        if (launcherActivity.isNullOrEmpty()) return
        startActivity(cls = launcherActivity, pkg = pkg)
    }

    /** @return the list of activity */
    val activityList: List<Activity>
        get() = UtilsActivityLifecycleImpl.activityList

    /** @return the name of launcher activity */
    val launcherActivity: String?
        get() = getLauncherActivity(Utils.app.packageName)

    /**
     * @param pkg The name of the package.
     * @return the name of launcher activity
     */
    fun getLauncherActivity(pkg: String): String? {
        if (pkg.isBlank()) return ""
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.setPackage(pkg)
        val pm: PackageManager = Utils.app.packageManager
        val info: List<ResolveInfo?> = pm.queryIntentActivities(intent, 0)
        if (info.isEmpty()) return null
        return info[0]?.activityInfo?.name
    }

    /** @return the list of main activities */
    val mainActivities
        get() = getMainActivities(Utils.app.packageName)

    /**
     * @param pkg The name of the package.
     * @return the list of main activities
     */
    fun getMainActivities(pkg: String): List<String> {
        val ret = ArrayList<String>()
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setPackage(pkg)
        val pm: PackageManager = Utils.app.packageManager
        val info: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        val size: Int = info.size
        if (size == 0) return ret
        for (i in 0..<size) {
            val ri: ResolveInfo = info[i]
            if (ri.activityInfo.processName.equals(pkg)) {
                ret.add(ri.activityInfo.name)
            }
        }
        return ret
    }

    /** @return the top activity in activity's stack */
    val topActivity: Activity?
        get() = UtilsActivityLifecycleImpl.topActivity

    /**
     * @param context The context.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isActivityAlive(context: Context): Boolean {
        return isActivityAlive(getActivityByContext(context))
    }

    /**
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isActivityAlive(activity: Activity?): Boolean {
        return activity != null && !activity.isFinishing && !activity.isDestroyed
    }

    /**
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isActivityExistsInStack(activity: Activity): Boolean {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (aActivity in activities) {
            if (aActivity == activity) {
                return true
            }
        }
        return false
    }

    /**
     * @param clz The activity class.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isActivityExistsInStack(clz: Class<out Activity>): Boolean {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (aActivity in activities) {
            if (aActivity.javaClass == clz) {
                return true
            }
        }
        return false
    }

    /**
     * Finish the activity.
     *
     * @param activity The activity.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    fun finishActivity(activity: Activity, isLoadAnim: Boolean = false) {
        activity.finish()
        if (!isLoadAnim) {
            activity.overridePendingTransition(0, 0)
        }
    }

    /**
     * Finish the activity.
     *
     * @param activity The activity.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun finishActivity(
        activity: Activity,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ) {
        activity.finish()
        activity.overridePendingTransition(enterAnim, exitAnim)
    }

    /**
     * Finish the activity.
     *
     * @param clz The activity class.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    fun finishActivity(
        clz: Class<out Activity>?,
        isLoadAnim: Boolean = false,
    ) {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (activity in activities) {
            if (activity.javaClass == clz) {
                activity.finish()
                if (!isLoadAnim) {
                    activity.overridePendingTransition(0, 0)
                }
            }
        }
    }

    /**
     * Finish the activity.
     *
     * @param clz The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun finishActivity(
        clz: Class<out Activity>,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ) {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (activity in activities) {
            if (activity.javaClass == clz) {
                activity.finish()
                activity.overridePendingTransition(enterAnim, exitAnim)
            }
        }
    }

    /**
     * Finish to the activity.
     *
     * @param activity The activity.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    fun finishToActivity(
        activity: Activity?,
        isIncludeSelf: Boolean,
        isLoadAnim: Boolean = false,
    ): Boolean {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (act in activities) {
            if (act == activity) {
                if (isIncludeSelf) {
                    finishActivity(act, isLoadAnim)
                }
                return true
            }
            finishActivity(act, isLoadAnim)
        }
        return false
    }

    /**
     * Finish to the activity.
     *
     * @param activity The activity.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun finishToActivity(
        activity: Activity,
        isIncludeSelf: Boolean,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ): Boolean {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (act in activities) {
            if (act == activity) {
                if (isIncludeSelf) {
                    finishActivity(act, enterAnim, exitAnim)
                }
                return true
            }
            finishActivity(act, enterAnim, exitAnim)
        }
        return false
    }

    /**
     * Finish to the activity.
     *
     * @param clz The activity class.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    fun finishToActivity(
        clz: Class<out Activity>,
        isIncludeSelf: Boolean,
        isLoadAnim: Boolean = false,
    ): Boolean {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (act in activities) {
            if (act.javaClass == clz) {
                if (isIncludeSelf) {
                    finishActivity(act, isLoadAnim)
                }
                return true
            }
            finishActivity(act, isLoadAnim)
        }
        return false
    }

    /**
     * Finish to the activity.
     *
     * @param clz The activity class.
     * @param isIncludeSelf True to include the activity, false otherwise.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun finishToActivity(
        clz: Class<out Activity>,
        isIncludeSelf: Boolean,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ): Boolean {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (act in activities) {
            if (act.javaClass == clz) {
                if (isIncludeSelf) {
                    finishActivity(act, enterAnim, exitAnim)
                }
                return true
            }
            finishActivity(act, enterAnim, exitAnim)
        }
        return false
    }

    /**
     * Finish the activities whose type not equals the activity class.
     *
     * @param clz The activity class.
     * @param isLoadAnim True to use animation for the outgoing activity, false otherwise.
     */
    fun finishOtherActivities(
        clz: Class<out Activity>,
        isLoadAnim: Boolean = false,
    ) {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (act in activities) {
            if (act.javaClass != clz) {
                finishActivity(act, isLoadAnim)
            }
        }
    }

    /**
     * Finish the activities whose type not equals the activity class.
     *
     * @param clz The activity class.
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun finishOtherActivities(
        clz: Class<out Activity>,
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ) {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (act in activities) {
            if (act.javaClass != clz) {
                finishActivity(act, enterAnim, exitAnim)
            }
        }
    }

    /** Finish all activities. */
    @JvmOverloads
    fun finishAllActivities(isLoadAnim: Boolean = false) {
        val activityList = UtilsActivityLifecycleImpl.activityList
        for (act in activityList) {
            act.finish()
            if (!isLoadAnim) {
                act.overridePendingTransition(0, 0)
            }
        }
    }

    /**
     * Finish all activities.
     *
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun finishAllActivities(
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ) {
        val activityList = UtilsActivityLifecycleImpl.activityList
        for (act in activityList) {
            act.finish()
            act.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    /** Finish all activities except the newest activity. */
    @JvmOverloads
    fun finishAllActivitiesExceptNewest(isLoadAnim: Boolean = false) {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (i in 1..<activities.size) {
            finishActivity(activities[i], isLoadAnim)
        }
    }

    /**
     * Finish all activities except the newest activity.
     *
     * @param enterAnim A resource ID of the animation resource to use for the incoming activity.
     * @param exitAnim A resource ID of the animation resource to use for the outgoing activity.
     */
    fun finishAllActivitiesExceptNewest(
        @AnimRes enterAnim: Int,
        @AnimRes exitAnim: Int,
    ) {
        val activities = UtilsActivityLifecycleImpl.activityList
        for (i in 1..<activities.size) {
            finishActivity(activities[i], enterAnim, exitAnim)
        }
    }

    /**
     * @param activity The activity.
     * @return the icon of activity
     */
    fun getActivityIcon(activity: Activity) = getActivityIcon(activity.componentName)

    /**
     * @param clz The activity class.
     * @return the icon of activity
     */
    fun getActivityIcon(clz: Class<out Activity>) = getActivityIcon(ComponentName(Utils.app, clz))

    /**
     * @param activityName The name of activity.
     * @return the icon of activity
     */
    fun getActivityIcon(activityName: ComponentName): Drawable? = runCatching {
        Utils.app.packageManager.getActivityIcon(activityName)
    }
        .getOrNull()

    /**
     * @param activity The activity.
     * @return the logo of activity
     */
    fun getActivityLogo(activity: Activity): Drawable? = getActivityLogo(activity.componentName)

    /**
     * @param clz The activity class.
     * @return the logo of activity
     */
    fun getActivityLogo(clz: Class<out Activity>): Drawable? =
        getActivityLogo(ComponentName(Utils.app, clz))

    /**
     * @param activityName The name of activity.
     * @return the logo of activity
     */
    fun getActivityLogo(activityName: ComponentName): Drawable? = runCatching {
        Utils.app.packageManager.getActivityLogo(activityName)
    }
        .getOrNull()

    private fun getOptionsBundle(
        context: Context,
        enterAnim: Int,
        exitAnim: Int,
    ): Bundle? {
        return ActivityOptionsCompat.makeCustomAnimation(context, enterAnim, exitAnim).toBundle()
    }

    private fun getOptionsBundle(
        activity: Activity,
        sharedElements: Array<View>?,
    ): Bundle? {
        if (sharedElements.isNullOrEmpty()) return null
        val pairs =
            sharedElements
                .map { androidx.core.util.Pair(it, it.transitionName.toString()) }
                .toTypedArray()

        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *pairs).toBundle()
    }

    private val topActivityOrApp: Context
        get() {
            return if (UtilsActivityLifecycleImpl.isAppForeground) topActivity ?: Utils.app
            else {
                Utils.app
            }
        }
}
