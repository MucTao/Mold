package org.muc.ui.floatdrag

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun BoxScope.DraggableContent(
    alignment: Alignment = Alignment.Center,
    snapToEdgeTime: Duration = 15.seconds,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.(Boolean) -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    var position by remember { mutableStateOf<IntOffset?>(null) }
    var iconSize by remember { mutableStateOf(IntSize.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .matchParentSize()
    ) {
        val rootSize = IntSize(constraints.maxWidth, constraints.maxHeight)

        // 初始定位
        LaunchedEffect(iconSize, rootSize, alignment) {
            if (position == null && iconSize != IntSize.Zero && rootSize != IntSize.Zero) {
                position = alignment.align(iconSize, rootSize, layoutDirection)
            }
        }

        // 自动吸附定时器（拖拽时自动取消，拖拽结束重新计时）
        LaunchedEffect(isDragging, snapToEdgeTime) {
            if (!isDragging && snapToEdgeTime.inWholeMilliseconds >= 0) {
                if (iconSize != IntSize.Zero && rootSize.width > 0) {
                    delay(snapToEdgeTime)
                    val pos = position ?: return@LaunchedEffect
                    val centerX = pos.x + iconSize.width / 2f
                    val halfWidth = rootSize.width / 2f
                    val snappedX = if (centerX <= halfWidth) 0
                    else (rootSize.width - iconSize.width).coerceAtLeast(0)
                    position = IntOffset(snappedX, pos.y)
                }
            }
        }

        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
                .offset {
                    position ?: IntOffset.Zero
                }
                .onGloballyPositioned { coordinates ->
                    val newSize = coordinates.size
                    if (iconSize != newSize) iconSize = newSize
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentPos = position ?: return@detectDragGestures
                            val maxX = (rootSize.width - iconSize.width).coerceAtLeast(0)
                            val maxY = (rootSize.height - iconSize.height).coerceAtLeast(0)
                            position = IntOffset(
                                x = (currentPos.x.toFloat() + dragAmount.x)
                                    .roundToInt()
                                    .coerceIn(0, maxX),
                                y = (currentPos.y.toFloat() + dragAmount.y)
                                    .roundToInt()
                                    .coerceIn(0, maxY)
                            )
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { if (!isDragging) onClick() }
                    )
                },
            Alignment.Center
        ) {
            content(isDragging)
        }
    }
}