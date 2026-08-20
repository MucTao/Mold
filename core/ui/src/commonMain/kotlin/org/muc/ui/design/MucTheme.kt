package org.muc.ui.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val MucDeckShapes = Shapes(
    extraSmall = RoundedCornerShape(MucCornerRadius.SMALL.value),
    small = RoundedCornerShape(MucCornerRadius.MEDIUM.value),
    medium = RoundedCornerShape(MucCornerRadius.LARGE.value),
    large = RoundedCornerShape(MucCornerRadius.XLARGE.value),
    extraLarge = RoundedCornerShape(MucCornerRadius.XLARGE.value),
)

@Composable
expect fun dynamicColor(darkTheme: Boolean): ColorScheme?

/**
 * ADB Deck 应用的根主题。
 *
 * 使用自定义颜色、排版和圆角包装 [MaterialTheme]。
 * 支持浅色和深色主题；切换通过参数 [isDarkTheme] 进行，
 *
 * @param isDarkTheme 如果为 `true` — 应用深色主题，否则应用浅色主题。
 *                    默认读取系统设置。
 * @param content     应用主题的内容。
 */
@Composable
fun MucTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val dynamicColor = dynamicColor(isDarkTheme)
    val semanticColors = if (isDarkTheme) DarkMucSemanticColors else LightMucSemanticColors
    CompositionLocalProvider(
        LocalMucSemanticColors provides semanticColors,
    ) {
        MaterialTheme(
            colorScheme = dynamicColor ?: (if (isDarkTheme) DarkColorScheme else LightColorScheme),
            typography = MucTypography,
            shapes = MucDeckShapes,
            content = content,
        )
    }
}
