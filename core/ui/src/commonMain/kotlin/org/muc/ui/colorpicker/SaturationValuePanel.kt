package org.muc.ui.colorpicker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SaturationValuePanel(
    state: ColorPickerState,
    onStateChange: (ColorPickerState) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.hue) {
                    // 处理点击手势
                    detectTapGestures { offset ->
                        val sat = (offset.x / size.width).coerceIn(0f, 1f)
                        val valPrice = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                        onStateChange(state.copy(saturation = sat, value = valPrice))
                    }
                }
                .pointerInput(state.hue) {
                    // 处理拖动手势
                    detectDragGestures { change, _ ->
                        val offset = change.position
                        val sat = (offset.x / size.width).coerceIn(0f, 1f)
                        val valPrice = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                        onStateChange(state.copy(saturation = sat, value = valPrice))
                    }
                }
        ) {
            // 1. 基础色相背景
            val baseColor = Color.hsv(state.hue, 1f, 1f)

            // 2. 水平方向：从白到基础色
            val shadowBrushHorizontal = Brush.horizontalGradient(
                colors = listOf(Color.White, baseColor)
            )
            drawRect(brush = shadowBrushHorizontal)

            // 3. 垂直方向：从透明到纯黑
            val shadowBrushVertical = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black)
            )
            drawRect(brush = shadowBrushVertical)

            // 4. 绘制图片中的选择圆圈 (小白色圆环)
            val circleX = state.saturation * size.width
            val circleY = (1f - state.value) * size.height

            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(circleX, circleY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}