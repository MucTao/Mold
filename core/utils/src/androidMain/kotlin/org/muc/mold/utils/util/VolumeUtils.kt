@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * 音量工具类 —— 提供音量控制 / 静音切换 / 音频焦点管理 / 音效播放能力。
 *
 * ## 核心功能
 * - 音量调节：增大 / 减小 / 设置绝对值 / 获取当前值 / 获取最大值
 * - 静音切换：静音 / 取消静音 / 查静音状态
 * - 音频焦点：请求焦点 → 播放音效 → 释放焦点
 * - 快捷操作：`playDTMF(digit)` / `playSoundEffect(effect)` / `playBeep(gain)`
 *
 * ## 权限要求
 * 无需额外权限（音频焦点基于 [AudioManager]）。
 *
 * 原始作者：Muc
 * 优化：`Handler` / `ThreadUtils` → `CoroutineScope(Dispatchers.IO)`、
 * `runCatching` → `try/catch` Result<T>、补全注释、默认参数精简重载
 */
object VolumeUtils {

    /** 音频流类型（常用）。 */
    enum class StreamType(val constant: Int) {
        /** 通话音量 */
        VOICE(AudioManager.STREAM_VOICE_CALL),

        /** 系统音量 */
        SYSTEM(AudioManager.STREAM_SYSTEM),

        /** 铃声音量 */
        RING(AudioManager.STREAM_RING),

        /** 媒体音量 */
        MUSIC(AudioManager.STREAM_MUSIC),

        /** 闹钟音量 */
        ALARM(AudioManager.STREAM_ALARM),

        /** 通知音量 */
        NOTIFICATION(AudioManager.STREAM_NOTIFICATION),
    }

    // ── 公开 API：音量调节 ────────────────────────

    /**
     * 获取当前音量。
     *
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @return [Result.success] 包含当前音量值；[Result.failure] 包含异常
     */
    fun getVolume(
        streamType: StreamType = StreamType.MUSIC
    ): Result<Int> = runCatching {
        val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.getStreamVolume(streamType.constant)
    }


    /**
     * 获取最大音量。
     *
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @return [Result.success] 包含最大音量值；[Result.failure] 包含异常
     */
    fun getMaxVolume(
        streamType: StreamType = StreamType.MUSIC
    ): Result<Int> = runCatching {
        val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.getStreamMaxVolume(streamType.constant)
    }

    /**
     * 设置绝对音量。
     *
     * @param volume     目标音量（0 ~ 最大值）
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @param flags      标志位，默认 [AudioManager.FLAG_SHOW_UI]
     * @return [Result.success] 或 [Result.failure]
     */
    suspend fun setVolume(
        volume: Int,
        streamType: StreamType = StreamType.MUSIC,
        flags: Int = AudioManager.FLAG_SHOW_UI
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(streamType.constant, volume, flags)
            volume
        }
    }

    /**
     * 音量增加一档。
     *
     * @param delta      增加量（正数），默认 1
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @param flags      标志位，默认 [AudioManager.FLAG_SHOW_UI]
     * @return [Result.success] 包含调整后的音量值；[Result.failure] 包含异常
     */
    suspend fun volumeUp(
        delta: Int = 1,
        streamType: StreamType = StreamType.MUSIC,
        flags: Int = AudioManager.FLAG_SHOW_UI
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val current = audioManager.getStreamVolume(streamType.constant)
            val max = audioManager.getStreamMaxVolume(streamType.constant)
            val newVolume = (current + delta).coerceAtMost(max)
            audioManager.setStreamVolume(streamType.constant, newVolume, flags)
            newVolume
        }
    }

    /**
     * 音量降低一档。
     *
     * @param delta      减少量（正数），默认 1
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @param flags      标志位，默认 [AudioManager.FLAG_SHOW_UI]
     * @return [Result.success] 包含调整后的音量值；[Result.failure] 包含异常
     */
    suspend fun volumeDown(
        delta: Int = 1,
        streamType: StreamType = StreamType.MUSIC,
        flags: Int = AudioManager.FLAG_SHOW_UI
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val current = audioManager.getStreamVolume(streamType.constant)
            val newVolume = (current - delta).coerceAtLeast(0)
            audioManager.setStreamVolume(streamType.constant, newVolume, flags)
            newVolume
        }
    }

    // ── 公开 API：静音 ────────────────────────────

    /**
     * 切换静音状态。
     * @param volume 取消静音时的音量 默认 max / 2
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @return [Result.success] 包含当前是否静音；[Result.failure] 包含异常
     */
    suspend fun toggleMute(
        volume: Int? = null,
        streamType: StreamType = StreamType.MUSIC,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val current = audioManager.getStreamVolume(streamType.constant)
            if (current > 0) {
                mute(streamType)
                true  // 已静音
            } else {
                unmute(volume, streamType)
                false // 已取消静音
            }
        }
    }

    /**
     * 静音指定流。
     *
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @return [Result.success] 或 [Result.failure]
     */
    suspend fun mute(
        streamType: StreamType = StreamType.MUSIC
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(streamType.constant, 0, 0)
        }
    }

    /**
     * 取消静音。
     * @param volume 取消静音时的音量 默认 max / 2
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @return [Result.success] 或 [Result.failure]
     */
    suspend fun unmute(
        volume: Int? = null,
        streamType: StreamType = StreamType.MUSIC
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = audioManager.getStreamMaxVolume(streamType.constant)
            audioManager.setStreamVolume(streamType.constant, volume ?: (max / 2), 0)
        }
    }

    /**
     * 查询指定流是否静音。
     *
     * @param streamType 音频流类型，默认 [StreamType.MUSIC]
     * @return [Result.success] 包含 `true`（已静音）或 `false`；[Result.failure] 包含异常
     */
    suspend fun isMuted(
        streamType: StreamType = StreamType.MUSIC
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val volume = audioManager.getStreamVolume(streamType.constant)
            volume == 0
        }
    }

    // ── 公开 API：音频焦点 + 音效 ────────────────

    /**
     * 快速播放 DTMF 音（双音多频）。
     * DTMF（Dual-Tone Multi-Frequency，双音多频）是一种电话拨号技术。
     * 简单来说，就是当你按下电话键盘上的按键时，每个键会发出由两个特定频率组合而成的声音，电话系统通过识别这两个频率来判断你按的是哪个键。
     *
     * @param digit DTMF 字符（`'0'`~`'9'`、`'*'`、`'#'`、`'A'`~`'D'`）
     * @param gain  音量增益比（0.0 ~ 1.0），默认 0.3f
     * @return [Result.success] 或 [Result.failure]
     */
    suspend fun playDTMF(
        digit: Char,
        gain: Float = 0.3f
    ): Result<Unit> = playTone(ToneGenerator.getToneForDigit(digit), 200, gain)

    /**
     * 快速播放系统音效（如按键音）。
     *
     * @param effect 系统音效常量，如 [AudioManager.FX_KEY_CLICK]
     * @param gain   音量增益比（0.0 ~ 1.0），默认 0.5f
     * @return [Result.success] 或 [Result.failure]
     */
    suspend fun playSoundEffect(
        effect: Int,
        gain: Float = 0.5f
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.playSoundEffect(effect, gain)
        }
    }

    /**
     * 快速播放短促哔声（纯音）。
     *
     * @param gain 音量增益比（0.0 ~ 1.0），默认 0.5f
     * @return [Result.success] 或 [Result.failure]
     */
    suspend fun playBeep(
        gain: Float = 0.5f
    ): Result<Unit> = playTone(ToneGenerator.TONE_PROP_BEEP, 200, gain)

    /**
     * 播放指定频率/时长的纯音，并自动管理音频焦点。
     *
     * @param toneType   音调类型（ToneGenerator 常量）
     * @param durationMs 播放时长（ms），默认 200ms
     * @param gain       音量增益比（0.0 ~ 1.0），默认 0.5f
     * @return [Result.success] 或 [Result.failure]
     */
    suspend fun playTone(
        toneType: Int,
        durationMs: Int = 200,
        gain: Float = 0.5f
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. 请求音频焦点
            requestAudioFocus()
            // 2. 播放
            playToneInternal(toneType, durationMs, gain)
            // 3. 等待播放完毕
            delay(durationMs.toLong().milliseconds)
            // 4. 释放音频焦点
            abandonAudioFocus()

            Result.success(Unit)
        } catch (e: Exception) {
            abandonAudioFocus()
            Result.failure(e)
        }
    }

    // ── 内部实现 ─────────────────────────────────

    /** 音频焦点请求（API 26+ 用 AudioFocusRequest，以下用旧 API）。 */
    private var audioFocusRequest: AudioFocusRequest? = null

    /**
     * 请求音频焦点。
     */
    private fun requestAudioFocus() {
        val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .build()
            audioFocusRequest = focusRequest
            audioManager.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    /**
     * 释放音频焦点。
     */
    private fun abandonAudioFocus() {
        val audioManager = Utils.app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    /**
     * 播放纯音（AudioTrack 低级别生成正弦波）。
     *
     * @param toneType   音调类型
     * @param durationMs 时长（ms）
     * @param gain       增益（0.0 ~ 1.0）
     */
    private fun playToneInternal(toneType: Int, durationMs: Int, gain: Float) {
        val sampleRate = 44100
        val numSamples = durationMs * sampleRate / 1000
        val samples = ShortArray(numSamples)

        val frequency = ToneGenerator.getFrequency(toneType)
        for (i in 0 until numSamples) {
            samples[i] = (gain * Short.MAX_VALUE * kotlin.math.sin(
                2.0 * Math.PI * i.toDouble() / (sampleRate / frequency)
            )).toInt().toShort()
        }

        val audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            numSamples * 2,
            AudioTrack.MODE_STATIC,
            0
        )

        audioTrack.write(samples, 0, numSamples)
        audioTrack.play()
        audioTrack.release()
    }
}

/**
 * 音调生成器（频率 + 类型映射）。
 */
private object ToneGenerator {

    const val TONE_PROP_BEEP = 24

    /** DTMF 行频率。 */
    private val DTMF_ROW_FREQUENCIES = intArrayOf(697, 770, 852, 941)

    /** DTMF 列频率。 */
    private val DTMF_COL_FREQUENCIES = intArrayOf(1209, 1336, 1477, 1633)

    /** 获取音调对应的频率（Hz）。 */
    fun getFrequency(toneType: Int): Double {
        return when (toneType) {
            TONE_PROP_BEEP -> 1000.0
            in 0..15 -> getDTMFFrequency(toneType)
            else -> 440.0 // 默认 A4
        }
    }

    /** 根据字符获取 DTMF 音调类型。 */
    fun getToneForDigit(digit: Char): Int {
        return when (digit) {
            '0' -> 0; '1' -> 1; '2' -> 2; '3' -> 3
            '4' -> 4; '5' -> 5; '6' -> 6; '7' -> 7
            '8' -> 8; '9' -> 9; '*' -> 10; '#' -> 11
            'A' -> 12; 'B' -> 13; 'C' -> 14; 'D' -> 15
            else -> throw IllegalArgumentException("Invalid DTMF digit: $digit")
        }
    }

    /** 计算 DTMF 双频频率（高频+低频平均值）。 */
    private fun getDTMFFrequency(toneType: Int): Double {
        val row = toneType / 4
        val col = toneType % 4
        return (DTMF_ROW_FREQUENCIES[row] + DTMF_COL_FREQUENCIES[col]) / 2.0
    }
}