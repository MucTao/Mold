@file:Suppress("unused")

package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.Lifecycle

/**
 * <pre>
 * author:
 * ___           ___           ___         ___
 * _____                       /  /\         /__/\         /__/|       /  /\
 * /  /::\                     /  /::\        \  \:\       |  |:|      /  /:/
 * /  /:/\:\    ___     ___    /  /:/\:\        \  \:\      |  |:|     /__/::\
 * /  /:/~/::\  /__/\   /  /\  /  /:/~/::\   _____\__\:\   __|  |:|     \__\/\:\
 * /__/:/ /:/\:| \  \:\ /  /:/ /__/:/ /:/\:\ /__/::::::::\ /__/\_|:|____    \  \:\
 * \  \:\/:/~/:/  \  \:\  /:/  \  \:\/:/__\/ \  \:\~~\~~\/ \  \:\/:::::/     \__\:\
 * \  \::/ /:/    \  \:\/:/    \  \::/       \  \:\  ~~~   \  \::/~~~~      /  /:/
 * \  \:\/:/      \  \::/      \  \:\        \  \:\        \  \:\         /__/:/
 * \  \::/        \__\/        \  \:\        \  \:\        \  \:\        \__\/
 * \__\/                       \__\/         \__\/         \__\/
 * blog  : http://Muc.com
 * time  : 16/12/08
 * desc  : utils about initialization
 * </pre> *
 */
object Utils {
    abstract class Task<Result>(private val mConsumer: ((Result?) -> Unit)?) : ThreadUtils.SimpleTask<Result>() {
        override fun onSuccess(result: Result?) {
            mConsumer?.invoke(result)
        }
     }

    interface OnAppStatusChangedListener {
        fun onForeground(activity: Activity?)

        fun onBackground(activity: Activity?)
    }

    class ActivityLifecycleCallbacks {
        fun onActivityCreated(activity: Activity) {
            /**/
        }

        fun onActivityStarted(activity: Activity) {
            /**/
        }

        fun onActivityResumed(activity: Activity) {
            /**/
        }

        fun onActivityPaused(activity: Activity) {
            /**/
        }

        fun onActivityStopped(activity: Activity) {
            /**/
        }

        fun onActivityDestroyed(activity: Activity) {
            /**/
        }

        fun onLifecycleChanged(activity: Activity, event: Lifecycle.Event?) {
            /**/
        }
    }

    @SuppressLint("StaticFieldLeak")
    private var sApp: Application? = null

    /**
     * Init utils.
     *
     * Init it in the class of UtilsFileProvider.
     *
     * @param app application
     */
    fun init(app: Application) {
        if (sApp == null) {
            sApp = app.also {
                UtilsActivityLifecycleImpl.init(it)
            }
            ThreadUtils.cachedPool.execute(AdaptScreenUtils.preLoadRunnable)
            return
        }
        if (sApp == app) return
        sApp = app.also {
            UtilsActivityLifecycleImpl.init(it)
        }
    }

    val app: Application
        /**
         * Return the Application object.
         *
         * Main process get app by UtilsFileProvider, and other process get app by reflect.
         *
         * @return the Application object
         */
        get() {
            if (sApp != null) return sApp!!
            UtilsActivityLifecycleImpl.applicationByReflect?.let { init(it) }
            if (sApp == null) throw NullPointerException("reflect failed.")
            Log.i("Utils", "${ProcessUtils.currentProcessName.getOrNull()} reflect app success.")
            return sApp!!
        }

    internal val sp: SharedPreferences by lazy { app.getSharedPreferences("utils", MODE_PRIVATE) }
}
