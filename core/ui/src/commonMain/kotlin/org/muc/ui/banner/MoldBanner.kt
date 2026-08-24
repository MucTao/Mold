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
import org.muc.ui.design.MoldTheme

private data class MoldBannerVisuals(
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
)


@Composable
fun MoldBanner(
    message: String,
    alignment: Alignment,
    type: MoldBannerType = MoldBannerType.INFO,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    dismissStyle: MoldBannerDismissStyle = MoldBannerDismissStyle.ICON,
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
fun MoldBanner(
    message: String,
    type: MoldBannerType = MoldBannerType.INFO,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    dismissStyle: MoldBannerDismissStyle = MoldBannerDismissStyle.ICON,
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
    style: MoldBannerDismissStyle,
    text: String,
    tint: Color,
    onClick: () -> Unit,
) {
    when (style) {
        MoldBannerDismissStyle.ICON -> {
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

        MoldBannerDismissStyle.TEXT -> {
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
private fun bannerVisuals(type: MoldBannerType): MoldBannerVisuals {
    return when (type) {
        MoldBannerType.INFO -> MoldBannerVisuals(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Outlined.Info,
        )

        MoldBannerType.SUCCESS -> MoldBannerVisuals(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Outlined.CheckCircle,
        )

        MoldBannerType.WARNING -> MoldBannerVisuals(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Outlined.WarningAmber,
        )

        MoldBannerType.ERROR -> MoldBannerVisuals(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Outlined.Error,
        )
    }
}

@Composable
private fun MoldBannerPreviewContent(isDarkTheme: Boolean) {
    MoldTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.paddingDefault),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MoldBanner(
                    message = "设备连接信息",
                    type = MoldBannerType.INFO,
                    onDismiss = {},
                )
                MoldBanner(
                    message = "操作已成功完成。",
                    type = MoldBannerType.SUCCESS,
                    onDismiss = {},
                )
                MoldBanner(
                    message = "设备电量过低，可能会出现延迟",
                    type = MoldBannerType.WARNING,
                    onDismiss = {},
                    dismissStyle = MoldBannerDismissStyle.TEXT,
                )
                MoldBanner(
                    message = "无法执行 adb shell",
                    type = MoldBannerType.ERROR,
                    onDismiss = {},
                )
                MoldBanner(
                    message = "不含关闭按钮的横幅",
                    type = MoldBannerType.INFO,
                )
            }
        }
    }
}
@Preview
@Composable
fun MoldBannerLightPreview() {
    MoldBannerPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
fun MoldBannerDarkPreview() {
    MoldBannerPreviewContent(isDarkTheme = true)
}
