package org.muc.mold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.muc.ui.buttons.MoldFilledButton
import org.muc.ui.design.MoldBlue
import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.design.MoldGreen
import org.muc.ui.design.MoldTheme
import org.muc.ui.floatdrag.DraggableContent

val space = Arrangement.spacedBy(8.dp)

@Composable
@Preview
fun App() {
    MoldTheme(MoldGreen) {
        val viewModel: MainViewModel = viewModel()
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp, 30.dp)
                    .background(Color.White)
            ) {
                items(viewModel.uiState, key = { it.title }) {
                    Text(text = it.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.size(6.dp))
                    Row(Modifier.fillMaxWidth(), space, Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), space) {
                            it.items.forEach { sub ->
                                Row(Modifier, Arrangement.spacedBy(2.dp), Alignment.CenterVertically) {
                                    if (sub.onClick != null) {
                                        Button(sub.onClick) {
                                            Text(text = sub.title, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    } else
                                        Text(text = sub.title, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.weight(1f))
                                    sub.ext?.invoke()
                                }
                            }
                        }
                        it.ext?.invoke()
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
            DraggableContent(BiasAlignment(0.9f, 0.9f)) {
                MoldFilledButton({}, text = "悬浮拖动按钮", cornerRadius = MoldCornerRadius.CIRCLE)
            }
        }
    }
}