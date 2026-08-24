package org.muc.ui.alertdialogs

import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldButtonType

/**
 * Конфигурация action-кнопки для [MoldAlertDialog].
 *
 * @param text Текст кнопки.
 * @param onClick Callback нажатия.
 * @param enabled Флаг доступности.
 * @param loading Показывать loader внутри кнопки.
 * @param type Цветовой тип кнопки.
 * @param size Размер кнопки.
 * @param cornerRadius Радиус скругления.
 */
data class MoldAlertDialogAction(
    val text: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val type: MoldButtonType = MoldButtonType.NEUTRAL,
    val size: MoldButtonSize = MoldButtonSize.MEDIUM,
    val cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
)
