@file:Suppress("unused")
package org.muc.network.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntAsBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntAsBoolean", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() != 0
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if (value) 1 else 0)
    }
}

object StringAsBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringAsBoolean", PrimitiveKind.STRING)

    // 可以通过配置改变序列化行为
    var trueString: String = "Y"
    var falseString: String = "N"

    override fun deserialize(decoder: Decoder): Boolean {
        val value = decoder.decodeString()
        return value.equals(trueString, ignoreCase = true)
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeString(if (value) trueString else falseString)
    }
}