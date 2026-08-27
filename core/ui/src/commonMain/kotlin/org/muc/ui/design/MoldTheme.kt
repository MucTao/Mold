package org.muc.ui.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import org.muc.ui.action.feedback.SnackBar
import org.muc.ui.action.feedback.SnackBarHost
import org.muc.ui.action.feedback.SnackbarData
import org.muc.ui.action.feedback.ToastData
import org.muc.ui.action.feedback.ToastHost
import org.muc.ui.action.feedback.ToastItem
import org.muc.ui.design.adaptive.LocalWindowWidthType
import org.muc.ui.design.adaptive.windowWidthType

private val MoldDeckShapes = Shapes(
    extraSmall = MoldCornerRadius.XSMALL.shape,
    small = MoldCornerRadius.SMALL.shape,
    medium = MoldCornerRadius.MEDIUM.shape,
    large = MoldCornerRadius.LARGE.shape,
    extraLarge = MoldCornerRadius.XLARGE.shape,
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
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MoldTheme(
    seedColor: Color = MoldBlue,
    isDynamic: Boolean? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    toastAlignment: Alignment = BiasAlignment(0f, .7f),
    toastContent: @Composable (ToastData) -> Unit = { ToastItem(it) },
    snackbarAlignment: Alignment = Alignment.BottomEnd,
    snackbarContent: @Composable (SnackbarData) -> Unit = { SnackBar(contentAlignment = snackbarAlignment, bar = it) },
    content: @Composable () -> Unit,
) {
    val dynamicColor = if (isDynamic == true) dynamicColor(isDarkTheme) else null
    val semanticColors = if (isDarkTheme) DarkMoldSemanticColors else LightMoldSemanticColors
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    CompositionLocalProvider(
        LocalMoldSemanticColors provides semanticColors,
        LocalWindowWidthType provides windowAdaptiveInfo.windowSizeClass.windowWidthType(),
    ) {
        MaterialTheme(
            colorScheme = dynamicColor ?: rememberDynamicColorScheme(seedColor = seedColor, isDark = isDarkTheme),
            typography = MoldTypography,
            shapes = MoldDeckShapes,
        ) {
            Surface(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()) {
                    content()
                    ToastHost(contentAlignment = toastAlignment, content = toastContent)
                    SnackBarHost(contentAlignment = snackbarAlignment, content = snackbarContent)
                }
            }
        }
    }
}
