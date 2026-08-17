package org.muc.ui.alertdialogs

import org.muc.ui.design.MucCornerRadius
import org.muc.ui.buttons.MucButtonSize
import org.muc.ui.buttons.MucButtonType

/**
 * Конфигурация action-кнопки для [MucAlertDialog].
 *
 * @param text Текст кнопки.
 * @param onClick Callback нажатия.
 * @param enabled Флаг доступности.
 * @param loading Показывать loader внутри кнопки.
 * @param type Цветовой тип кнопки.
 * @param size Размер кнопки.
 * @param cornerRadius Радиус скругления.
 */
data class MucAlertDialogAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val type: MucButtonType = MucButtonType.NEUTRAL,
    val size: MucButtonSize = MucButtonSize.MEDIUM,
    val cornerRadius: MucCornerRadius = MucCornerRadius.MEDIUM,
)
