@file:Suppress("unused")

package org.muc.network.serializer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ComposeColor", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(colorToHexStr(value))
    }

    override fun deserialize(decoder: Decoder): Color {
        return hexStrToColor(decoder.decodeString())
    }

    fun hexStrToColor(hex: String): Color {
        return runCatching {
            if (hex.contains("#")) {
                // 处理多种格式：#RRGGBB, #AARRGGBB, 或 RRGGBB
                val cleanHex = hex.removePrefix("#")
                when (cleanHex.length) {
                    3 -> {
                        // 3位颜色格式（如 #F0F）转6位：每个字符重复一次 -> FF00FF
                        val sixDigitHex = buildString {
                            cleanHex.forEach { char ->
                                append(char)
                                append(char)
                            }
                        }
                        // 补全不透明度（FF），转为Long后解析
                        Color(sixDigitHex.toLong(16) or 0xFF000000L)
                    }

                    4 -> {
                        // #ARGB → #AARRGGBB，每一位重复
                        val eightDigitHex = buildString {
                            cleanHex.forEach { char ->
                                append(char)
                                append(char)
                            }
                        }
                        Color(eightDigitHex.toLong(16))
                    }

                    6 -> Color(cleanHex.toLong(16) or 0xFF000000L) // 补全不透明度
                    8 -> Color(cleanHex.toLong(16))               // 带透明度的格式
                    else -> Color.Gray // 容错处理：格式不对时返回默认颜色
                }
            } else {
                Color(hex.toULong())
            }
        }.getOrElse {
            Color.Gray
        }
    }


    fun colorToHexStr(color: Color): String = color.toArgb().toHexColor()

}

fun Int.toHexColor(): String {
    val a = (this shr 24) and 0xFF
    val r = (this shr 16) and 0xFF
    val g = (this shr 8) and 0xFF
    val b = this and 0xFF
    return "#${a.toString(16).padStart(2, '0')}" +
            r.toString(16).padStart(2, '0') +
            g.toString(16).padStart(2, '0') +
            b.toString(16).padStart(2, '0').uppercase()
}