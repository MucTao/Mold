package org.muc.ui.textfields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import org.muc.ui.design.MucCornerRadius

/**
 * Выпадающее поле ввода со стилем `filled`.
 *
 * API выровнен с [MucOutlinedDropdownTextField] и [MucPlainDropdownTextField]
 * для взаимозаменяемости по месту использования.
 *
 * @param options Опции выпадающего списка.
 * @param selectedValue Текущее выбранное бизнес-значение.
 * @param onValueSelected Callback выбора опции.
 * @param modifier Modifier контейнера.
 * @param placeholder Placeholder для пустого значения.
 * @param type Цветовой тип поля (`NEUTRAL/DANGER/SUCCESS`).
 * @param size Размер поля.
 * @param cornerRadius Радиус скругления.
 * @param enabled Доступность поля.
 * @param leadingIcon Иконка слева.
 * @param supportingText Текст под полем (подсказка/ошибка).
 * @param showSelectedCheckmark Показывать отметку у выбранной опции.
 */
@Composable
fun <T> MucFilledDropdownTextField(
    options: List<MucDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MucTextFieldType = MucTextFieldType.NEUTRAL,
    size: MucTextFieldSize = MucTextFieldSize.MEDIUM,
    cornerRadius: MucCornerRadius = MucCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
) {
    MucBaseDropdownTextField(
        style = MucTextFieldStyle.FILLED,
        options = options,
        selectedValue = selectedValue,
        onValueSelected = onValueSelected,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        leadingIcon = leadingIcon,
        supportingText = supportingText,
        showSelectedCheckmark = showSelectedCheckmark,
    )
}

/**
 * Выпадающее поле ввода со стилем `outlined`.
 *
 * API выровнен с [MucFilledDropdownTextField] и [MucPlainDropdownTextField].
 */
@Composable
fun <T> MucOutlinedDropdownTextField(
    options: List<MucDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MucTextFieldType = MucTextFieldType.NEUTRAL,
    size: MucTextFieldSize = MucTextFieldSize.MEDIUM,
    cornerRadius: MucCornerRadius = MucCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
) {
    MucBaseDropdownTextField(
        style = MucTextFieldStyle.OUTLINED,
        options = options,
        selectedValue = selectedValue,
        onValueSelected = onValueSelected,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        leadingIcon = leadingIcon,
        supportingText = supportingText,
        showSelectedCheckmark = showSelectedCheckmark,
    )
}

/**
 * Выпадающее поле ввода со стилем `plain`.
 *
 * API выровнен с [MucFilledDropdownTextField] и [MucOutlinedDropdownTextField].
 */
@Composable
fun <T> MucPlainDropdownTextField(
    options: List<MucDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MucTextFieldType = MucTextFieldType.NEUTRAL,
    size: MucTextFieldSize = MucTextFieldSize.MEDIUM,
    cornerRadius: MucCornerRadius = MucCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
) {
    MucBaseDropdownTextField(
        style = MucTextFieldStyle.PLAIN,
        options = options,
        selectedValue = selectedValue,
        onValueSelected = onValueSelected,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        leadingIcon = leadingIcon,
        supportingText = supportingText,
        showSelectedCheckmark = showSelectedCheckmark,
    )
}

@Composable
private fun <T> MucBaseDropdownTextField(
    style: MucTextFieldStyle,
    options: List<MucDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier,
    placeholder: String?,
    type: MucTextFieldType,
    size: MucTextFieldSize,
    cornerRadius: MucCornerRadius,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    supportingText: String?,
    showSelectedCheckmark: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableIntStateOf(0) }

    val selectedLabel = remember(options, selectedValue) {
        options.firstOrNull { it.value == selectedValue }?.label.orEmpty()
    }

    val trailingIcon = if (expanded) {
        Icons.Outlined.KeyboardArrowUp
    } else {
        Icons.Outlined.KeyboardArrowDown
    }

    val fieldModifier = Modifier
        .onGloballyPositioned { coordinates ->
            anchorWidthPx = coordinates.size.width
        }

    Box(modifier = modifier) {
        when (style) {
            MucTextFieldStyle.FILLED -> MucFilledTextField(
                value = selectedLabel,
                onValueChange = {},
                modifier = fieldModifier,
                placeholder = placeholder,
                type = type,
                size = size,
                cornerRadius = cornerRadius,
                enabled = enabled,
                readOnly = true,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                onTrailingIconClick = {
                    if (enabled) expanded = !expanded
                },
                supportingText = supportingText,
            )

            MucTextFieldStyle.OUTLINED -> MucOutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                modifier = fieldModifier,
                placeholder = placeholder,
                type = type,
                size = size,
                cornerRadius = cornerRadius,
                enabled = enabled,
                readOnly = true,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                onTrailingIconClick = {
                    if (enabled) expanded = !expanded
                },
                supportingText = supportingText,
            )

            MucTextFieldStyle.PLAIN -> MucPlainTextField(
                value = selectedLabel,
                onValueChange = {},
                modifier = fieldModifier,
                placeholder = placeholder,
                type = type,
                size = size,
                cornerRadius = cornerRadius,
                enabled = enabled,
                readOnly = true,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                onTrailingIconClick = {
                    if (enabled) expanded = !expanded
                },
                supportingText = supportingText,
            )
        }

        // Открываем меню кликом по всей области поля, а не только по стрелке.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(size.height)
                .clip(RoundedCornerShape(cornerRadius.value))
                .clickable(enabled = enabled) {
                    expanded = !expanded
                },
        )

        val menuWidthDp = with(LocalDensity.current) { anchorWidthPx.toDp() }

        DropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(menuWidthDp),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    enabled = option.enabled,
                    text = {
                        Text(
                            text = option.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = if (showSelectedCheckmark && option.value == selectedValue) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onValueSelected(option.value)
                    },
                )
            }
        }
    }
}
