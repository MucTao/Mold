package org.muc.ui.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.muc.ui.design.MoldBlue
import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.design.MoldGreen
import org.muc.ui.design.MoldRed

/**
 * 通用填充式按钮。
 *
 * 与 [MoldOutlinedButton]（描边按钮）和 [MoldPlainButton]（纯文本按钮）拥有统一的API，
 * 因此无需修改其他代码即可切换按钮样式。
 *
 * @param onClick 点击回调。
 * @param text 按钮文本。若为 `null`，按钮可仅显示图标（icon-only）。
 * @param modifier 容器修饰符。
 * @param type 颜色类型（`中性/危险/成功`），使用固定调色板。
 * @param size 按钮尺寸。
 * @param cornerRadius 圆角半径。
 * @param enabled 按钮是否可用。
 * @param loading 是否显示内置加载指示器。为 `true` 时点击事件会被屏蔽。
 * @param leadingIcon 文本左侧的图标。
 * @param trailingIcon 文本右侧的图标。
 * @param contentDescription 无障碍访问文本（仅图标按钮时需配置）。
 * @param fullWidth 是否将按钮拉伸至容器全宽。
 */
@Composable
fun MoldFilledButton(
    onClick: () -> Unit,
    text: String? = null,
    modifier: Modifier = Modifier,
    type: MoldButtonType = MoldButtonType.PRIMARY,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
    fullWidth: Boolean = false,
) {
    MoldBaseTextButton(
        style = MoldButtonStyle.FILLED,
        onClick = onClick,
        text = text,
        modifier = modifier,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentDescription = contentDescription,
        fullWidth = fullWidth,
    )
}

@Composable
fun MoldFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: MoldButtonType = MoldButtonType.PRIMARY,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
    fullWidth: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    MoldBaseButton(
        style = MoldButtonStyle.FILLED,
        onClick = onClick,
        modifier = modifier,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentDescription = contentDescription,
        fullWidth = fullWidth,
        content = content
    )
}

/**
 * 通用描边式按钮。
 *
 * API 与 [MoldFilledButton]（填充按钮）和 [MoldPlainButton]（纯文本按钮）完全一致。
 */
@Composable
fun MoldOutlinedButton(
    onClick: () -> Unit,
    text: String? = null,
    modifier: Modifier = Modifier,
    type: MoldButtonType = MoldButtonType.PRIMARY,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
    fullWidth: Boolean = false,
) {
    MoldBaseTextButton(
        style = MoldButtonStyle.OUTLINED,
        onClick = onClick,
        text = text,
        modifier = modifier,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentDescription = contentDescription,
        fullWidth = fullWidth,
    )
}

@Composable
fun MoldOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: MoldButtonType = MoldButtonType.PRIMARY,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
    fullWidth: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    MoldBaseButton(
        style = MoldButtonStyle.OUTLINED,
        onClick = onClick,
        content = content,
        modifier = modifier,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentDescription = contentDescription,
        fullWidth = fullWidth,
    )
}

/**
 * 通用「纯文本」按钮（无填充、无描边）。
 *
 * API 与 [MoldFilledButton]（填充按钮）和 [MoldOutlinedButton]（描边按钮）完全一致。
 */
@Composable
fun MoldPlainButton(
    onClick: () -> Unit,
    text: String? = null,
    modifier: Modifier = Modifier,
    type: MoldButtonType = MoldButtonType.PRIMARY,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
    fullWidth: Boolean = false,
) {
    MoldBaseTextButton(
        style = MoldButtonStyle.PLAIN,
        onClick = onClick,
        text = text,
        modifier = modifier,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentDescription = contentDescription,
        fullWidth = fullWidth,
    )
}

@Composable
fun MoldPlainButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: MoldButtonType = MoldButtonType.PRIMARY,
    size: MoldButtonSize = MoldButtonSize.MEDIUM,
    cornerRadius: MoldCornerRadius = MoldCornerRadius.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentDescription: String? = null,
    fullWidth: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    MoldBaseButton(
        style = MoldButtonStyle.PLAIN,
        onClick = onClick,
        modifier = modifier,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentDescription = contentDescription,
        fullWidth = fullWidth,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoldBaseButton(
    style: MoldButtonStyle,
    onClick: () -> Unit,
    content: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier,
    type: MoldButtonType,
    size: MoldButtonSize,
    cornerRadius: MoldCornerRadius,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    contentDescription: String?,
    fullWidth: Boolean,
) {
    val resolvedColors = resolveButtonColors(style = style, type = type)
    val isClickable = enabled && !loading
    val hasContent = content != null
    val isIconOnly = !hasContent

    val containerColor = if (enabled) resolvedColors.containerColor else resolvedColors.disabledContainerColor
    val contentColor = if (enabled) resolvedColors.contentColor else resolvedColors.disabledContentColor
    val borderColor = if (enabled) resolvedColors.borderColor else resolvedColors.disabledBorderColor
    val border = if (borderColor.alpha > 0f) BorderStroke(1.dp, borderColor) else null

    val widthModifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier
    val contentWidthModifier = if (fullWidth) Modifier.fillMaxWidth() else Modifier
    val minWidth = if (isIconOnly) size.height else size.minWidth
    val horizontalPadding = if (isIconOnly) 0.dp else size.horizontalPadding

    val semanticsModifier = if (contentDescription.isNullOrBlank()) {
        Modifier
    } else {
        Modifier.semantics { this.contentDescription = contentDescription }
    }
    val shape = cornerRadius.shape
    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        modifier = modifier
            .then(widthModifier)
            .defaultMinSize(minWidth = minWidth, minHeight = size.height)
            .height(size.height)
            .clip(shape)
            .clickable(enabled = isClickable, onClick = onClick)
            .then(semanticsModifier),
    ) {
        Row(
            modifier = contentWidthModifier
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(
                        color = resolvedColors.loaderColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(size.loaderSize),
                    )
                    if (hasContent) {
                        Box(modifier = Modifier.size(size.contentSpacing))
                        content()
                    }
                }

                else -> {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(size.iconSize),
                        )
                    }

                    if (hasContent) {
                        if (leadingIcon != null) {
                            Box(modifier = Modifier.size(size.contentSpacing))
                        }
                        content()
                    }

                    if (trailingIcon != null) {
                        if (hasContent) {
                            Box(modifier = Modifier.size(size.contentSpacing))
                        }
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(size.iconSize),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoldBaseTextButton(
    style: MoldButtonStyle,
    onClick: () -> Unit,
    text: String?,
    modifier: Modifier,
    type: MoldButtonType,
    size: MoldButtonSize,
    cornerRadius: MoldCornerRadius,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    contentDescription: String?,
    fullWidth: Boolean,
) {
    val content: (@Composable RowScope.() -> Unit)? = if (text.isNullOrBlank()) null else {
        {
            Text(
                text = text,
                style = buttonTextStyle(size),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    MoldBaseButton(
        style = style,
        onClick = onClick,
        content = content,
        modifier = modifier,
        type = type,
        size = size,
        cornerRadius = cornerRadius,
        enabled = enabled,
        loading = loading,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        contentDescription = contentDescription,
        fullWidth = fullWidth,
    )
}

/**
 * 解析按钮配色
 *
 * 根据按钮样式和类型，返回对应的配色方案
 * @param style 按钮样式（填充/描边/纯文本）
 * @param type 按钮颜色类型（中性/危险/成功）
 * @return 解析后的按钮配色集合
 */
@Composable
private fun resolveButtonColors(
    style: MoldButtonStyle,
    type: MoldButtonType,
): MoldButtonResolvedColors {
    val baseColor = when (type) {
        MoldButtonType.NEUTRAL -> MoldBlue // 中性色（ADB Deck 蓝色）
        MoldButtonType.DANGER -> MoldRed   // 危险色（ADB Deck 红色）
        MoldButtonType.SUCCESS -> MoldGreen // 成功色（ADB Deck 绿色）
        MoldButtonType.NORMAL -> MaterialTheme.colorScheme.onSurface
        MoldButtonType.PRIMARY -> MaterialTheme.colorScheme.primary
    }

    val disabledContent = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // 禁用态内容色
    val disabledContainer = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) // 禁用态容器色
    val disabledBorder = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) // 禁用态描边色

    return when (style) {
        MoldButtonStyle.FILLED -> MoldButtonResolvedColors(
            containerColor = baseColor, // 容器填充色
            contentColor = Color.White, // 内容（文字/图标）色
            borderColor = Color.Transparent, // 描边色（透明）
            disabledContainerColor = disabledContainer, // 禁用态容器色
            disabledContentColor = disabledContent, // 禁用态内容色
            disabledBorderColor = Color.Transparent, // 禁用态描边色
            loaderColor = Color.White, // 加载指示器颜色
        )

        MoldButtonStyle.OUTLINED -> MoldButtonResolvedColors(
            containerColor = Color.Transparent, // 容器填充色（透明）
            contentColor = baseColor, // 内容色
            borderColor = baseColor, // 描边色
            disabledContainerColor = Color.Transparent, // 禁用态容器色
            disabledContentColor = disabledContent, // 禁用态内容色
            disabledBorderColor = disabledBorder, // 禁用态描边色
            loaderColor = baseColor, // 加载指示器颜色
        )

        MoldButtonStyle.PLAIN -> MoldButtonResolvedColors(
            containerColor = Color.Transparent, // 容器填充色（透明）
            contentColor = baseColor, // 内容色
            borderColor = Color.Transparent, // 描边色（透明）
            disabledContainerColor = Color.Transparent, // 禁用态容器色
            disabledContentColor = disabledContent, // 禁用态内容色
            disabledBorderColor = Color.Transparent, // 禁用态描边色
            loaderColor = baseColor, // 加载指示器颜色
        )
    }
}

/**
 * 获取按钮文本样式
 *
 * 根据按钮尺寸返回对应的文字样式
 * @param size 按钮尺寸
 * @return 适配尺寸的文本样式
 */
@Composable
private fun buttonTextStyle(size: MoldButtonSize): TextStyle {
    return when (size) {
        MoldButtonSize.XLARGE -> MaterialTheme.typography.titleLarge // 大尺寸
        MoldButtonSize.LARGE -> MaterialTheme.typography.labelLarge // 大尺寸
        MoldButtonSize.MEDIUM -> MaterialTheme.typography.labelMedium // 中尺寸
        MoldButtonSize.SMALL -> MaterialTheme.typography.labelSmall // 小尺寸
        MoldButtonSize.XSMALL -> MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp) // 超小尺寸
    }
}
