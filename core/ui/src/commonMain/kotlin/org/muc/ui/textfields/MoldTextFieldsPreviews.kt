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
import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.design.MoldTheme

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
private fun MoldTextFieldsPreviewContent(isDarkTheme: Boolean) {
    MoldTheme(isDarkTheme = isDarkTheme) {
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
                    MoldOutlinedTextField(
                        value = outlinedValue,
                        onValueChange = setOutlinedValue,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "placeholder",
                        type = MoldTextFieldType.NEUTRAL,
                        leadingIcon = Icons.Outlined.Search,
                        trailingIcon = Icons.Outlined.Close,
                        onTrailingIconClick = { setOutlinedValue("") },
                    )

                    MoldOutlinedTextField(
                        value = "delete /system",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Danger",
                        type = MoldTextFieldType.DANGER,
                        supportingText = "supportingText",
                        readOnly = true,
                    )

                    MoldOutlinedTextField(
                        value = "com.example.app",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Success",
                        type = MoldTextFieldType.SUCCESS,
                        supportingText = "supportingText",
                        enabled = false,
                    )
                }

                PreviewBlock(title = "Filled / size") {
                    MoldFilledTextField(
                        value = filledValue,
                        onValueChange = setFilledValue,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Large",
                        size = MoldTextFieldSize.LARGE,
                        leadingIcon = Icons.Outlined.Search,
                        cornerRadius = MoldCornerRadius.LARGE,
                    )

                    MoldFilledTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Medium",
                        size = MoldTextFieldSize.MEDIUM,
                        cornerRadius = MoldCornerRadius.MEDIUM,
                    )

                    MoldFilledTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Small",
                        size = MoldTextFieldSize.SMALL,
                        cornerRadius = MoldCornerRadius.SMALL,
                    )
                }

                PreviewBlock(title = "Plain") {
                    MoldPlainTextField(
                        value = "readonly value",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Plain field",
                        type = MoldTextFieldType.NEUTRAL,
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
private fun MoldTextFieldsLightPreview() {
    MoldTextFieldsPreviewContent(isDarkTheme = false)
}

@Preview
@Composable
private fun MoldTextFieldsDarkPreview() {
    MoldTextFieldsPreviewContent(isDarkTheme = true)
}
