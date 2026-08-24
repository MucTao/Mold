package org.muc.ui.textfields

import androidx.compose.foundation.ScrollState
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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
fun <T> MoldFilledAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
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
    MoldBaseAutocompleteTextField(
        style = MoldTextFieldStyle.FILLED,
        value = value,
        onValueChange = onValueChange,
        suggestions = suggestions,
        onSuggestionSelected = onSuggestionSelected,
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
fun <T> MoldOutlinedAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
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
    MoldBaseAutocompleteTextField(
        style = MoldTextFieldStyle.OUTLINED,
        value = value,
        onValueChange = onValueChange,
        suggestions = suggestions,
        onSuggestionSelected = onSuggestionSelected,
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
fun <T> MoldPlainAutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
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
    MoldBaseAutocompleteTextField(
        style = MoldTextFieldStyle.PLAIN,
        value = value,
        onValueChange = onValueChange,
        suggestions = suggestions,
        onSuggestionSelected = onSuggestionSelected,
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
private fun <T> MoldBaseAutocompleteTextField(
    style: MoldTextFieldStyle,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<MoldDropdownOption<T>>,
    onSuggestionSelected: (T) -> Unit,
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
) {
    // 下拉菜单展开状态
    var expanded by remember { mutableStateOf(false) }
    // 锚点（输入框）宽度（像素）
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    // 建议列表滚动状态
    val suggestionsScrollState: ScrollState = rememberScrollState()
    // 可见数量下限修正（至少显示1条）
    val visibleLimit = maxVisibleSuggestions.coerceAtLeast(1)

    // 标准化建议列表（去重、去空、去首尾空格）
    val normalizedSuggestions = remember(suggestions) {
        suggestions.asSequence()
            .filter { it.enabled && it.label.isNotBlank() }
            .distinct()
            .toList()
    }

    // 根据输入内容过滤建议列表
    val filteredSuggestions = remember(normalizedSuggestions, value, visibleLimit) {
        val query = value.trim()
        normalizedSuggestions
            .asSequence()
            .filter { query.isEmpty() || it.label.contains(query, ignoreCase = true) }
            .take(visibleLimit)
            .toList()
    }

    // 备用右侧图标（菜单展开/收起箭头）
    val fallbackTrailingIcon = if (expanded) {
        Icons.Outlined.KeyboardArrowUp
    } else {
        Icons.Outlined.KeyboardArrowDown
    }

    // 最终生效的右侧图标（优先使用自定义图标）
    val resolvedTrailingIcon = trailingIcon ?: fallbackTrailingIcon
    // 最终生效的右侧图标点击事件（优先使用自定义事件）
    val resolvedTrailingClick = onTrailingIconClick ?: {
        if (enabled) expanded = !expanded
    }

    // 监听可用状态和建议列表变化，自动关闭菜单
    LaunchedEffect(enabled, filteredSuggestions) {
        if (!enabled || filteredSuggestions.isEmpty()) {
            expanded = false
        }
    }

    // 输入框基础修饰符（宽满、记录宽度、焦点监听）
    val fieldModifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned { coordinates ->
            anchorWidthPx = coordinates.size.width
        }
        .onFocusChanged { focusState ->
            // 获取焦点且有建议项时自动展开菜单
            if (enabled && focusState.isFocused && filteredSuggestions.isNotEmpty()) {
                expanded = true
            }
        }

    // 菜单宽度（与输入框保持一致）
    val menuWidthDp = with(LocalDensity.current) { anchorWidthPx.toDp() }
    // 菜单高度（根据建议项数量自适应，限制最大/最小值）
    val menuHeight = remember(filteredSuggestions.size, menuMaxHeight) {
        (filteredSuggestions.size * 48).dp
            .coerceAtLeast(48.dp)      // 最小高度48dp（至少显示1项）
            .coerceAtMost(menuMaxHeight) // 最大高度不超过设定值
    }

    Box(modifier = modifier) {
        // 文本变更处理器（同步更新值并展开菜单）
        val onTextChanged: (String) -> Unit = { newValue ->
            onValueChange(newValue)
            if (enabled) {
                expanded = true
            }
        }

        // 根据样式渲染不同类型的输入框
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

        // 下拉建议菜单
        DropdownMenu(
            expanded = expanded && filteredSuggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(menuWidthDp),
            properties = PopupProperties(
                focusable = false, // 菜单不获取焦点（避免遮挡输入）
            ),
        ) {
            Box(
                modifier = Modifier
                    .width(menuWidthDp)
                    .height(menuHeight),
            ) {
                // 建议列表容器（可滚动）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(end = 8.dp) // 给滚动条留空间
                        .verticalScroll(suggestionsScrollState)
                        .align(Alignment.CenterStart),
                ) {
                    // 渲染所有过滤后的建议项
                    filteredSuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = suggestion.label,
                                    maxLines = 1,         // 单行显示
                                    overflow = TextOverflow.Ellipsis, // 超长文本省略
                                )
                            },
                            onClick = {
                                expanded = false // 选中后关闭菜单
                                onSuggestionSelected(suggestion.value)
                            },
                        )
                    }
                }

                // 垂直滚动条
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

@Composable
expect fun VerticalScrollbar(modifier:Modifier,scrollState: ScrollState )
