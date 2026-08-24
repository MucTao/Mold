package org.muc.ui.design

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 界面组件的固定圆角半径集合。
 *
 * 作为圆角半径的统一来源：
 * - 在 Material 主题中 ([MoldTheme])；
 * - 在 `core/ui` 的通用组件中。
 *
 * @param value 圆角半径（dp）。
 */
enum class MoldCornerRadius(val value: Dp, val shape: CornerBasedShape) {
    /** 无圆角。 */
    NONE(0.dp, RoundedCornerShape(0.dp)),
    XSMALL(4.dp, RoundedCornerShape(4.dp)),

    /** 小圆角。 */
    SMALL(6.dp, RoundedCornerShape(6.dp)),

    /** 默认基础圆角。 */
    MEDIUM(10.dp, RoundedCornerShape(10.dp)),

    /** 明显圆角。 */
    LARGE(14.dp, RoundedCornerShape(14.dp)),

    /** 强圆角。 */
    XLARGE(18.dp, RoundedCornerShape(18.dp)),

    /** 圆形（完全圆角）。 */
    CIRCLE(666.dp, CircleShape),
}
