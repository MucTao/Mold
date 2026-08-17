package org.muc.ui.tooltiparea

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun MucTooltipArea(text: String, modifier: Modifier, content: @Composable (() -> Unit)) {
    TooltipArea(
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(4.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.padding(6.dp, 4.dp)
                )
            }
        },
        modifier = modifier,
        delayMillis = 500,
        content = content
    )
}