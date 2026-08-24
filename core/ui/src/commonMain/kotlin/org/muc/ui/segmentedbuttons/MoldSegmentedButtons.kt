package org.muc.ui.segmentedbuttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.design.MoldTheme

/**
 * Универсальный single-choice segmented-контрол.
 *
 * Используется там, где может быть выбран только один вариант
 * (например, `Compact/Full`).
 *
 * @param options Набор опций.
 * @param selectedValue Текущее выбранное значение.
 * @param onValueSelected Callback выбора опции.
 * @param modifier Modifier контейнера.
 * @param size Размер сегментов.
 * @param cornerRadius Радиус скругления внешних углов компонента.
 * @param colors Цветовая схема компонента.
 */
@Composable
fun <T> MoldSingleSegmentedButtons(
    options: List<MoldSegmentedOption<T>>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    size: MoldSegmentedButtonSize = MoldSegmentedButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    colors: MoldSegmentedButtonColors = MoldSegmentedButtonDefaults.colors(),
) {
    if (options.isEmpty()) return

    Row(
        modifier = modifier.semantics(mergeDescendants = true) { },
        horizontalArrangement = Arrangement.spacedBy((-1).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            SegmentedItem(
                label = option.label,
                selected = option.value == selectedValue,
                enabled = option.enabled,
                leadingIcon = option.leadingIcon,
                optionContentColor = option.contentColor,
                indicatorColor = option.indicatorColor,
                contentDescription = option.contentDescription ?: option.label,
                onClick = { onValueSelected(option.value) },
                shape = segmentShape(
                    index = index,
                    count = options.size,
                    radius = cornerRadius.value,
                ),
                size = size,
                colors = colors,
            )
        }
    }
}

/**
 * 通用多选分段控制器。
 *
 * 适用于可同时选中多个选项的场景
 *（例如：日期/时间/毫秒/颜色 等选项）。
 *
 * @param options 选项集合
 * @param selectedValues 已选中的值集合
 * @param onValueToggle 分段选项状态切换回调
 * @param modifier 容器修饰符
 * @param size 分段控件尺寸
 * @param cornerRadius 组件外圆角半径
 * @param colors 组件配色方案
 */
@Composable
fun <T> MoldMultiSegmentedButtons(
    options: List<MoldSegmentedOption<T>>,
    selectedValues: Set<T>,
    onValueToggle: (value: T, checked: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: MoldSegmentedButtonSize = MoldSegmentedButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    colors: MoldSegmentedButtonColors = MoldSegmentedButtonDefaults.colors(),
) {
    if (options.isEmpty()) return

    Row(
        modifier = modifier.semantics(mergeDescendants = true) { },
        horizontalArrangement = Arrangement.spacedBy((-1).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            val selected = option.value in selectedValues
            SegmentedItem(
                label = option.label,
                selected = selected,
                enabled = option.enabled,
                leadingIcon = option.leadingIcon,
                optionContentColor = option.contentColor,
                indicatorColor = option.indicatorColor,
                contentDescription = option.contentDescription ?: option.label,
                onClick = { onValueToggle(option.value, !selected) },
                shape = segmentShape(
                    index = index,
                    count = options.size,
                    radius = cornerRadius.value,
                ),
                size = size,
                colors = colors,
            )
        }
    }
}

@Composable
private fun SegmentedItem(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    optionContentColor: Color?,
    indicatorColor: Color?,
    contentDescription: String,
    onClick: () -> Unit,
    shape: Shape,
    size: MoldSegmentedButtonSize,
    colors: MoldSegmentedButtonColors,
) {
    val containerColor = if (selected) colors.activeContainerColor else colors.inactiveContainerColor
    val defaultContentColor = when {
        !enabled -> colors.disabledContentColor
        selected -> colors.activeContentColor
        else -> colors.inactiveContentColor
    }
    val contentColor = if (enabled) optionContentColor ?: defaultContentColor else defaultContentColor

    val hasText = label.isNotBlank()

    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, colors.borderColor),
        modifier = Modifier
            .height(size.height)
            .defaultMinSize(minWidth = size.minWidth)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Box(
            modifier = Modifier.padding(horizontal = size.horizontalPadding),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                indicatorColor?.let { color ->
                    Box(
                        modifier = Modifier
                            .size(size.indicatorSize)
                            .background(color = color, shape = CircleShape),
                    )
                }

                leadingIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(size.indicatorSize + 10.dp),
                    )
                }

                if (hasText) {
                    Text(
                        text = label,
                        style = segmentedTextStyle(size = size),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun segmentedTextStyle(size: MoldSegmentedButtonSize): TextStyle {
    return when (size) {
        MoldSegmentedButtonSize.LARGE -> MaterialTheme.typography.labelLarge
        MoldSegmentedButtonSize.MEDIUM -> MaterialTheme.typography.labelMedium
        MoldSegmentedButtonSize.SMALL -> MaterialTheme.typography.labelSmall
        MoldSegmentedButtonSize.XSMALL -> MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
    }
}

private fun segmentShape(index: Int, count: Int, radius: Dp): Shape {
    if (count <= 1) return RoundedCornerShape(radius)
    return when (index) {
        0 -> RoundedCornerShape(topStart = radius, bottomStart = radius)
        count - 1 -> RoundedCornerShape(topEnd = radius, bottomEnd = radius)
        else -> RectangleShape
    }
}

@Composable
private fun SegmentedPreviewContent(isDarkTheme: Boolean) {
    MoldTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingDefault),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            ) {
                MoldSingleSegmentedButtons(
                    options = listOf(
                        MoldSegmentedOption("compact", "Compact"),
                        MoldSegmentedOption("full", "Full"),
                    ),
                    selectedValue = "compact",
                    onValueSelected = {},
                    size = MoldSegmentedButtonSize.LARGE,
                    cornerRadius = MoldCornerRadius.LARGE,
                )

                MoldSingleSegmentedButtons(
                    options = listOf(
                        MoldSegmentedOption("all", "All"),
                        MoldSegmentedOption("d", "D", indicatorColor = Color(0xFF4CAF50)),
                        MoldSegmentedOption("i", "I", indicatorColor = Color(0xFF2196F3)),
                        MoldSegmentedOption("w", "W", indicatorColor = Color(0xFFFF9800)),
                        MoldSegmentedOption("e", "E", indicatorColor = Color(0xFFF44336)),
                    ),
                    selectedValue = "w",
                    onValueSelected = {},
                    size = MoldSegmentedButtonSize.XSMALL,
                    cornerRadius = MoldCornerRadius.SMALL,
                )

                MoldMultiSegmentedButtons(
                    options = listOf(
                        MoldSegmentedOption("date", "Date"),
                        MoldSegmentedOption("time", "Time"),
                        MoldSegmentedOption("ms", "ms"),
                        MoldSegmentedOption("colors", "Colors"),
                    ),
                    selectedValues = setOf("date", "colors"),
                    onValueToggle = { _, _ -> },
                    size = MoldSegmentedButtonSize.MEDIUM,
                    cornerRadius = MoldCornerRadius.MEDIUM,
                )

                MoldSingleSegmentedButtons(
                    options = listOf(
                        MoldSegmentedOption("l", "Large"),
                        MoldSegmentedOption("m", "Medium"),
                        MoldSegmentedOption("s", "Small"),
                        MoldSegmentedOption("x", "XSmall"),
                    ),
                    selectedValue = "s",
                    onValueSelected = {},
                    size = MoldSegmentedButtonSize.SMALL,
                    cornerRadius = MoldCornerRadius.NONE,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SegmentedLightPreview() {
    SegmentedPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
private fun SegmentedDarkPreview() {
    SegmentedPreviewContent(isDarkTheme = true)
}
