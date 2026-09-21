@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.Manifest.permission.EXPAND_STATUS_BAR
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.RequiresPermission
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import java.lang.reflect.Method

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/09/23
 * desc  : utils about bar
 * </pre> *
 */
object BarUtils {
    /**/
    /////////////////////////////////////////////////////////////////////// */ // status bar
    /**/
    /////////////////////////////////////////////////////////////////////// */
    private const val TAG_STATUS_BAR = "TAG_STATUS_BAR"
    private const val TAG_OFFSET = "TAG_OFFSET"
    private const val KEY_OFFSET = -123

    /**
     * @return the status bar's height
     */
    @Suppress("InternalInsetResource", "DiscouragedApi")
    fun getStatusBarHeight(resources: Resources = Resources.getSystem()): Int {
        val height = ActivityUtils.topActivity?.let {
            val insets = ViewCompat.getRootWindowInsets(it.window.decorView)
            insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
        }
        if (height != null) return height
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }


    /**
     * Return whether the status bar is visible.
     *
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isStatusBarVisible(activity: Activity): Boolean {
        val flags: Int = activity.window.attributes.flags
        return (flags and WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0
    }

    /**
     * Set the status bar's light mode.
     *
     * @param activity The activity.
     * @param isLightMode True to set status bar light mode, false otherwise.
     */
    fun setStatusBarLightMode(
        activity: Activity,
        isLightMode: Boolean,
    ) {
        setStatusBarLightMode(activity.window, isLightMode)
    }

    /**
     * Set the status bar's light mode.
     *
     * @param window The window.
     * @param isLightMode True to set status bar light mode, false otherwise.
     */
    fun setStatusBarLightMode(
        window: Window,
        isLightMode: Boolean,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView: View = window.decorView
            var vis: Int = decorView.systemUiVisibility
            vis =
                if (isLightMode) {
                    vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            decorView.systemUiVisibility = vis
        }
    }

    /**
     * Is the status bar light mode.
     *
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isStatusBarLightMode(activity: Activity) = isStatusBarLightMode(activity.window)

    /**
     * Is the status bar light mode.
     *
     * @param window The window.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isStatusBarLightMode(window: Window): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView: View = window.decorView
            val vis: Int = decorView.systemUiVisibility
            return vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR != 0
        }
        return false
    }


    /**
     * Set the status bar's color.
     *
     * @param activity The activity.
     * @param color The status bar's color.
     * @param isDecor True to add fake status bar in DecorView, false to add fake status bar in
     *   ContentView.
     */
    fun setStatusBarColor(
        activity: Activity,
        @ColorInt color: Int,
        isDecor: Boolean = false,
    ): View {
        transparentStatusBar(activity)
        return applyStatusBarColor(activity, color, isDecor)
    }

    /**
     * Set the status bar's color.
     *
     * @param window The window.
     * @param color The status bar's color.
     * @param isDecor True to add fake status bar in DecorView, false to add fake status bar in
     *   ContentView.
     */
    fun setStatusBarColor(
        window: Window,
        @ColorInt color: Int,
        isDecor: Boolean = false,
    ): View {
        transparentStatusBar(window)
        return applyStatusBarColor(window, color, isDecor)
    }

    /**
     * Set the status bar's color.
     *
     * @param fakeStatusBar The fake status bar view.
     * @param color The status bar's color.
     */
    fun setStatusBarColor(
        fakeStatusBar: View,
        @ColorInt color: Int,
    ) {
        ActivityUtils.getActivityByContext(fakeStatusBar.context)?.let { transparentStatusBar(it) }
        fakeStatusBar.visibility = View.VISIBLE
        val layoutParams: ViewGroup.LayoutParams = fakeStatusBar.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = getStatusBarHeight()
        fakeStatusBar.setBackgroundColor(color)
    }

    /**
     * Set the custom status bar.
     *
     * @param fakeStatusBar The fake status bar view.
     */
    fun setStatusBarCustom(fakeStatusBar: View) {
        ActivityUtils.getActivityByContext(fakeStatusBar.context)?.let { transparentStatusBar(it) }
        fakeStatusBar.visibility = View.VISIBLE
        var layoutParams: ViewGroup.LayoutParams? = fakeStatusBar.layoutParams
        if (layoutParams == null) {
            layoutParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(),
                )
            fakeStatusBar.setLayoutParams(layoutParams)
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = getStatusBarHeight()
        }
    }

    private fun applyStatusBarColor(
        activity: Activity,
        color: Int,
        isDecor: Boolean,
    ): View {
        return applyStatusBarColor(activity.window, color, isDecor)
    }

    private fun applyStatusBarColor(
        window: Window,
        color: Int,
        isDecor: Boolean,
    ): View {
        val parent: ViewGroup =
            if (isDecor) window.decorView as ViewGroup
            else window.findViewById(android.R.id.content)
        var fakeStatusBarView: View? = parent.findViewWithTag(TAG_STATUS_BAR)
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.isGone) {
                fakeStatusBarView.visibility = View.VISIBLE
            }
            fakeStatusBarView.setBackgroundColor(color)
        } else {
            fakeStatusBarView = createStatusBarView(window.context, color)
            parent.addView(fakeStatusBarView)
        }
        return fakeStatusBarView
    }

    private fun hideStatusBarView(activity: Activity) = hideStatusBarView(activity.window)

    private fun hideStatusBarView(window: Window) {
        val decorView: ViewGroup = window.decorView as ViewGroup
        val fakeStatusBarView: View = decorView.findViewWithTag(TAG_STATUS_BAR) ?: return
        fakeStatusBarView.visibility = View.GONE
    }

    private fun showStatusBarView(window: Window) {
        val decorView: ViewGroup = window.decorView as ViewGroup
        val fakeStatusBarView: View = decorView.findViewWithTag(TAG_STATUS_BAR) ?: return
        fakeStatusBarView.visibility = View.VISIBLE
    }

    private fun createStatusBarView(context: Context, color: Int): View {
        val statusBarView = View(context)
        statusBarView.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(),
            )
        )
        statusBarView.setBackgroundColor(color)
        statusBarView.tag = TAG_STATUS_BAR
        return statusBarView
    }

    fun transparentStatusBar(activity: Activity) = transparentStatusBar(activity.window)

    fun transparentStatusBar(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val option: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val vis: Int = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = option or vis
        window.statusBarColor = Color.TRANSPARENT
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */
    // action bar
    /**
     * Return the action bar's height.
     *
     * @ return the action bar's height
     */
    val actionBarHeight: Int
        get() {
            val tv = TypedValue()
            if (Utils.app.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                return TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    Utils.app.resources.displayMetrics,
                )
            }
            return 0
        }

    /**/
    /////////////////////////////////////////////////////////////////////// */
    // notification bar
    /**
     * Set the notification bar's visibility.
     *
     * Must hold `<uses-permission android:name="android.permission.EXPAND_STATUS_BAR" />`
     *
     * @param isVisible True to set notification bar visible, false otherwise.
     */
    @RequiresPermission(EXPAND_STATUS_BAR)
    fun setNotificationBarVisibility(isVisible: Boolean) =
        invokePanels(if (isVisible) "expandNotificationsPanel" else "collapsePanels")

    private fun invokePanels(methodName: String) {
        runCatching {
            @SuppressLint("WrongConstant")
            val service: Any? = Utils.app.getSystemService("statusbar")

            @SuppressLint("PrivateApi")
            val statusBarManager: Class<*> = Class.forName("android.app.StatusBarManager")
            val expand: Method = statusBarManager.getMethod(methodName)
            expand.invoke(service)
        }.getOrElse { e ->
            e.printStackTrace()
        }
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */
    // navigation bar
    /**
     * @ return the navigation bar's height
     */
    @Suppress("InternalInsetResource", "DiscouragedApi")
    val navBarHeight: Int
        get() {
            val height = ActivityUtils.topActivity?.let {
                val insets = ViewCompat.getRootWindowInsets(it.window.decorView)
                insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.top
            }
            if (height != null) return height
            val res: Resources = Resources.getSystem()
            val resourceId: Int = res.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId != 0) res.getDimensionPixelSize(resourceId)
            else {
                0
            }
        }

    /**
     * Set the navigation bar's visibility.
     *
     * @param activity The activity.
     * @param isVisible True to set navigation bar visible, false otherwise.
     */
    fun setNavBarVisibility(activity: Activity, isVisible: Boolean) {
        setNavBarVisibility(activity.window, isVisible)
    }

    /**
     * Set the navigation bar's visibility.
     *
     * @param window The window.
     * @param isVisible True to set navigation bar visible, false otherwise.
     */
    fun setNavBarVisibility(window: Window, isVisible: Boolean) {
        val decorView: ViewGroup = window.decorView as ViewGroup
        var i = 0
        val count: Int = decorView.childCount
        while (i < count) {
            val child: View = decorView.getChildAt(i)
            val id: Int = child.id
            if (id != View.NO_ID) {
                val resourceEntryName: String = getResNameById(id)
                if ("navigationBarBackground" == resourceEntryName) {
                    child.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
                }
            }
            i++
        }
        val uiOptions: Int =
            (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (isVisible) {
            decorView.systemUiVisibility = decorView.systemUiVisibility and uiOptions.inv()
        } else {
            decorView.systemUiVisibility = decorView.systemUiVisibility or uiOptions
        }
    }

    /**
     * Return whether the navigation bar visible.
     *
     * Call it in onWindowFocusChanged will get right result.
     *
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isNavBarVisible(activity: Activity): Boolean {
        return isNavBarVisible(activity.window)
    }

    /**
     * Return whether the navigation bar visible.
     *
     * Call it in onWindowFocusChanged will get right result.
     *
     * @param window The window.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isNavBarVisible(window: Window): Boolean {
        var isVisible = false
        val decorView: ViewGroup = window.decorView as ViewGroup
        var i = 0
        val count: Int = decorView.childCount
        while (i < count) {
            val child: View = decorView.getChildAt(i)
            val id: Int = child.id
            if (id != View.NO_ID) {
                val resourceEntryName: String = getResNameById(id)
                if (
                    "navigationBarBackground" == resourceEntryName &&
                    child.isVisible
                ) {
                    isVisible = true
                    break
                }
            }
            i++
        }
        if (isVisible) {
            val visibility: Int = decorView.systemUiVisibility
            isVisible = (visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
        }

        return isVisible
    }

    private fun getResNameById(id: Int): String = runCatching {
        Utils.app.resources.getResourceEntryName(id)
    }
        .getOrElse { "" }

    /**
     * Set the navigation bar's color.
     *
     * @param activity The activity.
     * @param color The navigation bar's color.
     */
    fun setNavBarColor(activity: Activity, @ColorInt color: Int) {
        setNavBarColor(activity.window, color)
    }

    /**
     * Set the navigation bar's color.
     *
     * @param window The window.
     * @param color The navigation bar's color.
     */
    fun setNavBarColor(window: Window, @ColorInt color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = color
    }

    /**
     * Return the color of navigation bar.
     *
     * @param activity The activity.
     * @return the color of navigation bar
     */
    fun getNavBarColor(activity: Activity): Int {
        return getNavBarColor(activity.window)
    }

    /**
     * Return the color of navigation bar.
     *
     * @param window The window.
     * @return the color of navigation bar
     */
    fun getNavBarColor(window: Window): Int {
        return window.navigationBarColor
    }

    val isSupportNavBar: Boolean
        /**
         * Return whether the navigation bar visible.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            val wm: WindowManager =
                Utils.app.getSystemService(Context.WINDOW_SERVICE) as? WindowManager?
                    ?: return false
            val display: Display = wm.defaultDisplay
            val size = Point()
            val realSize = Point()
            display.getSize(size)
            display.getRealSize(realSize)
            return realSize.y != size.y || realSize.x != size.x
        }

    /**
     * Set the nav bar's light mode.
     *
     * @param activity The activity.
     * @param isLightMode True to set nav bar light mode, false otherwise.
     */
    fun setNavBarLightMode(activity: Activity, isLightMode: Boolean) {
        setNavBarLightMode(activity.window, isLightMode)
    }

    /**
     * Set the nav bar's light mode.
     *
     * @param window The window.
     * @param isLightMode True to set nav bar light mode, false otherwise.
     */
    fun setNavBarLightMode(window: Window, isLightMode: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView: View = window.decorView
            var vis: Int = decorView.systemUiVisibility
            vis =
                if (isLightMode) {
                    vis or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    vis and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            decorView.systemUiVisibility = vis
        }
    }

    /**
     * Is the nav bar light mode.
     *
     * @param activity The activity.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isNavBarLightMode(activity: Activity): Boolean {
        return isNavBarLightMode(activity.window)
    }

    /**
     * Is the nav bar light mode.
     *
     * @param window The window.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isNavBarLightMode(window: Window): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView: View = window.decorView
            val vis: Int = decorView.systemUiVisibility
            return (vis and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) != 0
        }
        return false
    }

    fun transparentNavBar(activity: Activity) {
        transparentNavBar(activity.window)
    }

    fun transparentNavBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false)
        }
        window.navigationBarColor = Color.TRANSPARENT
        val decorView: View = window.decorView
        val vis: Int = decorView.systemUiVisibility
        val option: Int =
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = vis or option
    }
}
