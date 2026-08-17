package org.muc.ui.colorpicker

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min

fun Color.toHsv(): FloatArray {
    val r = this.red
    val g = this.green
    val b = this.blue

    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val delta = max - min

    var h = 0f
    val s = if (max == 0f) 0f else delta / max
    val v = max

    if (delta != 0f) {
        h = when (max) {
            r -> ((g - b) / delta) % 6f
            g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        }
        h *= 60f
        if (h < 0) h += 360f
    }

    return floatArrayOf(h, s, v)
}

val Color.hue: Float get() = toHsv()[0]
val Color.saturation: Float get() = toHsv()[1]

fun Int.toHexColor(supportAlpha: Boolean = true): String {
    fun Int.toHex(): String {
        val hex = toString(16).uppercase()
        return if (hex.length == 1) "0$hex" else hex
    }

    val r = (this shr 16) and 0xFF
    val g = (this shr 8) and 0xFF
    val b = this and 0xFF
    return if (supportAlpha) {
        val a = (this shr 24) and 0xFF
        "#${a.toHex()}${r.toHex()}${g.toHex()}${b.toHex()}"
    } else
        "#${r.toHex()}${g.toHex()}${b.toHex()}"
}