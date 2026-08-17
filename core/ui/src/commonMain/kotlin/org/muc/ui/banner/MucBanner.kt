package org.muc.ui.banner

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MucTheme

private data class MucBannerVisuals(
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
)


@Composable
fun MucBanner(
    message: String,
    alignment: Alignment,
    type: MucBannerType = MucBannerType.INFO,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    dismissStyle: MucBannerDismissStyle = MucBannerDismissStyle.ICON,
    dismissText: String = "ok",
) {
    // 🎯 核心魔法：使用全屏透明、允许点击穿透的 Popup 将 Banner 提权到最顶层
    Popup(
        alignment = alignment, // 响应外部传入的位置参数
        properties = PopupProperties(
            focusable = false,          // ⚡ 关键：必须为 false，这样背后的 Dialog 和输入框才能正常响应点击和打字
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        // 外部传入的 modifier 会作用在这层 Surface 上，比如设置 padding、width 等
        val visuals = bannerVisuals(type = type)

        Surface(
            modifier = modifier
                // 给通知横幅加上默认防贴边间距（可被外部 modifier 覆盖）
                .padding(horizontal = Dimensions.paddingMedium, vertical = Dimensions.paddingSmall),
            color = visuals.containerColor,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp, // 略微调高阴影，使其在顶层更有立体感
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.paddingMedium, vertical = Dimensions.paddingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall),
            ) {
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = null,
                    tint = visuals.contentColor,
                    modifier = Modifier.size(Dimensions.iconSizeSmall),
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = visuals.contentColor,
                    modifier = Modifier.weight(1f),
                )

                if (onDismiss != null) {
                    BannerDismissAction(
                        style = dismissStyle,
                        text = dismissText,
                        tint = visuals.contentColor,
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
fun MucBanner(
    message: String,
    type: MucBannerType = MucBannerType.INFO,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    dismissStyle: MucBannerDismissStyle = MucBannerDismissStyle.ICON,
    dismissText: String = "ok",
) {
    val visuals = bannerVisuals(type = type)

    Surface(
        modifier = modifier,
        color = visuals.containerColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingMedium, vertical = Dimensions.paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall),
        ) {
            Icon(
                imageVector = visuals.icon,
                contentDescription = null,
                tint = visuals.contentColor,
                modifier = Modifier.size(Dimensions.iconSizeSmall),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = visuals.contentColor,
                modifier = Modifier.weight(1f),
            )

            if (onDismiss != null) {
                BannerDismissAction(
                    style = dismissStyle,
                    text = dismissText,
                    tint = visuals.contentColor,
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun BannerDismissAction(
    style: MucBannerDismissStyle,
    text: String,
    tint: Color,
    onClick: () -> Unit,
) {
    when (style) {
        MucBannerDismissStyle.ICON -> {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "关闭",
                    tint = tint,
                    modifier = Modifier.size(Dimensions.iconSizeSmall),
                )
            }
        }

        MucBannerDismissStyle.TEXT -> {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .defaultMinSize(minWidth = 24.dp)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.padding(horizontal = Dimensions.paddingXSmall),
                )
            }
        }
    }
}

@Composable
private fun bannerVisuals(type: MucBannerType): MucBannerVisuals {
    return when (type) {
        MucBannerType.INFO -> MucBannerVisuals(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Outlined.Info,
        )

        MucBannerType.SUCCESS -> MucBannerVisuals(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Outlined.CheckCircle,
        )

        MucBannerType.WARNING -> MucBannerVisuals(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Outlined.WarningAmber,
        )

        MucBannerType.ERROR -> MucBannerVisuals(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Outlined.Error,
        )
    }
}

@Composable
private fun MucBannerPreviewContent(isDarkTheme: Boolean) {
    MucTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.paddingDefault),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MucBanner(
                    message = "设备连接信息",
                    type = MucBannerType.INFO,
                    onDismiss = {},
                )
                MucBanner(
                    message = "操作已成功完成。",
                    type = MucBannerType.SUCCESS,
                    onDismiss = {},
                )
                MucBanner(
                    message = "设备电量过低，可能会出现延迟",
                    type = MucBannerType.WARNING,
                    onDismiss = {},
                    dismissStyle = MucBannerDismissStyle.TEXT,
                )
                MucBanner(
                    message = "无法执行 adb shell",
                    type = MucBannerType.ERROR,
                    onDismiss = {},
                )
                MucBanner(
                    message = "不含关闭按钮的横幅",
                    type = MucBannerType.INFO,
                )
            }
        }
    }
}
@Preview
@Composable
fun MucBannerLightPreview() {
    MucBannerPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
fun MucBannerDarkPreview() {
    MucBannerPreviewContent(isDarkTheme = true)
}
