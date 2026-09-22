@file:Suppress("unused")

package org.muc.mold.utils.util

import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char
import kotlinx.datetime.number
import kotlinx.datetime.periodUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 时间工具类 —— 提供日期时间格式化、时间差计算、星座/生肖/节日查询等能力。
 *
 * 全部基于 [kotlinx.datetime] 实现，彻底移除 `java.util.Date` / `java.util.Calendar`
 * / `java.text.SimpleDateFormat` 等遗留 API。
 * 格式化全部使用 **builder DSL**，不使用 `byUnicodePattern` 格式字符串。
 * 可能失败的操作直接返回 [Result<T>]（不再使用 [runCatching]）。
 *
 * ## 依赖
 * ```kotlin
 * implementation("org.jetbrains.kotlinx:kotlinx-datetime:last")
 * ```
 *
 * 原始作者：Muc
 * 优化：java.time → kotlinx.datetime、Thread → 协程、runCatching → Result、格式字符串 → Builder DSL
 */

internal expect fun getUsingNetworkTime(): Boolean
internal expect fun setUsingNetworkTime(use: Boolean)

object TimeUtils {
    /**
     * 检查设备是否正在使用网络提供的时间。
     * 在您想验证设备是否设置了正确的时间以避免欺诈的情况下，
     * 或者如果您想防止用户篡改时间并滥用您的“一次性”和“到期”功能，则非常有用。
     * @return {@code true}: yes<br>{@code false}: no
     */
    var isUsingNetworkProvidedTime
        get() = getUsingNetworkTime()
        set(value) {
            setUsingNetworkTime(value)
        }

    // ──────────────────────────────────────────────
    // 常量
    // ──────────────────────────────────────────────

    /** 秒 / 毫秒 换算常量 */
    private const val MILLIS_PER_SECOND = 1000L

    /** 默认时区 */
    private val defaultTimeZone: TimeZone get() = TimeZone.currentSystemDefault()

    // ──────────────────────────────────────────────
    // 预定义格式 —— 全部 Builder DSL，无格式字符串
    // ──────────────────────────────────────────────

    /** `yyyy-MM-dd HH:mm:ss` */
    private val dateTimeFormat = LocalDateTime.Format {
        year(); char('-')
        monthNumber(); char('-')
        day(); char(' ')
        hour(); char(':')
        minute(); char(':')
        second()
    }

    /** `yyyy-MM-dd` */
    private val dateFormat = LocalDate.Format {
        year(); char('-')
        monthNumber(); char('-')
        day()
    }

    /** `HH:mm:ss` */
    private val timeFormat = LocalTime.Format {
        hour(); char(':')
        minute(); char(':')
        second()
    }

    /** `yyyy年MM月dd日 HH:mm:ss` */
    private val dateTimeChineseFormat = LocalDateTime.Format {
        year(); char('年')
        monthNumber(); char('月')
        day(); char('日')
        char(' ')
        hour(); char(':')
        minute(); char(':')
        second()
    }

    /** `yyyy年MM月dd日` */
    private val dateChineseFormat = LocalDate.Format {
        year(); char('年')
        monthNumber(); char('月')
        day(); char('日')
    }

    /** `MM月dd日 HH:mm:ss` */
    private val dateTimeNoYearFormat = LocalDateTime.Format {
        monthNumber(); char('月')
        day(); char('日')
        char(' ')
        hour(); char(':')
        minute(); char(':')
        second()
    }

    /** `MM月dd日` */
    private val dateNoYearFormat = LocalDate.Format {
        monthNumber(); char('月')
        day(); char('日')
    }

    // ──────────────────────────────────────────────
    // 公开 API：当前时间获取
    // ──────────────────────────────────────────────

    /**
     * 获取当前日期时间字符串（`yyyy-MM-dd HH:mm:ss`）。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun getNowDateTimeString(timeZone: TimeZone = defaultTimeZone, format: DateTimeFormat<LocalDateTime> = dateTimeFormat): String {
        return getNowDateTime(timeZone).format(format)
    }

    /**
     * 获取当前日期字符串（`yyyy-MM-dd`）。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun getNowDateString(timeZone: TimeZone = defaultTimeZone, format: DateTimeFormat<LocalDate> = dateFormat): String {
        return getNowDate(timeZone).format(format)
    }

    /**
     * 获取当前时间字符串（`HH:mm:ss`）。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun getNowTimeString(timeZone: TimeZone = defaultTimeZone, format: DateTimeFormat<LocalTime> = timeFormat): String {
        return getNowTime(timeZone).format(format)
    }


    /**
     * 获取当前时间的 [LocalDateTime]。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun getNowDateTime(timeZone: TimeZone = defaultTimeZone): LocalDateTime {
        return Clock.System.now().toLocalDateTime(timeZone)
    }

    /**
     * 获取当前日期 [LocalDate]。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun getNowDate(timeZone: TimeZone = defaultTimeZone): LocalDate {
        return Clock.System.todayIn(timeZone)
    }

    /**
     * 获取当前时间 [LocalTime]。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun getNowTime(timeZone: TimeZone = defaultTimeZone): LocalTime {
        return Clock.System.now().toLocalDateTime(timeZone).time
    }

    /**
     * 获取当前毫秒时间戳（epoch millis）。
     */
    fun getNowMills(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * 获取当前秒时间戳（epoch seconds）。
     */
    fun getNowSeconds(): Long = Clock.System.now().epochSeconds

    // ──────────────────────────────────────────────
    // 公开 API：格式化（时间戳 / 日期 → 字符串）
    // ──────────────────────────────────────────────
    /**
     * 将毫秒时间戳格式化为日期时间。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun Long.asDateTime(timeZone: TimeZone = defaultTimeZone) =
        Instant.fromEpochMilliseconds(this)
            .toLocalDateTime(timeZone)

    /**
     * 将毫秒时间戳格式化为日期时间字符串（`yyyy-MM-dd HH:mm:ss`）。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun Long.asDateTimeString(
        timeZone: TimeZone = defaultTimeZone,
        format: DateTimeFormat<LocalDateTime> = dateTimeFormat
    ): String = asDateTime(timeZone).format(format)

    /**
     * 将毫秒时间戳格式化为日期
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun Long.asDate(timeZone: TimeZone = defaultTimeZone) = asDateTime(timeZone).date

    /**
     * 将毫秒时间戳格式化为日期字符串（`yyyy-MM-dd`）。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun Long.asDateString(
        timeZone: TimeZone = defaultTimeZone,
        format: DateTimeFormat<LocalDate> = dateFormat
    ) = asDate(timeZone).format(format)

    /**
     * 将毫秒时间戳格式化为时间
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun Long.asTime(timeZone: TimeZone = defaultTimeZone) = asDateTime(timeZone).time

    /**
     * 将毫秒时间戳格式化为时间字符串（`HH:mm:ss`）。
     *
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     */
    fun Long.asTimeString(
        timeZone: TimeZone = defaultTimeZone,
        format: DateTimeFormat<LocalTime> = timeFormat
    ) = asTime(timeZone).format(format)


    // ──────────────────────────────────────────────
    // 公开 API：解析（字符串 → 日期 / 时间戳）
    // ──────────────────────────────────────────────

    /**
     * 将日期字符串解析为 [LocalDate]。
     * 日期字符串，格式 `yyyy-MM-dd`
     * @return [Result.success] 包含 [LocalDate]；[Result.failure] 包含解析异常
     */
    fun String.asTime(format: DateTimeFormat<LocalTime> = timeFormat) = runCatching {
        LocalTime.parse(this, format)
    }

    /**
     * 将日期字符串解析为 [LocalDate]。
     * 日期字符串，格式 `yyyy-MM-dd`
     * @return [Result.success] 包含 [LocalDate]；[Result.failure] 包含解析异常
     */
    fun String.asDate(format: DateTimeFormat<LocalDate> = dateFormat) = runCatching {
        LocalDate.parse(this, format)
    }

    /**
     * 将日期时间字符串解析为 [LocalDateTime]。
     * 日期时间字符串，格式 `yyyy-MM-dd HH:mm:ss`
     * @return [Result.success] 包含 [LocalDateTime]；[Result.failure] 包含解析异常
     */
    fun String.asDateTime(format: DateTimeFormat<LocalDateTime> = dateTimeFormat) = runCatching {
        LocalDateTime.parse(this, format)
    }

    /**
     * 将时间字符串解析为毫秒时间戳（以日期[date] + 该时间）。
     * 时间字符串，格式 `HH:mm:ss`
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     * @return [Result.success] 包含毫秒时间戳；[Result.failure] 包含解析异常
     */
    fun String.asTimeMillis(
        timeZone: TimeZone = defaultTimeZone,
        date: LocalDate = getNowDate(timeZone),
        format: DateTimeFormat<LocalTime> = timeFormat
    ) = runCatching {
        asTime(format).getOrThrow().atDate(date).toInstant(timeZone).toEpochMilliseconds()
    }

    /**
     * 将日期字符串解析为毫秒时间戳（以该日期 + 时间[time]）。
     * 日期字符串，格式 `yyyy-MM-dd`
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     * @return [Result.success] 包含毫秒时间戳；[Result.failure] 包含解析异常
     */
    fun String.asDateMillis(
        timeZone: TimeZone = defaultTimeZone,
        time: LocalTime = getNowTime(timeZone),
        format: DateTimeFormat<LocalDate> = dateFormat,
    ) = runCatching {
        asDate(format).getOrThrow().atTime(time).toInstant(timeZone).toEpochMilliseconds()
    }

    /**
     * 将日期时间字符串解析为毫秒时间戳。
     * 日期时间字符串，格式 `yyyy-MM-dd HH:mm:ss`
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     * @return [Result.success] 包含毫秒时间戳；[Result.failure] 包含解析异常
     */
    fun String.asDateTimeMillis(
        timeZone: TimeZone = defaultTimeZone,
        format: DateTimeFormat<LocalDateTime> = dateTimeFormat
    ): Result<Long> = runCatching {
        asDateTime(format).getOrThrow().toInstant(timeZone).toEpochMilliseconds()
    }

    // ──────────────────────────────────────────────
    // 公开 API：时间差计算
    // ──────────────────────────────────────────────

    /**
     * 按指定时区计算从 [dateTime1] 和 [dateTime2] 的日历时间差，保留方向。
     * 年、月、日按日历计算，不将一个月固定换算为 30 天。
     */
    fun getDateTimePeriod(
        dateTime1: LocalDateTime,
        dateTime2: LocalDateTime,
        timeZone: TimeZone = defaultTimeZone
    ): DateTimePeriod =
        if (dateTime1 <= dateTime2)
            dateTime1.toInstant(timeZone).periodUntil(dateTime2.toInstant(timeZone), timeZone)
        else
            dateTime2.toInstant(timeZone).periodUntil(dateTime1.toInstant(timeZone), timeZone)

    fun LocalDateTime.periodUntil(other: LocalDateTime, timeZone: TimeZone = defaultTimeZone): DateTimePeriod = getDateTimePeriod(this, other, timeZone)


    /**
     * 将时间差转换为中文，例如“1年2个月3天4小时5分钟6秒”。
     * 跳过零值，零时间差返回“0秒”；负值保留负号，混合正负值逐项显示。
     * 秒的小数部分最多保留 9 位，避免丢失不足一秒的时间差。
     */
    fun DateTimePeriod.toChinese(showZero: Boolean = false): String = buildString {
        fun appendUnit(value: Int, unit: String) {
            if (value != 0 || showZero) append(value).append(unit)
        }
        appendUnit(years, "年")
        appendUnit(months, "个月")
        appendUnit(days, "天")
        appendUnit(hours, "小时")
        appendUnit(minutes, "分钟")
        val secondsInNanos = seconds.toLong() * 1_000_000_000L + nanoseconds
        if (secondsInNanos != 0L) {
            if (secondsInNanos < 0) append('-')
            val magnitude = abs(secondsInNanos)
            append(magnitude / 1_000_000_000L)
            val fraction = magnitude % 1_000_000_000L
            if (fraction != 0L) {
                append('.').append(fraction.toString().padStart(9, '0').trimEnd('0'))
            }
            append("秒")
        }
        if (isEmpty()) append("0秒")
    }

    /**
     * 计算两个日期时间字符串之间的时间差，返回友好描述。
     *
     * @param dateTimeStr1    日期时间字符串（`yyyy-MM-dd HH:mm:ss`）
     * @param dateTimeStr2    日期时间字符串（`yyyy-MM-dd HH:mm:ss`）
     * @param timeZone 时区，默认 [TimeZone.currentSystemDefault]
     * @return [Result.success] 包含友好描述；[Result.failure] 包含解析异常
     */
    fun getFitTimeSpan(
        dateTimeStr1: String,
        dateTimeStr2: String,
        timeZone: TimeZone = defaultTimeZone,
        format: DateTimeFormat<LocalDateTime> = dateTimeFormat
    ): Result<String> = runCatching {
        val dateTime2 = dateTimeStr2.asDateTime(format).getOrThrow()
        dateTimeStr1.asDateTime(format).getOrThrow().periodUntil(dateTime2, timeZone).toChinese()
    }

    /**
     * 计算两个毫秒时间戳之间的时间差，返回友好描述。
     *
     * @param millis1 第一个时间戳
     * @param millis2 第二个时间戳
     */
    fun getFitTimeSpan(millis1: Long, millis2: Long, timeZone: TimeZone = defaultTimeZone): String {
        val dateTime1 = millis1.asDateTime(timeZone)
        val dateTime2 = millis2.asDateTime(timeZone)
        return getDateTimePeriod(dateTime1, dateTime2, timeZone).toChinese()
    }

    /**
     * 计算当前时间与给定毫秒时间戳的时间差，返回友好描述。
     * @return 如 "5分钟前" / "3天后"
     */
    fun LocalDateTime.getFitTimeSpanByNow(timeZone: TimeZone = defaultTimeZone): String {
        val now = getNowDateTime(timeZone)
        return if (now < this) this.periodUntil(now, timeZone).toChinese() + "后"
        else this.periodUntil(now, timeZone).toChinese() + "前"
    }

    // ──────────────────────────────────────────────
    // 公开 API：星期 / 月份 / 日期字段
    // ──────────────────────────────────────────────

    /**
     * 获取中文星期名称。
     *
     * @param date 日期，默认今天
     * @return 如 "星期一"
     */
    fun getChineseWeek(date: LocalDate = getNowDate()): String {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "星期一"
            DayOfWeek.TUESDAY -> "星期二"
            DayOfWeek.WEDNESDAY -> "星期三"
            DayOfWeek.THURSDAY -> "星期四"
            DayOfWeek.FRIDAY -> "星期五"
            DayOfWeek.SATURDAY -> "星期六"
            DayOfWeek.SUNDAY -> "星期日"
        }
    }

    /**
     * 获取英文星期名称。
     *
     * @param date 日期，默认今天
     * @return 如 "Monday"
     */
    fun getEnglishWeek(date: LocalDate = getNowDate()): String {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
        }
    }

    /**
     * 获取中文月份名称。
     *
     * @param date 日期，默认今天
     * @return 如 "一月"
     */
    fun getChineseMonth(date: LocalDate = getNowDate()): String {
        return when (date.month) {
            Month.JANUARY -> "一月"
            Month.FEBRUARY -> "二月"
            Month.MARCH -> "三月"
            Month.APRIL -> "四月"
            Month.MAY -> "五月"
            Month.JUNE -> "六月"
            Month.JULY -> "七月"
            Month.AUGUST -> "八月"
            Month.SEPTEMBER -> "九月"
            Month.OCTOBER -> "十月"
            Month.NOVEMBER -> "十一月"
            Month.DECEMBER -> "十二月"
        }
    }

    /**
     * 获取英文月份名称。
     *
     * @param date 日期，默认今天
     * @return 如 "January"
     */
    fun getEnglishMonth(date: LocalDate = getNowDate()): String {
        return when (date.month) {
            Month.JANUARY -> "January"
            Month.FEBRUARY -> "February"
            Month.MARCH -> "March"
            Month.APRIL -> "April"
            Month.MAY -> "May"
            Month.JUNE -> "June"
            Month.JULY -> "July"
            Month.AUGUST -> "August"
            Month.SEPTEMBER -> "September"
            Month.OCTOBER -> "October"
            Month.NOVEMBER -> "November"
            Month.DECEMBER -> "December"
        }
    }


    // ──────────────────────────────────────────────
    // 公开 API：友好好记时间
    // ──────────────────────────────────────────────

    /**
     * 将毫秒时间戳转为友好描述（如 "刚刚"、"5分钟前"）。
     */
    fun LocalDateTime.getFriendlyTimeSpanByNow(timeZone: TimeZone = defaultTimeZone): String {
        val dateTime = this.toInstant(timeZone).toEpochMilliseconds()
        val span = getNowMills() - dateTime
        return when {
            span < 0 -> "刚刚"
            span < MILLIS_PER_SECOND -> "刚刚"
            span < 60 * MILLIS_PER_SECOND -> "${span / MILLIS_PER_SECOND}秒前"
            span < 3600 * MILLIS_PER_SECOND -> "${span / (60 * MILLIS_PER_SECOND)}分钟前"
            span < 86400 * MILLIS_PER_SECOND -> "${span / (3600 * MILLIS_PER_SECOND)}小时前"
            span < 7L * 86400 * MILLIS_PER_SECOND -> "${span / (86400 * MILLIS_PER_SECOND)}天前"
            else -> this.format(dateTimeFormat)
        }
    }


    // ──────────────────────────────────────────────
    // 公开 API：判断操作
    // ──────────────────────────────────────────────

    /**
     * 判断是否是闰年。
     *
     * @param date 日期，默认今天
     */
    fun isLeapYear(date: LocalDate = getNowDate()): Boolean {
        val year = date.year
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    }

    // ──────────────────────────────────────────────
    // 公开 API：星座 / 生肖 / 节日
    // ──────────────────────────────────────────────

    /**
     * 根据日期获取星座。
     *
     * @param date 日期，默认今天
     * @return 星座名称
     */
    fun getConstellation(date: LocalDate = getNowDate()): String {
        val month = date.month.number
        val day = date.day
        return when {
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> "水瓶座"
            (month == 2) || (month == 3 && day <= 20) -> "双鱼座"
            (month == 3) || (month == 4 && day <= 19) -> "白羊座"
            (month == 4) || (month == 5 && day <= 20) -> "金牛座"
            (month == 5) || (month == 6 && day <= 21) -> "双子座"
            (month == 6) || (month == 7 && day <= 22) -> "巨蟹座"
            (month == 7) || (month == 8 && day <= 22) -> "狮子座"
            (month == 8) || (month == 9 && day <= 22) -> "处女座"
            (month == 9) || (month == 10 && day <= 23) -> "天秤座"
            (month == 10) || (month == 11 && day <= 22) -> "天蝎座"
            (month == 11) || (month == 12 && day <= 21) -> "射手座"
            else -> "摩羯座"
        }
    }

    /**
     * 根据日期获取生肖（精确到年）。
     *
     * @param date 日期，默认今天
     * @return 生肖名称
     */
    fun getChineseZodiac(date: LocalDate = getNowDate()): String {
        return when (date.year % 12) {
            0 -> "猴"
            1 -> "鸡"
            2 -> "狗"
            3 -> "猪"
            4 -> "鼠"
            5 -> "牛"
            6 -> "虎"
            7 -> "兔"
            8 -> "龙"
            9 -> "蛇"
            10 -> "马"
            11 -> "羊"
            else -> ""
        }
    }

    /**
     * 根据日期获取阳历节日（简化版，仅覆盖主要节日）。
     * 农历节日需要调用api查询
     * @return 节日名称，非节日返回空字符串
     */
    fun LocalDate.getChineseFestival(): String {
        val month = month.number
        val day = day
        return when (month) {
            1 if day == 1 -> "元旦"
            2 if day == 14 -> "情人节"
            3 if day == 8 -> "妇女节"
            4 if day == 1 -> "愚人节"
            5 if day == 1 -> "劳动节"
            6 if day == 1 -> "儿童节"
            7 if day == 1 -> "建党节"
            8 if day == 1 -> "建军节"
            9 if day == 10 -> "教师节"
            10 if day == 1 -> "国庆节"
            12 if day == 25 -> "圣诞节"
            else -> ""
        }
    }
}
