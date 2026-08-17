package org.muc.ui.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

data class ColorPickerState(
    val hue: Float = 180f,          // 0° - 360°
    val saturation: Float = 0.5f,   // 0.0 - 1.0
    val value: Float = 0.5f,        // 0.0 - 1.0
    val alpha: Float = 1.0f,         // 0.0 - 1.0
    val supportAlpha: Boolean = false //
) {
    fun toColor(): Color = Color.hsv(hue, saturation, value, if (supportAlpha) alpha else 1.0f)

    fun toHexStr() = toColor().toArgb().toHexColor(supportAlpha)
}

