package org.muc.ui.textfields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
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
 * 树形多级联动下拉选择框（Filled 样式）
 * @param options 根层级选项列表
 * @param selectedValue 最终选中的叶子节点值
 * @param onValueSelected 叶子节点选中回调
 * @param backIcon 层级返回图标
 * @param expandIcon 子节点展开图标
 */
@Composable
fun <T> MoldFilledTreeDropdownTextField(
    options: List<MoldTreeDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.NEUTRAL,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
    backIcon: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
    expandIcon: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight
) {
    MoldBaseTreeDropdownTextField(
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
        backIcon = backIcon,
        expandIcon = expandIcon
    )
}

/**
 * 树形多级联动下拉选择框（Outlined 样式）
 */
@Composable
fun <T> MoldOutlinedTreeDropdownTextField(
    options: List<MoldTreeDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.NEUTRAL,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
    backIcon: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
    expandIcon: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight
) {
    MoldBaseTreeDropdownTextField(
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
        backIcon = backIcon,
        expandIcon = expandIcon
    )
}

/**
 * 树形多级联动下拉选择框（Plain 样式）
 */
@Composable
fun <T> MoldPlainTreeDropdownTextField(
    options: List<MoldTreeDropdownOption<T>>,
    selectedValue: T?,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.NEUTRAL,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    supportingText: String? = null,
    showSelectedCheckmark: Boolean = true,
    backIcon: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
    expandIcon: ImageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight
) {
    MoldBaseTreeDropdownTextField(
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
        backIcon = backIcon,
        expandIcon = expandIcon
    )
}

/**
 * 树形下拉选择框核心实现
 */
@Composable
private fun <T> MoldBaseTreeDropdownTextField(
    style: MoldTextFieldStyle,
    options: List<MoldTreeDropdownOption<T>>,
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
    backIcon: ImageVector,
    expandIcon: ImageVector
) {
    // 下拉展开状态
    var expanded by remember { mutableStateOf(false) }
    // 下拉框宽度（适配父容器）
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    // 层级导航栈：保存每一级的选项列表，实现返回上一级
    var navigationStack by remember { mutableStateOf(listOf(options)) }
    // 当前显示的层级选项
    val currentOptions = navigationStack.last()

    // 找到选中值对应的完整标签（拼接所有父层级）
    val selectedLabel = remember(options, selectedValue) {
        fun findLabel(
            options: List<MoldTreeDropdownOption<T>>,
            targetValue: T?,
            path: MutableList<String> = mutableListOf()
        ): String {
            if (targetValue == null) return ""
            for (option in options) {
                path.add(option.label)
                if (option.value == targetValue && option.isLeaf) {
                    return path.joinToString(" / ")
                }
                val childLabel = findLabel(option.children, targetValue, path)
                if (childLabel.isNotEmpty()) return childLabel
                path.removeLast()
            }
            return ""
        }
        findLabel(options, selectedValue).ifEmpty { placeholder ?: "" }
    }

    // 下拉箭头图标（展开/收起）
    val trailingIcon = if (expanded) {
        Icons.Outlined.KeyboardArrowUp
    } else {
        Icons.Outlined.KeyboardArrowDown
    }

    // 监听组件宽度
    val fieldModifier = Modifier
        .onGloballyPositioned { coordinates ->
            anchorWidthPx = coordinates.size.width
        }

    Box(modifier = modifier) {
        // 复用原有样式的TextField
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
                onTrailingIconClick = { if (enabled) expanded = !expanded },
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
                onTrailingIconClick = { if (enabled) expanded = !expanded },
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
                onTrailingIconClick = { if (enabled) expanded = !expanded },
                supportingText = supportingText,
            )
        }

        // 点击整个区域展开/收起下拉
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(size.height)
                .clip(RoundedCornerShape(cornerRadius.value))
                .clickable(enabled = enabled) { expanded = !expanded },
        )

        // 下拉菜单
        val menuWidthDp = with(LocalDensity.current) { anchorWidthPx.toDp() }
        DropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(menuWidthDp),
        ) {
            // 层级返回按钮（非根层级显示）
            if (navigationStack.size > 1) {
                DropdownMenuItem(
                    enabled = enabled,
                    text = { Text(text = "返回上一级") },
                    leadingIcon = { Icon(backIcon, contentDescription = "返回上一级") },
                    onClick = {
                        navigationStack = navigationStack.dropLast(1)
                    }
                )
            }

            // 当前层级选项列表
            currentOptions.forEach { option ->
                DropdownMenuItem(
                    enabled = option.enabled && enabled,
                    text = {
                        Text(
                            text = option.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = if (showSelectedCheckmark && option.value == selectedValue && option.isLeaf) {
                        { Icon(Icons.Outlined.Check, contentDescription = "已选中") }
                    } else {
                        null
                    },
                    trailingIcon = if (!option.isLeaf) {
                        { Icon(expandIcon, contentDescription = "展开子节点") }
                    } else {
                        null
                    },
                    onClick = {
                        if (option.isLeaf) {
                            // 叶子节点：选中并关闭下拉
                            expanded = false
                            onValueSelected(option.value)
                        } else {
                            // 非叶子节点：进入子层级
                            navigationStack = navigationStack + listOf(option.children)
                        }
                    }
                )
            }
        }
    }
}