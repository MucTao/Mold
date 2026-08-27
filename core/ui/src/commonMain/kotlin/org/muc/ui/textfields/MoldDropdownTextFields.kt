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
import org.muc.ui.design.MoldCornerRadius

/**
 * `filled` 样式的下拉输入框。
 *
 * API 与 [MoldOutlinedDropdownTextField] 和 [MoldPlainDropdownTextField] 保持一致，
 * 以便在使用处相互替换。
 *
 * @param options 下拉列表选项。
 * @param selectedValue 当前选中的业务值。
 * @param onValueSelected 选项选择回调。
 * @param modifier 容器的 Modifier。
 * @param placeholder 空值时的占位文本。
 * @param type 输入框的颜色类型（`NEUTRAL/DANGER/SUCCESS`）。
 * @param size 输入框尺寸。
 * @param cornerRadius 圆角半径。
 * @param enabled 输入框是否可用。
 * @param leadingIcon 左侧图标。
 * @param supportingText 输入框下方的文本（提示/错误）。
 * @param showSelectedCheckmark 是否在已选选项旁显示勾选标记。
 */
@Composable
fun <T> MoldFilledDropdownTextField(
    options: List<MoldDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.PRIMARY,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
) {
    MoldBaseDropdownTextField(
        style = MoldTextFieldStyle.FILLED,
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
 * `outlined` 样式的下拉输入框。
 *
 * API 与 [MoldFilledDropdownTextField] 和 [MoldPlainDropdownTextField] 保持一致。
 */
@Composable
fun <T> MoldOutlinedDropdownTextField(
    options: List<MoldDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.PRIMARY,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
) {
    MoldBaseDropdownTextField(
        style = MoldTextFieldStyle.OUTLINED,
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
 * `plain` 样式的下拉输入框。
 *
 * API 与 [MoldFilledDropdownTextField] 和 [MoldOutlinedDropdownTextField] 保持一致。
 */
@Composable
fun <T> MoldPlainDropdownTextField(
    options: List<MoldDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.PRIMARY,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
) {
    MoldBaseDropdownTextField(
        style = MoldTextFieldStyle.PLAIN,
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
private fun <T> MoldBaseDropdownTextField(
    style: MoldTextFieldStyle,
    options: List<MoldDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier,
    placeholder: String?,
    type: MoldTextFieldType,
    size: MoldTextFieldSize,
    cornerRadius: MoldCornerRadius,
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
            MoldTextFieldStyle.FILLED -> MoldFilledTextField(
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

            MoldTextFieldStyle.OUTLINED -> MoldOutlinedTextField(
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

            MoldTextFieldStyle.PLAIN -> MoldPlainTextField(
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

        // 点击输入框的整个区域都可打开菜单，而不只是点击箭头。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(size.height)
                .clip(cornerRadius.shape)
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
