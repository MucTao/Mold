package org.muc.ui.tooltiparea

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MucTooltipArea(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit)