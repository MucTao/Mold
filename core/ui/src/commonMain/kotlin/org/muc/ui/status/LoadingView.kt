package org.muc.ui.status

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.muc.ui.design.Dimensions

/**
 * Экран загрузки — отображается, пока данные еще загружаются.
 *
 * @param message Текстовое сообщение под индикатором (可选).
 * @param modifier Модификатор контейнера.
 */
@Composable
fun LoadingView(
    message: String = "加载中…",
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
            )
            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
