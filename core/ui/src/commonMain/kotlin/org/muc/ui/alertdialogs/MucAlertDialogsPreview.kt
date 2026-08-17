package org.muc.ui.alertdialogs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.muc.ui.buttons.MucButtonType
import org.muc.ui.design.MucTheme

@Composable
private fun MucAlertDialogPreviewContent(isDarkTheme: Boolean, loading: Boolean) {
    MucTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            MucAlertDialog(
                onDismissRequest = {},
                title = if (loading) "文件已存在" else "删除此项？",
                titleIcon = if (loading) Icons.Outlined.WarningAmber else null,
                titleIconTint = MaterialTheme.colorScheme.error,
                confirmAction = MucAlertDialogAction(
                    text = if (loading) "覆盖" else "删除",
                    onClick = {},
                    type = if (loading) MucButtonType.DANGER else MucButtonType.NEUTRAL,
                    loading = loading,
                ),
                dismissAction = MucAlertDialogAction(
                    text = "取消",
                    onClick = {},
                ),
            ) {
                Text(
                    text = if (loading) {
                        "目标路径下已存在同名项目，是否覆盖？"
                    } else {
                        "即将删除: /sdcard/Documents/report.txt"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview
@Composable
private fun MucAlertDialogLightPreview() {
    MucAlertDialogPreviewContent(
        isDarkTheme = false,
        loading = false,
    )
}

@Preview
@Composable
private fun MucAlertDialogDarkPreview() {
    MucAlertDialogPreviewContent(
        isDarkTheme = true,
        loading = true,
    )
}
