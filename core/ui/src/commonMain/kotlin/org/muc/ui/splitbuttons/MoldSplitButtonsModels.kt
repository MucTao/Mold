package com.adbdeck.core.ui.splitbuttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 拆分按钮的尺寸。
 */
enum class MoldSplitButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val menuPartWidth: Dp,
    val iconSize: Dp,
) {
    /** 大尺寸。 */
    LARGE(
        height = 40.dp,
        horizontalPadding = 14.dp,
        menuPartWidth = 36.dp,
        iconSize = 18.dp,
    ),

    /** 中等尺寸。 */
    MEDIUM(
        height = 34.dp,
        horizontalPadding = 12.dp,
        menuPartWidth = 32.dp,
        iconSize = 16.dp,
    ),

    /** 小尺寸。 */
    SMALL(
        height = 30.dp,
        horizontalPadding = 10.dp,
        menuPartWidth = 28.dp,
        iconSize = 14.dp,
    ),

    /** 超小尺寸。 */
    XSMALL(
        height = 26.dp,
        horizontalPadding = 8.dp,
        menuPartWidth = 24.dp,
        iconSize = 12.dp,
    ),
}

/**
 * 拆分按钮的下拉菜单项。
 *
 * @param value 菜单项的业务值。
 * @param label 菜单项文本。
 * @param enabled 菜单项是否可用。
 */
@Immutable
data class MoldSplitMenuItem<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

/**
 * 拆分按钮的颜色。
 */
@Immutable
data class MoldSplitButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

/**
 * 拆分按钮的默认值集合。
 */
object MoldSplitButtonDefaults {
    /**
     * 拆分按钮的配色方案。
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        disabledContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ): MoldSplitButtonColors {
        return MoldSplitButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            borderColor = borderColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )
    }
}
