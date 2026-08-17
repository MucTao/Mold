package org.muc.ui.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ────────────────────────────────────────────────────────────────
// Muc 基础色彩调色板
// ────────────────────────────────────────────────────────────────

/** 主色调 —— 饱和蓝色 */
val MucBlue = Color(0xFF1976D2)

/** 次要颜色 */
val MucTeal = Color(0xFF00897B)

/** 错误颜色 */
val MucRed = Color(0xFFD32F2F)

/** 成功颜色 */
val MucGreen = Color(0xFF388E3C)

/** 禁用颜色 */
val MucGray = Color(0xFFBDBDBD)

/** 警告颜色 */
val MucAmber = Color(0xFFF57C00)

// ────────────────────────────────────────────────────────────────
// 浅色主题
// ────────────────────────────────────────────────────────────────

/** 浅色主题的 ColorScheme。 */
val LightColorScheme = lightColorScheme(
    primary = MucBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),

    secondary = MucTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF004D40),

    error = MucRed,
    onError = Color.White,

    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1C1E),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),

    surfaceVariant = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFF424242),

    outline = MucGray,
)

// ────────────────────────────────────────────────────────────────
// 深色主题
// ────────────────────────────────────────────────────────────────

/** 深色主题的 ColorScheme。 */
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),

    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF004D40),
    secondaryContainer = Color(0xFF00695C),
    onSecondaryContainer = Color(0xFFB2DFDB),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF7F0000),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),

    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),

    outline = Color(0xFF616161),
)
