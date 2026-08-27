package org.muc.ui.segmentedbuttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 分段按钮的尺寸。
 *
 * 尺寸与 [com.adbdeck.core.ui.splitbuttons.MoldSplitButtonSize] 保持同步，
 * 以确保控件外观一致。
 */
enum class MoldSegmentedButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val minWidth: Dp,
    val indicatorSize: Dp,
) {
    /** 大尺寸。 */
    LARGE(
        height = 40.dp,
        horizontalPadding = 14.dp,
        minWidth = 80.dp,
        indicatorSize = 8.dp,
    ),

    /** 中等尺寸。 */
    MEDIUM(
        height = 34.dp,
        horizontalPadding = 12.dp,
        minWidth = 68.dp,
        indicatorSize = 7.dp,
    ),

    /** 小尺寸。 */
    SMALL(
        height = 30.dp,
        horizontalPadding = 10.dp,
        minWidth = 56.dp,
        indicatorSize = 6.dp,
    ),

    /** 超小尺寸。 */
    XSMALL(
        height = 26.dp,
        horizontalPadding = 8.dp,
        minWidth = 44.dp,
        indicatorSize = 5.dp,
    ),
}

/**
 * 分段控件的选项。
 *
 * @param value 选项的业务值。
 * @param label 分段文本。
 * @param leadingIcon 分段图标（可选）。
 * @param enabled 选项是否可用。
 * @param contentColor 指定选项的可选内容颜色（图标/文本）。
 * @param indicatorColor 标签左侧圆点的可选颜色。
 * @param contentDescription 无障碍说明文本。为 `null` 时使用 [label]。
 */
@Immutable
data class MoldSegmentedOption<T>(
    val value: T,
    val label: String,
    val leadingIcon: ImageVector? = null,
    val enabled: Boolean = true,
    val contentColor: Color? = null,
    val indicatorColor: Color? = null,
    val contentDescription: String? = null,
)

/**
 * 分段控件的颜色。
 */
@Immutable
data class MoldSegmentedButtonColors(
    val activeContainerColor: Color,
    val activeContentColor: Color,
    val inactiveContainerColor: Color,
    val inactiveContentColor: Color,
    val borderColor: Color,
    val disabledContentColor: Color,
)

/**
 * 分段控件的默认值。
 */
object MoldSegmentedButtonDefaults {
    /**
     * 分段控件的配色方案。
     */
    @Composable
    fun colors(
        activeContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        activeContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        inactiveContainerColor: Color = MaterialTheme.colorScheme.surface,
        inactiveContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): MoldSegmentedButtonColors {
        return MoldSegmentedButtonColors(
            activeContainerColor = activeContainerColor,
            activeContentColor = activeContentColor,
            inactiveContainerColor = inactiveContainerColor,
            inactiveContentColor = inactiveContentColor,
            borderColor = borderColor,
            disabledContentColor = disabledContentColor,
        )
    }
}
