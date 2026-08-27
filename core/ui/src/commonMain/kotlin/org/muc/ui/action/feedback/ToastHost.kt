package org.muc.ui.action.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MoldTheme

@Composable
internal fun ToastHost(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = BiasAlignment(0f, .7f),
    content: @Composable (ToastData) -> Unit = { ToastItem(it) }
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = contentAlignment
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FeedbackManager.toasts.forEach { toast ->
                key(toast) {
                    LaunchedEffect(toast) { FeedbackManager.removeToastAfterDelay(toast) }
                    content(toast)
                }
            }
        }
    }
}

@Composable
internal fun ToastItem(data: ToastData) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { it })
    ) {
        Surface(
            shape = MoldTheme.shapes.medium,
            color = when (data.type) {
                FeedBackType.SUCCESS -> MoldTheme.semanticColors.success
                FeedBackType.ERROR -> MoldTheme.semanticColors.error
                FeedBackType.WARNING -> MoldTheme.semanticColors.warning
                FeedBackType.INFO -> MoldTheme.semanticColors.info
                FeedBackType.PRIMARY -> MoldTheme.colorScheme.primary
            },
            shadowElevation = 6.dp,
            modifier = Modifier.padding(Dimensions.paddingDefault)
        ) {
            Text(
                text = data.message,
                color = Color.White,
                modifier = Modifier.padding(horizontal = Dimensions.paddingDefault, vertical = Dimensions.paddingSmall)
            )
        }
    }
}