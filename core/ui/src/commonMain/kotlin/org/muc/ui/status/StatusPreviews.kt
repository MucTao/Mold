package org.muc.ui.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.muc.ui.design.MoldTheme

@Composable
private fun CoreUiPreviewContainer(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MoldTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun CoreStatesPreviewBody(isDarkTheme: Boolean) {
    CoreUiPreviewContainer(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            LoadingView(
                message = "加载中…",
                modifier = Modifier.weight(1f),
            )
            EmptyView(
                message = "204 没有数据",
                modifier = Modifier.weight(1f),
            )
            ErrorView(
                message = "错误",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview
@Composable
private fun CoreUiLightPreview() {
    CoreStatesPreviewBody(isDarkTheme = false)
}

@Preview
@Composable
private fun CoreUiDarkPreview() {
    CoreStatesPreviewBody(isDarkTheme = true)
}
