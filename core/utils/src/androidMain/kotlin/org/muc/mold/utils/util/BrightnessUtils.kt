@file:Suppress("unused")

package org.muc.mold.utils.util

import android.content.ContentResolver
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import androidx.annotation.IntRange

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2018/02/08
 * desc  : utils about brightness
 * </pre> *
 */
object BrightnessUtils {
    val isAutoBrightnessEnabled: Boolean
        /**
         * Return whether automatic brightness mode is enabled.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            return runCatching {
                val mode: Int =
                    Settings.System.getInt(
                        Utils.app.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                    )
                return@runCatching mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            }.onFailure { e -> e.printStackTrace() }.getOrDefault(false)
        }

    /**
     * Enable or disable automatic brightness mode.
     *
     * Must hold `<uses-permission android:name="android.permission.WRITE_SETTINGS" />`
     *
     * @param enabled True to enabled, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun setAutoBrightnessEnabled(enabled: Boolean): Boolean {
        return Settings.System.putInt(
            Utils.app.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            if (enabled) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
        )
    }

    val brightness: Int
        /**
         * 获取屏幕亮度
         *
         * @return 屏幕亮度 0-255
         */
        get() {
            return runCatching {
                Settings.System.getInt(
                    Utils.app.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                )
            }.onFailure { e -> e.printStackTrace() }.getOrDefault(0)
        }

    /**
     * 设置屏幕亮度
     *
     * 需添加权限 `<uses-permission android:name="android.permission.WRITE_SETTINGS" />` 并得到授权
     *
     * @param brightness 亮度值
     */
    fun setBrightness(@IntRange(from = 0, to = 255) brightness: Int): Boolean {
        val resolver: ContentResolver = Utils.app.contentResolver
        val b: Boolean =
            Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
        resolver.notifyChange(Settings.System.getUriFor("screen_brightness"), null)
        return b
    }

    /**
     * 设置窗口亮度
     *
     * @param window 窗口
     * @param brightness 亮度值
     */
    fun setWindowBrightness(
        window: Window,
        @IntRange(from = 0, to = 255) brightness: Int,
    ) {
        val lp: WindowManager.LayoutParams = window.attributes
        lp.screenBrightness = brightness / 255f
        window.setAttributes(lp)
    }

    /**
     * 获取窗口亮度
     *
     * @param window 窗口
     * @return 屏幕亮度 0-255
     */
    fun getWindowBrightness(window: Window): Int {
        val lp: WindowManager.LayoutParams = window.attributes
        val brightness: Float = lp.screenBrightness
        if (brightness < 0) return BrightnessUtils.brightness
        return (brightness * 255).toInt()
    }
}
