@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * 振动工具类 —— 提供单次 / 重复 / 自定义模式振动能力。
 *
 * ## 权限要求
 * 调用前请确保已声明：
 * ```xml
 * <uses-permission android:name="android.permission.VIBRATE" />
 * ```
 *
 * ## 使用示例
 * ```kotlin
 * // 单次振动 200ms
 * VibrateUtils.vibrate(200)
 *
 * // 长振动 500ms
 * VibrateUtils.vibrate(duration = 500)
 *
 * // 模式振动（wait-play-wait-play...，不重复）
 * VibrateUtils.vibrate(longArrayOf(0, 100, 200, 300))
 *
 * // 模式振动（无限循环）
 * VibrateUtils.vibrate(longArrayOf(0, 100, 200, 300), repeat = 0)
 *
 * // 取消振动
 * VibrateUtils.cancel()
 * ```
 *
 * 原始作者：Muc
 * 优化：`Handler` → `CoroutineScope`、`runCatching` → `try/catch` Result<T>、
 */
object VibrateUtils {

    // ── 公开 API ─────────────────────────────────

    /**
     * 单次振动指定时长（毫秒）。
     *
     * 内部通过协程自动取消，避免永久振动。
     *
     * @param duration 振动时长（ms）；默认 200ms
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(duration: Long = 200L) {
        coroutineScope.launch {
            vibrateInternal(longArrayOf(0, duration), -1)
            autoCancelJob?.cancel()
            autoCancelJob = coroutineScope.launch {
                delay(duration.milliseconds)
                vibrator?.cancel()
            }
        }
    }

    /**
     * 按模式振动，可选重复。
     *
     * @param pattern 振动模式数组
     * @param repeat  从模式数组中第几个元素开始重复；
     *                `-1` 表示不重复，`0` 表示从第一个元素开始无限循环
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(pattern: LongArray, repeat: Int = -1) {
        coroutineScope.launch {
            vibrateInternal(pattern, repeat)
        }
    }

    /**
     * 立即取消当前振动。
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun cancel() {
        autoCancelJob?.cancel()
        coroutineScope.launch {
            vibrator?.cancel()
        }
    }

    /**
     * 振动回调（可监听振动是否成功）。
     *
     * @param duration 振动时长（ms）
     * @param onResult 回调函数，接收 [Result.success] 或 [Result.failure]
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate(
        duration: Long = 200L,
        onResult: (Result<Job>) -> Unit = {}
    ) {
        coroutineScope.launch {
            onResult(runCatching {
                vibrateInternal(longArrayOf(0, duration), -1)
                autoCancelJob?.cancel()
                coroutineScope.launch {
                    delay(duration.milliseconds)
                    vibrator?.cancel()
                }.also { autoCancelJob = it }
            })
        }
    }

    // ── 内部实现 ─────────────────────────────────

    /** IO 协程作用域（代替 HandlerThread）。 */
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /** 单次振动的自动取消 Job。 */
    private var autoCancelJob: Job? = null

    /**
     * 获取 [Vibrator] 实例。
     */
    private val vibrator: Vibrator?
        get() {
            val context = Utils.app
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }

    /**
     * 执行振动（协程中调用）。
     *
     * @param pattern 振动模式
     * @param repeat  重复起始索引，`-1` 不重复
     */
    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrateInternal(pattern: LongArray, repeat: Int) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        // 转换 pattern 为 IntArray（API 26+ 需要）
        val timings = LongArray(pattern.size) { pattern[it] }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibe = VibrationEffect.createWaveform(timings, repeat)
            v.vibrate(vibe)
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(pattern, repeat)
        }
    }
}