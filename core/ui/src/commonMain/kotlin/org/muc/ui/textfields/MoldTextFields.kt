package org.muc.ui.textfields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.muc.ui.design.MoldBlue
import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.design.MoldGreen
import org.muc.ui.design.MoldRed

/**
 * Универсальное текстовое поле с заливкой.
 *
 * Имеет единый API с [MoldOutlinedTextField] и [MoldPlainTextField], чтобы стиль
 * можно было менять без изменения остальной логики.
 *
 * @param value Текущее текстовое значение.
 * @param onValueChange Callback изменения текста.
 * @param modifier Modifier контейнера.
 * @param placeholder Текст placeholder. Показывается, когда [value] пустой.
 * @param type Цветовой тип поля (`NEUTRAL/DANGER/SUCCESS`).
 * @param size Размер поля.
 * @param cornerRadius Радиус скругления поля.
 * @param enabled Доступность поля.
 * @param readOnly Режим только для чтения.
 * @param singleLine Однострочный режим.
 * @param leadingIcon Иконка слева.
 * @param trailingIcon Иконка справа.
 * @param onTrailingIconClick Callback клика по правой иконке. Если `null`, иконка не кликабельна.
 * @param supportingText Дополнительный текст под полем (подсказка/ошибка).
 * @param visualTransformation Визуальная трансформация текста.
 * @param keyboardOptions Настройки клавиатуры.
 * @param keyboardActions Обработчики действий клавиатуры.
 */
@Composable
fun MoldFilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.NEUTRAL,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    MoldBaseTextField(
        style = MoldTextFieldStyle.FILLED,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

/**
 * Универсальное текстовое поле с обводкой.
 *
 * API идентичен [MoldFilledTextField] и [MoldPlainTextField].
 */
@Composable
fun MoldOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.NEUTRAL,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    MoldBaseTextField(
        style = MoldTextFieldStyle.OUTLINED,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

/**
 * Универсальное «плоское» текстовое поле без заливки и обводки.
 *
 * API идентичен [MoldFilledTextField] и [MoldOutlinedTextField].
 */
@Composable
fun MoldPlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.NEUTRAL,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    MoldBaseTextField(
        style = MoldTextFieldStyle.PLAIN,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

@Composable
private fun MoldBaseTextField(
    style: MoldTextFieldStyle,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String?,
    type: MoldTextFieldType,
    size: MoldTextFieldSize,
    cornerRadius: MoldCornerRadius,
    enabled: Boolean,
    readOnly: Boolean,
    singleLine: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    onTrailingIconClick: (() -> Unit)?,
    supportingText: String?,
    visualTransformation: VisualTransformation,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val resolved = resolveTextFieldColors(
        style = style,
        type = type,
        focused = isFocused,
    )

    val containerColor = if (enabled) resolved.containerColor else resolved.disabledContainerColor
    val borderColor = if (enabled) resolved.borderColor else resolved.disabledBorderColor
    val textColor = if (enabled) resolved.textColor else resolved.disabledTextColor
    val placeholderColor = if (enabled) resolved.placeholderColor else resolved.disabledPlaceholderColor
    val iconColor = if (enabled) resolved.iconColor else resolved.disabledIconColor
    val supportingColor = if (enabled) resolved.supportingColor else resolved.disabledTextColor
    val cursorColor = if (enabled) resolved.cursorColor else resolved.disabledTextColor

    val border = if (borderColor.alpha > 0f) BorderStroke(1.dp, borderColor) else null

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            shape = cornerRadius.shape,
            color = containerColor,
            border = border,
            modifier = Modifier.height(size.height),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxSize(),
                enabled = enabled,
                readOnly = readOnly,
                singleLine = singleLine,
                textStyle = textFieldTextStyle(size = size).copy(color = textColor),
                cursorBrush = SolidColor(cursorColor),
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = size.horizontalPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(size.contentSpacing),
                    ) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(size.iconSize),
                            )
                        }

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                                Text(
                                    text = placeholder,
                                    style = textFieldTextStyle(size = size),
                                    color = placeholderColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }

                        if (trailingIcon != null) {
                            val trailingModifier = if (onTrailingIconClick != null) {
                                Modifier
                                    .size((size.iconSize.value + 8f).dp)
                                    .clickable(enabled = enabled, onClick = onTrailingIconClick)
                            } else {
                                Modifier.size((size.iconSize.value + 8f).dp)
                            }

                            Box(
                                modifier = trailingModifier,
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = trailingIcon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(size.iconSize),
                                )
                            }
                        }
                    }
                },
            )
        }

        if (!supportingText.isNullOrBlank()) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.labelSmall,
                color = supportingColor,
            )
        }
    }
}

@Composable
private fun textFieldTextStyle(size: MoldTextFieldSize): TextStyle {
    return when (size) {
        MoldTextFieldSize.LARGE -> MaterialTheme.typography.bodyMedium
        MoldTextFieldSize.MEDIUM -> MaterialTheme.typography.bodySmall
        MoldTextFieldSize.SMALL -> MaterialTheme.typography.labelMedium
        MoldTextFieldSize.XSMALL -> MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
    }
}

@Composable
private fun resolveTextFieldColors(
    style: MoldTextFieldStyle,
    type: MoldTextFieldType,
    focused: Boolean,
): MoldTextFieldResolvedColors {
    val baseColor = when (type) {
        MoldTextFieldType.NEUTRAL -> MoldBlue
        MoldTextFieldType.DANGER -> MoldRed
        MoldTextFieldType.SUCCESS -> MoldGreen
    }

    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val placeholder = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
    val supporting = if (type == MoldTextFieldType.NEUTRAL) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        baseColor
    }

    val disabledContainer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    val disabledBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val disabledText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    return when (style) {
        MoldTextFieldStyle.FILLED -> MoldTextFieldResolvedColors(
            containerColor = if (focused) baseColor.copy(alpha = 0.10f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            borderColor = if (focused) baseColor.copy(alpha = 0.65f) else Color.Transparent,
            textColor = onSurface,
            placeholderColor = placeholder,
            iconColor = if (focused) baseColor else MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = baseColor,
            supportingColor = supporting,
            disabledContainerColor = disabledContainer,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = disabledText,
            disabledPlaceholderColor = disabledText,
            disabledIconColor = disabledText,
        )

        MoldTextFieldStyle.OUTLINED -> MoldTextFieldResolvedColors(
            containerColor = Color.Transparent,
            borderColor = if (focused) baseColor else outline,
            textColor = onSurface,
            placeholderColor = placeholder,
            iconColor = if (focused) baseColor else MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = baseColor,
            supportingColor = supporting,
            disabledContainerColor = Color.Transparent,
            disabledBorderColor = disabledBorder,
            disabledTextColor = disabledText,
            disabledPlaceholderColor = disabledText,
            disabledIconColor = disabledText,
        )

        MoldTextFieldStyle.PLAIN -> MoldTextFieldResolvedColors(
            containerColor = Color.Transparent,
            borderColor = Color.Transparent,
            textColor = onSurface,
            placeholderColor = placeholder,
            iconColor = if (focused) baseColor else MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = baseColor,
            supportingColor = supporting,
            disabledContainerColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = disabledText,
            disabledPlaceholderColor = disabledText,
            disabledIconColor = disabledText,
        )
    }
}
