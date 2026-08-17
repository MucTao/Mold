@file:Suppress("unused")
package org.muc.network.serializer

import androidx.compose.runtime.Stable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

val weekFullNames = DayOfWeekNames(listOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"))

val weekShortNames = DayOfWeekNames(listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日"))

object LocalDateStringSerializer : KSerializer<LocalDate> {
     val formatter = LocalDate.Format {
        year(); char('-'); monthNumber(); char('-'); day()
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}

object LocalDateLongSerializer : KSerializer<LocalDate> {
    private val DEFAULT_TIME_ZONE = TimeZone.currentSystemDefault()

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTimestamp", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        // 将 LocalDate 转为该时区当天 0 点的时间戳（毫秒）
        val millis = value.atStartOfDayIn(DEFAULT_TIME_ZONE).toEpochMilliseconds()
        encoder.encodeLong(millis)
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val timestamp = decoder.decodeLong()
        // 兼容秒级和毫秒级
        val epochMillis = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
        return Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(DEFAULT_TIME_ZONE).date
    }
}

@Stable
inline val DayOfWeek.shortName: String
    get() = weekShortNames.names[this.ordinal]