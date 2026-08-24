package org.muc.ui.textfields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.muc.ui.design.MoldCornerRadius

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suggestions: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) {
        MoldOutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = placeholder,
            type = MoldTextFieldType.NEUTRAL,
            size = MoldTextFieldSize.MEDIUM,
            cornerRadius = MoldCornerRadius.LARGE,
            leadingIcon = null,
            trailingIcon = if (value.isNotEmpty()) Icons.Outlined.Close else null,
            onTrailingIconClick = if (value.isNotEmpty()) {
                { onValueChange("") }
            } else {
                null
            },
            singleLine = true,
        )
    } else {
        MoldOutlinedAutocompleteTextField(
            value = value,
            onValueChange = onValueChange,
            suggestions = suggestions.map { MoldDropdownOption(it, it) },
            onSuggestionSelected = onValueChange,
            modifier = modifier,
            placeholder = placeholder,
            type = MoldTextFieldType.NEUTRAL,
            size = MoldTextFieldSize.MEDIUM,
            cornerRadius = MoldCornerRadius.LARGE,
            trailingIcon = if (value.isNotEmpty()) Icons.Outlined.Close else null,
            onTrailingIconClick = if (value.isNotEmpty()) {
                { onValueChange("") }
            } else {
                null
            },
            maxVisibleSuggestions = 40,
        )
    }
}

@Composable
fun CompactTreeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suggestions: List<MoldTreeDropdownOption<String>> = emptyList(),
    onlyLeaf: Boolean = true,
    modifier: Modifier = Modifier,
) {
    MoldOutlinedAutocompleteTreeTextField(
        value = value,
        onValueChange = onValueChange,
        suggestions = suggestions,
        onSuggestionSelected = onValueChange,
        onlyLeaf = onlyLeaf,
        modifier = modifier,
        placeholder = placeholder,
        type = MoldTextFieldType.NEUTRAL,
        size = MoldTextFieldSize.MEDIUM,
        cornerRadius = MoldCornerRadius.LARGE,
        trailingIcon = if (value.isNotEmpty()) Icons.Outlined.Close else null,
        onTrailingIconClick = if (value.isNotEmpty()) {
            { onValueChange("") }
        } else {
            null
        },
        maxVisibleSuggestions = 40,
    )
}