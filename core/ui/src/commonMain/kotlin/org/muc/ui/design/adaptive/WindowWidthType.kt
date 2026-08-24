package org.muc.ui.design.adaptive

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

enum class WindowWidthType(val col: Int) {
    COMPACT(1),// 紧凑型布局（适用于手机竖屏）：单列垂直布局
    MEDIUM(2), // 中型布局（适用于折叠屏、小平板）：双列水平布局
    EXPANDED(3) // 扩展型布局（适用于大平板、桌面模式）：三列水平布局
}

val LocalWindowWidthType = staticCompositionLocalOf {
    WindowWidthType.COMPACT
}

internal fun WindowSizeClass.windowWidthType(): WindowWidthType = when {
    isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowWidthType.EXPANDED
    isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowWidthType.MEDIUM
    else -> WindowWidthType.COMPACT
}
