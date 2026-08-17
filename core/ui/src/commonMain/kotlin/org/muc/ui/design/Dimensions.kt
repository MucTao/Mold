package org.muc.ui.design

import androidx.compose.ui.unit.dp

/**
 * ADB Deck 中使用的尺寸和间距。
 *
 * 集中化的常量有助于保持界面的一致性，
 * 并在需要时缩放 UI。
 */
object Dimensions {

    // ── 间距 ────────────────────────────────────────────────
    /** 最小间距 (4 dp)。 */
    val paddingXSmall = 4.dp

    /** 小间距 (8 dp)。 */
    val paddingSmall = 8.dp

    /** 中等间距 (12 dp)。 */
    val paddingMedium = 12.dp

    /** 标准间距 (16 dp)。 */
    val paddingDefault = 16.dp

    /** 大间距 (24 dp)。 */
    val paddingLarge = 24.dp

    /** 超大间距 (32 dp)。 */
    val paddingXLarge = 32.dp

    // ── 元素尺寸 ──────────────────────────────────────
    /** 侧边栏宽度。 */
    val sidebarWidth = 220.dp

    /** 顶部栏高度。 */
    val topBarHeight = 48.dp

    /** 状态栏高度。 */
    val statusBarHeight = 28.dp

    /** 侧边菜单项高度。 */
    val navItemHeight = 44.dp

    /** 卡片圆角半径。 */
    val cardCornerRadius = MucCornerRadius.LARGE.value

    /** 按钮和标签圆角半径。 */
    val buttonCornerRadius = MucCornerRadius.MEDIUM.value

    // ── 图标 ────────────────────────────────────────────────
    /** 紧凑图标（例如表格/列表行中的图标）。 */
    val iconSizeSmall = 16.dp

    /** 导航中的标准图标大小。 */
    val iconSizeNav = 20.dp

    /** 卡片中的标准图标大小。 */
    val iconSizeCard = 24.dp

    /** 大图标（用于空状态）。 */
    val iconSizeLarge = 48.dp
}
