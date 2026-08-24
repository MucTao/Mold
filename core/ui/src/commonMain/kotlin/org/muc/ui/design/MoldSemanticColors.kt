package org.muc.ui.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 应用的语义色。
 *
 * 这些令牌描述的是含义，而非具体的 UI 元素：
 * - [info] — 信息状态；
 * - [success] — 成功状态；
 * - [warning] — 警告。
 *
 * 在 feature 模块中使用语义色可以：
 * - 消除屏幕中硬编码的 `Color(0x...)`；
 * - 根据 light/dark 主题集中调整调色板。
 */
@Immutable
data class MoldSemanticColors(
    val info: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
)

/** 浅色主题的语义色。 */
internal val LightMoldSemanticColors = MoldSemanticColors(
    info = MoldBlue,
    success = MoldGreen,
    warning = MoldAmber,
    error = MoldRed,
)

/** 深色主题的语义色。 */
internal val DarkMoldSemanticColors = MoldSemanticColors(
    info = Color(0xFF90CAF9),
    success = Color(0xFF81C784),
    warning = Color(0xFFFFB74D),
    error = MoldRed,
)

internal val LocalMoldSemanticColors = staticCompositionLocalOf { LightMoldSemanticColors }

/**
 * 设计系统令牌的访问入口。
 */
object MoldTheme {
    /**
     * 当前主题的 Material3 colorScheme。
     *
     * 作为 feature 模块中系统颜色角色的统一访问点。
     */
    val colorScheme: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    /** [MoldTheme] 中当前的语义色。 */
    val semanticColors: MoldSemanticColors
        @Composable
        get() = LocalMoldSemanticColors.current
}
