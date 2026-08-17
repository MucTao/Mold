@file:Suppress("unused")
package org.muc.network.serializer

import kotlinx.datetime.*
import kotlinx.datetime.format.char
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Clock
import kotlin.time.Instant

object LocalDateTimeStringSerializer : KSerializer<LocalDateTime> {
    val format = LocalDateTime.Format {
        year(); char('-'); monthNumber(); char('-'); day(); char(' ')
        hour(); char(':'); minute(); char(':'); second()
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTimeString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(format))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), format)
    }
}

object LocalDateTimeLongSerializer : KSerializer<LocalDateTime> {
    private val DEFAULT_TIME_ZONE = TimeZone.currentSystemDefault()

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTimestamp", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val millis = value.toInstant(DEFAULT_TIME_ZONE).toEpochMilliseconds()
        encoder.encodeLong(millis)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val timestamp = decoder.decodeLong()
        // 兼容秒级和毫秒级
        val epochMillis = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
        return Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(DEFAULT_TIME_ZONE)
    }
}

fun LocalDateTime.Companion.now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
fun LocalDate.Companion.now() = LocalDateTime.now().date