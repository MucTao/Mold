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
import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.design.MoldTheme

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
private fun MoldButtonsPreviewContent(isDarkTheme: Boolean) {
    MoldTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.paddingDefault),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            ) {
                PreviewSection(title = "Filled") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                        MoldFilledButton(
                            onClick = {},
                            text = "下载",
                            type = MoldButtonType.NEUTRAL,
                            leadingIcon = Icons.Outlined.Download,
                        )
                        MoldFilledButton(
                            onClick = {},
                            text = "删除",
                            type = MoldButtonType.DANGER,
                            leadingIcon = Icons.Outlined.Delete,
                        )
                        MoldFilledButton(
                            onClick = {},
                            text = "完成",
                            type = MoldButtonType.SUCCESS,
                            trailingIcon = Icons.Outlined.Check,
                        )
                        MoldFilledButton(
                            onClick = {},
                            text = "加载中",
                            loading = true,
                        )
                        MoldFilledButton(
                            onClick = {},
                            enabled = false,
                            leadingIcon = Icons.Outlined.PlayArrow,
                            contentDescription = "Disabled play",
                        )
                    }
                }

                PreviewSection(title = "Outlined") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                        MoldOutlinedButton(
                            onClick = {},
                            text = "播放",
                            leadingIcon = Icons.Outlined.PlayArrow,
                        )
                        MoldOutlinedButton(
                            onClick = {},
                            text = "重试",
                            type = MoldButtonType.DANGER,
                            loading = true,
                        )
                        MoldOutlinedButton(
                            onClick = {},
                            type = MoldButtonType.SUCCESS,
                            leadingIcon = Icons.Outlined.Check,
                            contentDescription = "Success icon",
                        )
                    }
                }

                PreviewSection(title = "Plain") {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                        MoldPlainButton(
                            onClick = {},
                            text = "下载",
                            leadingIcon = Icons.Outlined.Download,
                        )
                        MoldPlainButton(
                            onClick = {},
                            text = "禁用",
                            type = MoldButtonType.DANGER,
                            enabled = false,
                        )
                        MoldPlainButton(
                            onClick = {},
                            type = MoldButtonType.SUCCESS,
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
                        MoldFilledButton(
                            onClick = {},
                            text = "L",
                            size = MoldButtonSize.LARGE,
                            cornerRadius = MoldCornerRadius.XLARGE,
                        )
                        MoldFilledButton(
                            onClick = {},
                            text = "M",
                            size = MoldButtonSize.MEDIUM,
                            cornerRadius = MoldCornerRadius.LARGE,
                        )
                        MoldFilledButton(
                            onClick = {},
                            text = "S",
                            size = MoldButtonSize.SMALL,
                            cornerRadius = MoldCornerRadius.SMALL,
                        )
                        MoldFilledButton(
                            onClick = {},
                            text = "XS",
                            size = MoldButtonSize.XSMALL,
                            cornerRadius = MoldCornerRadius.NONE,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MoldButtonsLightPreview() {
    MoldButtonsPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
private fun MoldButtonsDarkPreview() {
    MoldButtonsPreviewContent(isDarkTheme = true)
}
