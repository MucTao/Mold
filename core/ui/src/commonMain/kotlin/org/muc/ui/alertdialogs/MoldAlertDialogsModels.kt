package org.muc.ui.alertdialogs

import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldButtonType

/**
 * [MoldAlertDialog] 的操作按钮配置。
 *
 * @param text 按钮文本。
 * @param onClick 点击回调。
 * @param enabled 是否可用。
 * @param loading 是否在按钮内显示加载指示器。
 * @param type 按钮的颜色类型。
 * @param size 按钮尺寸。
 * @param cornerRadius 圆角半径。
 */
data class MoldAlertDialogAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val type: MoldButtonType = MoldButtonType.PRIMARY,
    val size: MoldButtonSize = MoldButtonSize.MEDIUM,
    val cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
)
