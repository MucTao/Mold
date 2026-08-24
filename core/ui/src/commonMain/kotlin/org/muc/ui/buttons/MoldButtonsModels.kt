package org.muc.ui.buttons

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 预配置颜色语义的固定按钮类型。
 */
enum class MoldButtonType {
    /** 中性操作（主要场景）。 */
    NEUTRAL,

    /** 危险操作（删除、重置等）。 */
    DANGER,

    /** 正面操作（确认、成功场景）。 */
    SUCCESS,

    NORMAL,

    PRIMARY,
}

/**
 * 按钮尺寸。
 *
 * 尺寸与 `segmented/split` 组件的高度同步。
 */
enum class MoldButtonSize(
    val height: Dp,
    val minWidth: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val loaderSize: Dp,
    val contentSpacing: Dp,
) {

    XLARGE(
        height = 48.dp,
        minWidth = 100.dp,
        horizontalPadding = 16.dp,
        iconSize = 22.dp,
        loaderSize = 20.dp,
        contentSpacing = 10.dp,
    ),

    /** 大尺寸。 */
    LARGE(
        height = 40.dp,
        minWidth = 80.dp,
        horizontalPadding = 14.dp,
        iconSize = 18.dp,
        loaderSize = 16.dp,
        contentSpacing = 8.dp,
    ),

    /** 中尺寸。 */
    MEDIUM(
        height = 34.dp,
        minWidth = 68.dp,
        horizontalPadding = 12.dp,
        iconSize = 16.dp,
        loaderSize = 14.dp,
        contentSpacing = 6.dp,
    ),

    /** 小尺寸。 */
    SMALL(
        height = 30.dp,
        minWidth = 56.dp,
        horizontalPadding = 10.dp,
        iconSize = 14.dp,
        loaderSize = 12.dp,
        contentSpacing = 6.dp,
    ),

    /** 超小尺寸。 */
    XSMALL(
        height = 26.dp,
        minWidth = 44.dp,
        horizontalPadding = 8.dp,
        iconSize = 12.dp,
        loaderSize = 10.dp,
        contentSpacing = 4.dp,
    ),
}

internal enum class MoldButtonStyle {
    FILLED,
    OUTLINED,
    PLAIN,
}

@Immutable
internal data class MoldButtonResolvedColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val disabledBorderColor: Color,
    val loaderColor: Color,
)
