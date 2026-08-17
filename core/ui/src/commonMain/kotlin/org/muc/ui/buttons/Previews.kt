package org.muc.ui.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MucCornerRadius
import org.muc.ui.design.MucTheme

@Composable
private fun PreviewSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun MucButtonsPreviewContent(isDarkTheme: Boolean) {
    MucTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.paddingDefault),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            ) {
                PreviewSection(title = "Filled") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                        MucFilledButton(
                            onClick = {},
                            text = "下载",
                            type = MucButtonType.NEUTRAL,
                            leadingIcon = Icons.Outlined.Download,
                        )
                        MucFilledButton(
                            onClick = {},
                            text = "删除",
                            type = MucButtonType.DANGER,
                            leadingIcon = Icons.Outlined.Delete,
                        )
                        MucFilledButton(
                            onClick = {},
                            text = "完成",
                            type = MucButtonType.SUCCESS,
                            trailingIcon = Icons.Outlined.Check,
                        )
                        MucFilledButton(
                            onClick = {},
                            text = "加载中",
                            loading = true,
                        )
                        MucFilledButton(
                            onClick = {},
                            enabled = false,
                            leadingIcon = Icons.Outlined.PlayArrow,
                            contentDescription = "Disabled play",
                        )
                    }
                }

                PreviewSection(title = "Outlined") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                        MucOutlinedButton(
                            onClick = {},
                            text = "播放",
                            leadingIcon = Icons.Outlined.PlayArrow,
                        )
                        MucOutlinedButton(
                            onClick = {},
                            text = "重试",
                            type = MucButtonType.DANGER,
                            loading = true,
                        )
                        MucOutlinedButton(
                            onClick = {},
                            type = MucButtonType.SUCCESS,
                            leadingIcon = Icons.Outlined.Check,
                            contentDescription = "Success icon",
                        )
                    }
                }

                PreviewSection(title = "Plain") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                        MucPlainButton(
                            onClick = {},
                            text = "下载",
                            leadingIcon = Icons.Outlined.Download,
                        )
                        MucPlainButton(
                            onClick = {},
                            text = "禁用",
                            type = MucButtonType.DANGER,
                            enabled = false,
                        )
                        MucPlainButton(
                            onClick = {},
                            type = MucButtonType.SUCCESS,
                            loading = true,
                            contentDescription = "Loading success icon",
                        )
                    }
                }

                PreviewSection(title = "尺寸展示") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall),
                    ) {
                        MucFilledButton(
                            onClick = {},
                            text = "L",
                            size = MucButtonSize.LARGE,
                            cornerRadius = MucCornerRadius.XLARGE,
                        )
                        MucFilledButton(
                            onClick = {},
                            text = "M",
                            size = MucButtonSize.MEDIUM,
                            cornerRadius = MucCornerRadius.LARGE,
                        )
                        MucFilledButton(
                            onClick = {},
                            text = "S",
                            size = MucButtonSize.SMALL,
                            cornerRadius = MucCornerRadius.SMALL,
                        )
                        MucFilledButton(
                            onClick = {},
                            text = "XS",
                            size = MucButtonSize.XSMALL,
                            cornerRadius = MucCornerRadius.NONE,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MucButtonsLightPreview() {
    MucButtonsPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
private fun MucButtonsDarkPreview() {
    MucButtonsPreviewContent(isDarkTheme = true)
}
