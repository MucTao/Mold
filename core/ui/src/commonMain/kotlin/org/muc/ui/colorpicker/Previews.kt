package org.muc.ui.colorpicker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.muc.ui.design.MoldTheme


@Composable
private fun MoldButtonsPreviewContent(isDarkTheme: Boolean) {
    MoldTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ColorPicker(
                initialColor = Color.Green,
                onColorConfirmed = {

                },
                onCancel = {

                },
            )
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
