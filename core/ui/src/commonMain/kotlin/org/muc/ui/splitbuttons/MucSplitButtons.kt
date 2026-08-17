package org.muc.ui.splitbuttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adbdeck.core.ui.splitbuttons.MucSplitButtonColors
import com.adbdeck.core.ui.splitbuttons.MucSplitButtonDefaults
import com.adbdeck.core.ui.splitbuttons.MucSplitButtonSize
import com.adbdeck.core.ui.splitbuttons.MucSplitMenuItem
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MucCornerRadius
import org.muc.ui.design.MucTheme

/**
 * 通用分割按钮（主操作 + 菜单展开按钮）。
 *
 * 适用于“主操作 + 附加选项”的场景，
 * 例如通过下拉菜单选择日志级别。
 *
 * @param text 主按钮文本。
 * @param onPrimaryClick 主操作回调（分割按钮左侧部分）。
 * @param menuItems 下拉菜单项。
 * @param onMenuItemClick 菜单项选择回调。
 * @param modifier 外部容器修饰符。
 * @param size 分割按钮尺寸。
 * @param cornerRadius 分割按钮外圆角半径。
 * @param enabled 分割按钮整体是否可用。
 * @param menuEnabled 菜单展开按钮是否可用。
 * @param selectedMenuValue 当前选中值（用于在菜单中标记）。
 * @param showSelectedCheckmark 是否显示选中项的勾选标记。
 * @param colors 组件配色方案。
 */
@Composable
fun <T> MucSplitButton(
    text: String,
    onPrimaryClick: () -> Unit,
    menuItems: List<MucSplitMenuItem<T>>,
    onMenuItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    size: MucSplitButtonSize = MucSplitButtonSize.MEDIUM,
    cornerRadius: MucCornerRadius = MucCornerRadius.MEDIUM,
    enabled: Boolean = true,
    menuEnabled: Boolean = true,
    selectedMenuValue: T? = null,
    showSelectedCheckmark: Boolean = true,
    colors: MucSplitButtonColors = MucSplitButtonDefaults.colors(),
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy((-1).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SplitPrimaryPart(
                text = text,
                enabled = enabled,
                size = size,
                colors = colors,
                shape = RoundedCornerShape(
                    topStart = cornerRadius.value,
                    bottomStart = cornerRadius.value,
                ),
                onClick = onPrimaryClick,
            )

            SplitMenuPart(
                enabled = enabled && menuEnabled && menuItems.isNotEmpty(),
                size = size,
                colors = colors,
                shape = RoundedCornerShape(
                    topEnd = cornerRadius.value,
                    bottomEnd = cornerRadius.value,
                ),
                onClick = { menuExpanded = true },
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    enabled = item.enabled,
                    onClick = {
                        menuExpanded = false
                        onMenuItemClick(item.value)
                    },
                    leadingIcon = if (showSelectedCheckmark && item.value == selectedMenuValue) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSizeSmall),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun SplitPrimaryPart(
    text: String,
    enabled: Boolean,
    size: MucSplitButtonSize,
    colors: MucSplitButtonColors,
    shape: Shape,
    onClick: () -> Unit,
) {
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, colors.borderColor),
        modifier = Modifier
            .defaultMinSize(minHeight = size.height, minWidth = 64.dp)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = size.horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = splitTextStyle(size),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SplitMenuPart(
    enabled: Boolean,
    size: MucSplitButtonSize,
    colors: MucSplitButtonColors,
    shape: Shape,
    onClick: () -> Unit,
) {
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, colors.borderColor),
        modifier = Modifier
            .defaultMinSize(minHeight = size.height, minWidth = size.menuPartWidth)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size.menuPartWidth, size.height),
        ) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "Открыть меню",
                modifier = Modifier.size(size.iconSize),
            )
        }
    }
}

@Composable
private fun splitTextStyle(size: MucSplitButtonSize): TextStyle {
    return when (size) {
        MucSplitButtonSize.LARGE -> MaterialTheme.typography.labelLarge
        MucSplitButtonSize.MEDIUM -> MaterialTheme.typography.labelMedium
        MucSplitButtonSize.SMALL -> MaterialTheme.typography.labelSmall
        MucSplitButtonSize.XSMALL -> MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
    }
}

@Composable
private fun SplitButtonsPreviewContent(isDarkTheme: Boolean) {
    MucTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.paddingDefault),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            ) {
                MucSplitButton(
                    text = "Log level: All",
                    onPrimaryClick = {},
                    menuItems = listOf(
                        MucSplitMenuItem("all", "All"),
                        MucSplitMenuItem("v", "Verbose"),
                        MucSplitMenuItem("d", "Debug"),
                        MucSplitMenuItem("i", "Info"),
                        MucSplitMenuItem("w", "Warning"),
                        MucSplitMenuItem("e", "Error"),
                    ),
                    selectedMenuValue = "all",
                    onMenuItemClick = {},
                    size = MucSplitButtonSize.LARGE,
                    cornerRadius = MucCornerRadius.LARGE,
                )

                MucSplitButton(
                    text = "Mode: Compact",
                    onPrimaryClick = {},
                    menuItems = listOf(
                        MucSplitMenuItem("compact", "Compact"),
                        MucSplitMenuItem("full", "Full"),
                    ),
                    selectedMenuValue = "compact",
                    onMenuItemClick = {},
                    size = MucSplitButtonSize.MEDIUM,
                    cornerRadius = MucCornerRadius.MEDIUM,
                )

                MucSplitButton(
                    text = "Level: W",
                    onPrimaryClick = {},
                    menuItems = listOf(
                        MucSplitMenuItem("all", "All"),
                        MucSplitMenuItem("w", "Warning"),
                        MucSplitMenuItem("e", "Error"),
                    ),
                    selectedMenuValue = "w",
                    onMenuItemClick = {},
                    size = MucSplitButtonSize.SMALL,
                    cornerRadius = MucCornerRadius.SMALL,
                )

                MucSplitButton(
                    text = "L: E",
                    onPrimaryClick = {},
                    menuItems = listOf(
                        MucSplitMenuItem("all", "All"),
                        MucSplitMenuItem("e", "Error"),
                    ),
                    selectedMenuValue = "e",
                    onMenuItemClick = {},
                    size = MucSplitButtonSize.XSMALL,
                    cornerRadius = MucCornerRadius.NONE,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SplitButtonsLightPreview() {
    SplitButtonsPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
private fun SplitButtonsDarkPreview() {
    SplitButtonsPreviewContent(isDarkTheme = true)
}
