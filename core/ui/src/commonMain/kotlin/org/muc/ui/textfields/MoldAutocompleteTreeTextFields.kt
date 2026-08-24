package org.muc.ui.textfields

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import org.muc.ui.design.MoldCornerRadius

/**
 * 可编辑的自动补全输入框（填充样式）。
 *
 * 用户既可手动输入文本，也可从下拉建议列表中选择
 * 对应值。
 *
 * @param value 当前文本值。
 * @param onValueChange 文本变更回调。
 * @param suggestions 完整的建议列表。
 * @param onSuggestionSelected 选中建议项的回调。
 * @param modifier 容器修饰符。
 * @param placeholder 空值时的占位文本。
 * @param type 输入框颜色类型（`中性/危险/成功`）。
 * @param size 输入框尺寸。
 * @param cornerRadius 圆角半径。
 * @param enabled 输入框是否可用。
 * @param leadingIcon 左侧图标。
 * @param trailingIcon 右侧图标。若为`null`，则使用菜单箭头图标。
 * @param onTrailingIconClick 右侧图标点击事件处理器。
 * @param supportingText 输入框下方的辅助说明文本。
 * @param maxVisibleSuggestions 菜单中最大可见建议项数量。
 * @param menuMaxHeight 下拉菜单的最大高度。
 */
@Composable
fun <T> MoldFilledAutocompleteTreeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldTreeDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    onlyLeaf: Boolean = true,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.PRIMARY,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    maxVisibleSuggestions: Int = 20,
    menuMaxHeight: Dp = 280.dp,
) {
    MoldBaseAutocompleteTreeTextField(
        style = MoldTextFieldStyle.FILLED,
        value = value,
        onValueChange = onValueChange,
        suggestions = suggestions,
        onSuggestionSelected = onSuggestionSelected,
        onlyLeaf = onlyLeaf,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        supportingText = supportingText,
        maxVisibleSuggestions = maxVisibleSuggestions,
        menuMaxHeight = menuMaxHeight,
    )
}

/**
 * 可编辑的自动补全输入框（轮廓样式）。
 *
 * 接口设计与 [MoldFilledAutocompleteTextField] 和 [MoldPlainAutocompleteTextField] 保持一致。
 */
@Composable
fun <T> MoldOutlinedAutocompleteTreeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldTreeDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    onlyLeaf: Boolean = true,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.PRIMARY,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    maxVisibleSuggestions: Int = 20,
    menuMaxHeight: Dp = 280.dp,
) {
    MoldBaseAutocompleteTreeTextField(
        style = MoldTextFieldStyle.OUTLINED,
        value = value,
        onValueChange = onValueChange,
        suggestions = suggestions,
        onSuggestionSelected = onSuggestionSelected,
        onlyLeaf = onlyLeaf,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        supportingText = supportingText,
        maxVisibleSuggestions = maxVisibleSuggestions,
        menuMaxHeight = menuMaxHeight,
    )
}

/**
 * 可编辑的自动补全输入框（纯文本样式）。
 *
 * 接口设计与 [MoldFilledAutocompleteTextField] 和 [MoldOutlinedAutocompleteTextField] 保持一致。
 */
@Composable
fun <T> MoldPlainAutocompleteTreeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldTreeDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    onlyLeaf: Boolean = true,
    placeholder: String? = null,
    type: MoldTextFieldType = MoldTextFieldType.PRIMARY,
    size: MoldTextFieldSize = MoldTextFieldSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    maxVisibleSuggestions: Int = 20,
    menuMaxHeight: Dp = 280.dp,
) {
    MoldBaseAutocompleteTreeTextField(
        style = MoldTextFieldStyle.PLAIN,
        value = value,
        onValueChange = onValueChange,
        suggestions = suggestions,
        onSuggestionSelected = onSuggestionSelected,
        onlyLeaf = onlyLeaf,
        modifier = modifier,
        placeholder = placeholder,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        onTrailingIconClick = onTrailingIconClick,
        supportingText = supportingText,
        maxVisibleSuggestions = maxVisibleSuggestions,
        menuMaxHeight = menuMaxHeight,
    )
}

@Composable
private fun <T> MoldBaseAutocompleteTreeTextField(
    style: MoldTextFieldStyle,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldTreeDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
    onlyLeaf: Boolean,// 是否只返回叶子节点
    modifier: Modifier,
    placeholder: String?,
    type: MoldTextFieldType,
    size: MoldTextFieldSize,
    cornerRadius: MoldCornerRadius,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    onTrailingIconClick: (() -> Unit)?,
    supportingText: String?,
    maxVisibleSuggestions: Int,
    menuMaxHeight: Dp,
    treeIndent: Dp = 16.dp, // 树形缩进
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val suggestionsScrollState = rememberScrollState()
    val visibleLimit = maxVisibleSuggestions.coerceAtLeast(1)

    // ====================== 树形导航核心逻辑 ======================
    var navigationStack by remember(suggestions) { mutableStateOf(listOf(suggestions)) }
    val currentTreeOptions = navigationStack.last()

    // 过滤树形节点（支持搜索）
    val filteredTreeOptions = remember(currentTreeOptions, value, visibleLimit) {
        val query = value.trim()
        fun filterTree(
            options: List<MoldTreeDropdownOption<T>>,
            result: MutableList<MoldTreeDropdownOption<T>>
        ) {
            options.forEach { opt ->
                val match = query.isEmpty() || opt.label.contains(query, ignoreCase = true)
                if (match) result.add(opt)
                filterTree(opt.children, result)
            }
        }

        val list = mutableListOf<MoldTreeDropdownOption<T>>()
        filterTree(currentTreeOptions, list)
        list.take(visibleLimit)
    }

    // 判断当前使用 普通列表 / 树形列表
    val showList = filteredTreeOptions.isNotEmpty()

    // 图标
    val fallbackTrailingIcon = if (expanded) {
        Icons.Outlined.KeyboardArrowUp
    } else {
        Icons.Outlined.KeyboardArrowDown
    }
    val resolvedTrailingIcon = trailingIcon ?: fallbackTrailingIcon
    val resolvedTrailingClick = onTrailingIconClick ?: {
        if (enabled) expanded = !expanded
    }

    LaunchedEffect(enabled, filteredTreeOptions) {
        if (!enabled || !showList) expanded = false
    }

    val fieldModifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned { anchorWidthPx = it.size.width }
        .onFocusChanged {
            if (enabled && it.isFocused && showList) expanded = true
        }

    val menuWidthDp = with(LocalDensity.current) { anchorWidthPx.toDp() }
    val itemCount = filteredTreeOptions.size
    val menuHeight = remember(itemCount, menuMaxHeight) {
        (itemCount * 48).dp.coerceAtLeast(48.dp).coerceAtMost(menuMaxHeight)
    }

    Box(modifier = modifier) {
        val onTextChanged: (String) -> Unit = { newValue ->
            onValueChange(newValue)
            if (enabled) expanded = true
        }

        // 原有输入框渲染（完全不变）
        when (style) {
            MoldTextFieldStyle.FILLED -> MoldFilledTextField(
                value = value,
                onValueChange = onTextChanged,
                modifier = fieldModifier,
                placeholder = placeholder,
                type = type,
                size = size,
                cornerRadius = cornerRadius,
                enabled = enabled,
                readOnly = false,
                leadingIcon = leadingIcon,
                trailingIcon = resolvedTrailingIcon,
                onTrailingIconClick = resolvedTrailingClick,
                supportingText = supportingText,
            )

            MoldTextFieldStyle.OUTLINED -> MoldOutlinedTextField(
                value = value,
                onValueChange = onTextChanged,
                modifier = fieldModifier,
                placeholder = placeholder,
                type = type,
                size = size,
                cornerRadius = cornerRadius,
                enabled = enabled,
                readOnly = false,
                leadingIcon = leadingIcon,
                trailingIcon = resolvedTrailingIcon,
                onTrailingIconClick = resolvedTrailingClick,
                supportingText = supportingText,
            )

            MoldTextFieldStyle.PLAIN -> MoldPlainTextField(
                value = value,
                onValueChange = onTextChanged,
                modifier = fieldModifier,
                placeholder = placeholder,
                type = type,
                size = size,
                cornerRadius = cornerRadius,
                enabled = enabled,
                readOnly = false,
                leadingIcon = leadingIcon,
                trailingIcon = resolvedTrailingIcon,
                onTrailingIconClick = resolvedTrailingClick,
                supportingText = supportingText,
            )
        }

        DropdownMenu(
            expanded = expanded && showList,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(menuWidthDp),
            properties = PopupProperties(focusable = false)
        ) {
            Box(
                modifier = Modifier
                    .width(menuWidthDp)
                    .height(menuHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                        .verticalScroll(suggestionsScrollState)
                        .align(Alignment.CenterStart)
                ) {
                    // ====================== 返回上一级（树形专用） ======================
                    if (navigationStack.size > 1) {
                        DropdownMenuItem(
                            text = { Text("← 返回上一级") },
                            onClick = { navigationStack = navigationStack.dropLast(1) }
                        )
                    }
                    // ====================== 渲染树形列表 ======================
                    filteredTreeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    modifier = Modifier.padding(start = treeIndent * option.level),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                if (!option.isLeaf) {
                                    if (onlyLeaf)
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                            contentDescription = "展开下一级"
                                        )
                                    else
                                        IconButton(
                                            onClick = { navigationStack = navigationStack + listOf(option.children) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                                contentDescription = "展开下一级"
                                            )
                                        }
                                }
                            },
                            onClick = {
                                if (option.isLeaf || !onlyLeaf) {
                                    expanded = false
                                    onSuggestionSelected(option.value)
                                } else {
                                    navigationStack = navigationStack + listOf(option.children)
                                }
                            }
                        )
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd) // 靠右对齐
                        .height(menuHeight)         // 与菜单高度一致
                        .width(8.dp),               // 滚动条宽度
                    scrollState = suggestionsScrollState,
                )
            }
        }
    }
}

// 给 MoldTreeDropdownOption 加个扩展属性（自动计算层级）
val <T> MoldTreeDropdownOption<T>.level: Int
    get() {
        var depth = 0
        var parent = this
        while (parent.children.isNotEmpty()) {
            depth++
            parent = parent.children.first()
        }
        return depth
    }
