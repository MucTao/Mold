@file:Suppress("unused")

package org.muc.mold.utils.util

import org.muc.mold.utils.util.NumberUtils.float2Double
import org.muc.mold.utils.util.NumberUtils.format
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2020/04/12
 * desc  : utils about number
 * </pre>
 */
object NumberUtils {

    /**
     * DecimalFormat 不是线程安全的，用 ThreadLocal 为每个线程提供独立实例。
     * 初始化后必定非空，所以泛型用非空 `DecimalFormat`，消灭后续的 `!!`。
     */
    private val DF_THREAD_LOCAL: ThreadLocal<DecimalFormat> =
        object : ThreadLocal<DecimalFormat>() {
            override fun initialValue(): DecimalFormat =
                NumberFormat.getInstance() as DecimalFormat
        }

    val safeDecimalFormat: DecimalFormat? get() = DF_THREAD_LOCAL.get()

    // ====================================================================================
    /**
     * Format the value.
     *
     * @param value The value.
     * @param fractionDigits The number of digits allowed in the fraction portion of value.
     * @param isHalfUp True to rounded towards the nearest neighbor. Default: true
     * @param minIntegerDigits The minimum number of digits allowed in the integer portion of value. Default: 1
     * @param isGrouping True to set grouping will be used in this format, false otherwise. Default: false
     */
    @JvmOverloads
    fun format(
        value: Double,
        fractionDigits: Int,
        isHalfUp: Boolean = true,
        minIntegerDigits: Int = 1,
        isGrouping: Boolean = false,
    ): String {
        val nf = safeDecimalFormat ?: return value.toString()
        nf.isGroupingUsed = isGrouping
        nf.roundingMode = if (isHalfUp) RoundingMode.HALF_UP else RoundingMode.DOWN
        nf.minimumIntegerDigits = minIntegerDigits
        nf.minimumFractionDigits = fractionDigits
        nf.maximumFractionDigits = fractionDigits
        return nf.format(value)
    }

    /**
     * Format the value.
     *
     * Float 版先经 [float2Double] 转 Double，规避 `1.1f` 这类浮点精度陷阱。
     * 参数语义与 [format] 的 Double 版完全一致。
     */
    @JvmOverloads
    fun format(
        value: Float,
        fractionDigits: Int,
        isHalfUp: Boolean = true,
        minIntegerDigits: Int = 1,
        isGrouping: Boolean = false,
    ): String = format(
        value = float2Double(value),
        fractionDigits = fractionDigits,
        isHalfUp = isHalfUp,
        minIntegerDigits = minIntegerDigits,
        isGrouping = isGrouping,
    )

    // ====================================================================================
    //  Float → Double
    // ====================================================================================

    /**
     * Float to double.
     *
     * 用 `String.valueOf` 中转，避免 `value.toDouble()` 引入的二进制浮点误差。
     */
    fun float2Double(value: Float): Double = BigDecimal(value.toString()).toDouble()
}