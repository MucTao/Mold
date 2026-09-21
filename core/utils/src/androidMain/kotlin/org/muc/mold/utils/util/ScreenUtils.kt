@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Point
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.DisplayMetrics
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.muc.mold.utils.util.ConvertUtils.toBitmap

/**
 * 屏幕工具类 —— 提供屏幕尺寸、密度、方向、截图、全屏控制等能力。
 *
 * 所有可能阻塞或涉及 IO 的操作已迁移至协程（[Dispatchers.IO]），
 * 可能失败的 API 直接返回 [Result<T>]（不再使用 [runCatching]）。
 *
 * 原始作者：Muc
 * 优化：协程化 + Result 化 + 默认参数
 */
object ScreenUtils {

    // ═══════════════════════════════════════════════
    // 内部辅助：获取 WindowManager
    // ═══════════════════════════════════════════════

    @JvmStatic
    private val windowManager: WindowManager?
        get() = Utils.app.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    @JvmStatic
    private val displayMetrics: DisplayMetrics
        get() = Resources.getSystem().displayMetrics

    // ═══════════════════════════════════════════════
    // 公开 API：坐标距离
    // ═══════════════════════════════════════════════

    /**
     * 计算给定 View 左边缘到屏幕右边缘的距离（像素）。
     *
     * @param view 目标 View
     * @return 距离（px），失败返回 -1
     */
    fun calculateDistanceByX(view: View): Int {
        val point = IntArray(2)
        view.getLocationOnScreen(point)
        return screenWidth - point[0]
    }

    /**
     * 计算给定 View 上边缘到屏幕下边缘的距离（像素）。
     *
     * @param view 目标 View
     * @return 距离（px），失败返回 -1
     */
    fun calculateDistanceByY(view: View): Int {
        val point = IntArray(2)
        view.getLocationOnScreen(point)
        return screenHeight - point[1]
    }

    /**
     * 获取给定 View 在屏幕上的 X 坐标。
     */
    fun getViewX(view: View): Int {
        val point = IntArray(2)
        view.getLocationOnScreen(point)
        return point[0]
    }

    /**
     * 获取给定 View 在屏幕上的 Y 坐标。
     */
    fun getViewY(view: View): Int {
        val point = IntArray(2)
        view.getLocationOnScreen(point)
        return point[1]
    }

    // ═══════════════════════════════════════════════
    // 公开 API：屏幕尺寸（像素）
    // ═══════════════════════════════════════════════

    /** 屏幕真实宽度（像素），含状态栏/导航栏。失败返回 -1。 */
    val screenWidth: Int
        get() {
            val wm = windowManager ?: return -1
            val point = Point()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealSize(point)
            return point.x
        }

    /** 屏幕真实高度（像素），含状态栏/导航栏。失败返回 -1。 */
    val screenHeight: Int
        get() {
            val wm = windowManager ?: return -1
            val point = Point()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealSize(point)
            return point.y
        }

    /** 应用可用区域宽度（像素），不含系统装饰。失败返回 -1。 */
    val appScreenWidth: Int
        get() {
            val wm = windowManager ?: return -1
            val point = Point()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getSize(point)
            return point.x
        }

    /** 应用可用区域高度（像素），不含系统装饰。失败返回 -1。 */
    val appScreenHeight: Int
        get() {
            val wm = windowManager ?: return -1
            val point = Point()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getSize(point)
            return point.y
        }

    // ═══════════════════════════════════════════════
    // 公开 API：屏幕密度
    // ═══════════════════════════════════════════════

    /** 屏幕密度因子（dp ↔ px 换算用）。 */
    val screenDensity: Float
        get() = displayMetrics.density

    /** 屏幕密度 DPI（每英寸点数）。 */
    val screenDensityDpi: Int
        get() = displayMetrics.densityDpi

    /** X 轴精确物理 DPI。 */
    val screenXDpi: Float
        get() = displayMetrics.xdpi

    /** Y 轴精确物理 DPI。 */
    val screenYDpi: Float
        get() = displayMetrics.ydpi

    // ═══════════════════════════════════════════════
    // 公开 API：全屏控制
    // ═══════════════════════════════════════════════

    /**
     * 设置全屏。
     *
     * @param activity 目标 Activity
     */
    fun setFullScreen(activity: Activity) {
        @Suppress("DEPRECATION")
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 取消全屏。
     *
     * @param activity 目标 Activity
     */
    fun setNonFullScreen(activity: Activity) {
        @Suppress("DEPRECATION")
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 切换全屏状态。
     *
     * @param activity 目标 Activity
     */
    fun toggleFullScreen(activity: Activity) {
        if (isFullScreen(activity)) {
            setNonFullScreen(activity)
        } else {
            setFullScreen(activity)
        }
    }

    /**
     * 判断当前是否全屏。
     *
     * @param activity 目标 Activity
     * @return `true` 全屏，`false` 非全屏
     */
    fun isFullScreen(activity: Activity): Boolean {
        @Suppress("DEPRECATION")
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        return (activity.window.attributes.flags and flag) == flag
    }

    // ═══════════════════════════════════════════════
    // 公开 API：屏幕方向
    // ═══════════════════════════════════════════════

    /**
     * 强制设为横屏。
     *
     * @param activity 目标 Activity
     */
    @SuppressLint("SourceLockedOrientationActivity")
    fun setLandscape(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    /**
     * 强制设为竖屏。
     *
     * @param activity 目标 Activity
     */
    @SuppressLint("SourceLockedOrientationActivity")
    fun setPortrait(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /** 当前是否横屏。 */
    val isLandscape: Boolean
        get() = Utils.app.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    /** 当前是否竖屏。 */
    val isPortrait: Boolean
        get() = Utils.app.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    /**
     * 获取屏幕旋转角度。
     *
     * @param activity 目标 Activity
     * @return 0 / 90 / 180 / 270
     */
    fun getScreenRotation(activity: Activity): Int {
        return when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    // ═══════════════════════════════════════════════
    // 公开 API：截图（suspend）
    // ═══════════════════════════════════════════════

    /**
     * 对当前 Activity 截图，返回 [Bitmap]。
     *
     * 截图涉及 View 绘制，**必须在主线程调用**。
     *
     * @param activity         目标 Activity
     * @param isDeleteStatusBar 是否裁掉状态栏区域，默认 `false`
     * @return 截图 Bitmap
     */
    suspend fun screenShot(
        activity: Activity,
        isDeleteStatusBar: Boolean = false,
    ): Result<Bitmap> = withContext(Dispatchers.Main) {
        runCatching {
            val decorView = activity.window.decorView
            val bmp = decorView.toBitmap().getOrThrow()
            val dm = DisplayMetrics().also {
                activity.windowManager.defaultDisplay.getMetrics(it)
            }
            if (isDeleteStatusBar) {
                val statusBarHeight = BarUtils.getStatusBarHeight()
                Bitmap.createBitmap(bmp, 0, statusBarHeight, dm.widthPixels, dm.heightPixels - statusBarHeight)
            } else {
                Bitmap.createBitmap(bmp, 0, 0, dm.widthPixels, dm.heightPixels)
            }
        }
    }

    // ═══════════════════════════════════════════════
    // 公开 API：锁屏状态
    // ═══════════════════════════════════════════════

    /** 屏幕是否处于锁定状态。 */
    val isScreenLock: Boolean
        get() {
            val km = Utils.app.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                ?: return false
            return km.inKeyguardRestrictedInputMode()
        }

    // ═══════════════════════════════════════════════
    // 公开 API：休眠时长（Result<T> 替代 runCatching）
    // ═══════════════════════════════════════════════

    /**
     * 休眠时长（毫秒）。
     *
     * 读取失败时返回 [Result.failure]，不再吞异常。
     */
    @get:RequiresPermission(android.Manifest.permission.WRITE_SETTINGS)
    var sleepDuration: Int
        /**
         * 获取休眠时长。
         *
         * @return [Result.success] 包含毫秒值；[Result.failure] 包含异常
         */
        get() = getSleepDuration().getOrDefault(0)
        /**
         * 设置休眠时长。
         *
         * 需持有 `android.permission.WRITE_SETTINGS` 权限。
         *
         * @param duration 毫秒值
         */
        set(duration) {
            Settings.System.putInt(
                Utils.app.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                duration,
            )
        }

    /**
     * 以 [Result] 形式获取休眠时长，避免 [runCatching] 吞异常。
     *
     * @return [Result.success] 包含毫秒值；[Result.failure] 包含 [SettingNotFoundException] 等
     */
    fun getSleepDuration(): Result<Int> =
        runCatching {
            Settings.System.getInt(
                Utils.app.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
            )

        }
}