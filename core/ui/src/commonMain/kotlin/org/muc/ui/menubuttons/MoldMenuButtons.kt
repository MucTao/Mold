package org.muc.ui.menubuttons

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldOutlinedButton
import org.muc.ui.design.MoldCornerRadius

/**
 * 按钮下拉菜单的选项项。
 *
 * @param value 选项的业务值。
 * @param label 显示文本。
 * @param enabled 菜单中该选项是否可用。
 */
@Immutable
data class MoldMenuButtonOption<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

/**
 * 基于 [MoldOutlinedButton] 实现的带下拉菜单的按钮。
 *
 * @param text 按钮文本。
 * @param options 下拉菜单的选项列表。
 * @param onOptionSelected 选项选中的回调函数。
 * @param modifier 外部修饰符 [Modifier]。
 * @param enabled 按钮是否可用。
 * @param size 按钮尺寸。
 * @param cornerRadius 按钮圆角半径。
 * @param leadingIcon 文本左侧的图标。
 * @param trailingIcon 文本右侧的图标（默认是向下箭头）。
 * @param selectedOption 选中的值（用于在菜单中标记）。
 * @param showSelectedCheckmark 是否在选中项旁显示勾选标记。
 * @param contentDescription 无障碍服务描述文本（仅图标按钮时生效）。
 */
@Composable
fun <T> MoldOutlinedMenuButton(
    text: String,
    options: List<MoldMenuButtonOption<T>>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector = Icons.Outlined.KeyboardArrowDown,
    selectedOption: T? = null,
    showSelectedCheckmark: Boolean = false,
    contentDescription: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val canOpenMenu = enabled && options.isNotEmpty()

    Box(modifier = modifier) {
        MoldOutlinedButton(
            onClick = { expanded = true },
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            contentDescription = contentDescription,
            enabled = canOpenMenu,
            size = size,
            cornerRadius = cornerRadius,
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    enabled = option.enabled,
                    leadingIcon = if (showSelectedCheckmark && selectedOption == option.value) {
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
                        onOptionSelected(option.value)
                    },
                )
            }
        }
    }
}