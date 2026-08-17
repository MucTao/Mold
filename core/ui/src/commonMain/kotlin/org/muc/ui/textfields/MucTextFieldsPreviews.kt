package org.muc.ui.textfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MucCornerRadius
import org.muc.ui.design.MucTheme

@Composable
private fun PreviewBlock(
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
private fun MucTextFieldsPreviewContent(isDarkTheme: Boolean) {
    MucTheme(isDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimensions.paddingDefault),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            ) {
                val (outlinedValue, setOutlinedValue) = remember { mutableStateOf("adb logcat") }
                val (filledValue, setFilledValue) = remember { mutableStateOf("") }

                PreviewBlock(title = "Outlined / DANGER / SUCCESS") {
                    MucOutlinedTextField(
                        value = outlinedValue,
                        onValueChange = setOutlinedValue,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "placeholder",
                        type = MucTextFieldType.NEUTRAL,
                        leadingIcon = Icons.Outlined.Search,
                        trailingIcon = Icons.Outlined.Close,
                        onTrailingIconClick = { setOutlinedValue("") },
                    )

                    MucOutlinedTextField(
                        value = "delete /system",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Danger",
                        type = MucTextFieldType.DANGER,
                        supportingText = "supportingText",
                        readOnly = true,
                    )

                    MucOutlinedTextField(
                        value = "com.example.app",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Success",
                        type = MucTextFieldType.SUCCESS,
                        supportingText = "supportingText",
                        enabled = false,
                    )
                }

                PreviewBlock(title = "Filled / size") {
                    MucFilledTextField(
                        value = filledValue,
                        onValueChange = setFilledValue,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Large",
                        size = MucTextFieldSize.LARGE,
                        leadingIcon = Icons.Outlined.Search,
                        cornerRadius = MucCornerRadius.LARGE,
                    )

                    MucFilledTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Medium",
                        size = MucTextFieldSize.MEDIUM,
                        cornerRadius = MucCornerRadius.MEDIUM,
                    )

                    MucFilledTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Small",
                        size = MucTextFieldSize.SMALL,
                        cornerRadius = MucCornerRadius.SMALL,
                    )
                }

                PreviewBlock(title = "Plain") {
                    MucPlainTextField(
                        value = "readonly value",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Plain field",
                        type = MucTextFieldType.NEUTRAL,
                        readOnly = true,
                        trailingIcon = Icons.Outlined.Close,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MucTextFieldsLightPreview() {
    MucTextFieldsPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
private fun MucTextFieldsDarkPreview() {
    MucTextFieldsPreviewContent(isDarkTheme = true)
}
