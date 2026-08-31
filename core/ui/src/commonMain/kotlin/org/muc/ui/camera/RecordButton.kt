package org.muc.ui.camera

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.muc.ui.design.MoldTheme
import kotlin.time.Clock

@Composable
fun RecordButton(
    onTakePhoto: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier,
    enabledRecord: Boolean = true,
    longPressThreshold: Long = 300L,       // 长按判定阈值 ms
    maxRecordDuration: Long = 60_000L,     // 最大录制时长 ms
) {
    var isRecording by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableLongStateOf(0L) }

    // 录制进度动画
    val animatedProgress by animateFloatAsState(
        targetValue = if (isRecording) 1f else 0f,
        animationSpec = tween(
            durationMillis = maxRecordDuration.toInt(),
            easing = LinearEasing
        ),
        finishedListener = {
            // 达到最大时长自动停止
            if (isRecording) {
                isRecording = false
                onStopRecording()
            }
        }
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .pointerInput(longPressThreshold) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    pressStartTime = Clock.System.now().toEpochMilliseconds()

                    // 等待松手或超时
                    val longPressTimeout = withTimeoutOrNull(longPressThreshold) {
                        // 在阈值内松手 → 不是长按
                        waitForUpOrCancellation()
                    }

                    if (longPressTimeout == null) {
                        if (enabledRecord) {
                            // ⏱️ 超过阈值未松手 → 开始录制
                            if (!isRecording) {
                                isRecording = true
                                onStartRecording()
                            }
                            // 继续等待松手
                            waitForUpOrCancellation()
                            isRecording = false
                            onStopRecording()
                        }
                    } else {
                        // 👆 阈值内松手 → 拍照
                        onTakePhoto()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 外圈进度环
        if (isRecording) {
            val color = MoldTheme.colorScheme.primary
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth / 2
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // 内圈按钮
        Box(
            Modifier
                .size(if (isRecording) 50.dp else 60.dp)
                .background(Color.White, CircleShape)
                .animateContentSize()
        )
    }
}