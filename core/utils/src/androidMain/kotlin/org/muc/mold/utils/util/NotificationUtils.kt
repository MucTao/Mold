@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.Manifest
import android.Manifest.permission.EXPAND_STATUS_BAR
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.lang.reflect.Method

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2019/10/20
 * desc  : utils about notification (coroutines edition)
 * </pre>
 */
object NotificationUtils {

    const val IMPORTANCE_UNSPECIFIED: Int = -1000
    const val IMPORTANCE_NONE: Int = 0
    const val IMPORTANCE_MIN: Int = 1
    const val IMPORTANCE_LOW: Int = 2
    const val IMPORTANCE_DEFAULT: Int = 3
    const val IMPORTANCE_HIGH: Int = 4

    // ====================================================================================
    //  查询
    // ====================================================================================

    /** Return whether the notifications enabled. */
    fun areNotificationsEnabled(): Boolean =
        NotificationManagerCompat.from(Utils.app).areNotificationsEnabled()

    // ====================================================================================
    //  发送通知（挂起 lambda 替代 Utils.Consumer）
    // ====================================================================================

    /**
     * Post a notification to be shown in the status bar.
     *
     * @param id An identifier for this notification.
     * @param tag A string identifier for this notification. May be `null`.
     * @param channelConfig The notification channel of config.
     * @param builderBlock 挂起 lambda，允许在里面对 builder 做异步操作（如加载 Bitmap）。
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    suspend fun notify(
        id: Int,
        tag: String? = null,
        channelConfig: ChannelConfig = ChannelConfig.DEFAULT_CHANNEL_CONFIG,
        builderBlock: suspend NotificationCompat.Builder.() -> Unit = {},
    ) {
        val notification = buildNotification(channelConfig, builderBlock)
        NotificationManagerCompat.from(Utils.app).notify(tag, id, notification)
    }

    /**
     * 构建 [Notification]，供需要自己发送（比如 `startForeground`）的场景使用。
     */
    suspend fun buildNotification(
        channelConfig: ChannelConfig = ChannelConfig.DEFAULT_CHANNEL_CONFIG,
        builderBlock: suspend NotificationCompat.Builder.() -> Unit = {},
    ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = Utils.app.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            nm.createNotificationChannel(channelConfig.notificationChannel)
        }

        val builder = NotificationCompat.Builder(Utils.app)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelConfig.notificationChannel.id)
        }
        // 挂起 lambda 内允许切线程、suspend 调用
        builder.builderBlock()
        return builder.build()
    }


    // ====================================================================================
    //  取消
    // ====================================================================================

    fun cancel(tag: String?, id: Int) {
        NotificationManagerCompat.from(Utils.app).cancel(tag, id)
    }

    fun cancel(id: Int) {
        NotificationManagerCompat.from(Utils.app).cancel(id)
    }

    fun cancelAll() {
        NotificationManagerCompat.from(Utils.app).cancelAll()
    }

    // ====================================================================================
    //  状态栏可见性
    // ====================================================================================

    /**
     * Set the notification bar's visibility.
     *
     * Must hold `<uses-permission android:name="android.permission.EXPAND_STATUS_BAR" />`
     */
    @RequiresPermission(EXPAND_STATUS_BAR)
    fun setNotificationBarVisibility(isVisible: Boolean) {
        val methodName = if (isVisible) "expandNotificationsPanel" else "collapsePanels"
        invokePanels(methodName)
    }

    private fun invokePanels(methodName: String) {
        runCatching {
            @SuppressLint("WrongConstant")
            val service: Any? = Utils.app.getSystemService("statusbar")

            @SuppressLint("PrivateApi")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val method: Method = statusBarManager.getMethod(methodName)
            method.invoke(service)
        }.onFailure { e ->
            if (e !is Exception) throw e
            e.printStackTrace()
        }
    }

    // ====================================================================================
    //  注解
    // ====================================================================================


    // ====================================================================================
    //  ChannelConfig
    // ====================================================================================
    class ChannelConfig(
        id: String?,
        name: CharSequence?,
        importance: Int,
    ) {
        private var mNotificationChannel: NotificationChannel? = null

        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationChannel = NotificationChannel(id, name, importance)
            }
        }

        val notificationChannel: NotificationChannel
            get() = mNotificationChannel ?: error("NotificationChannel is only available on Android O+")

        @RequiresApi(Build.VERSION_CODES.O)
        fun setBypassDnd(bypassDnd: Boolean): ChannelConfig = applyO {
            it.setBypassDnd(bypassDnd)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setDescription(description: String?): ChannelConfig = applyO {
            it.description = description
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setGroup(groupId: String?): ChannelConfig = applyO {
            it.group = groupId
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setImportance(importance: Int): ChannelConfig = applyO {
            it.importance = importance
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setLightColor(argb: Int): ChannelConfig = applyO {
            it.lightColor = argb
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setLockscreenVisibility(lockscreenVisibility: Int): ChannelConfig = applyO {
            it.lockscreenVisibility = lockscreenVisibility
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setName(name: CharSequence?): ChannelConfig = applyO {
            it.name = name
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setShowBadge(showBadge: Boolean): ChannelConfig = applyO {
            it.setShowBadge(showBadge)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setSound(sound: Uri?, audioAttributes: AudioAttributes?): ChannelConfig = applyO {
            it.setSound(sound, audioAttributes)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun setVibrationPattern(vibrationPattern: LongArray?): ChannelConfig = applyO {
            it.setVibrationPattern(vibrationPattern)
        }

        /** Android O+ 才真正执行 channel 配置，低版本静默跳过。 */
        private inline fun applyO(block: (NotificationChannel) -> Unit): ChannelConfig {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationChannel?.let(block)
            }
            return this
        }

        companion object {
            val DEFAULT_CHANNEL_CONFIG: ChannelConfig by lazy {
                val pkg = Utils.app.packageName
                ChannelConfig(pkg, pkg, IMPORTANCE_DEFAULT)
            }
        }
    }
}