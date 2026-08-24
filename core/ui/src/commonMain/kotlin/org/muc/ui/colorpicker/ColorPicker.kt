package org.muc.ui.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.jetbrains.compose.resources.stringResource
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldFilledButton
import org.muc.ui.buttons.MoldOutlinedButton
import org.muc.ui.design.Dimensions
import org.muc.ui.i18n.MoldCommonStringRes

@Composable
fun ColorPickerPopup(
    initialColor: Color = Color(0xFF508F85),
    onColorConfirmed: (Color) -> Unit,
    supportAlpha: Boolean = false,
    onCancel: () -> Unit
) {
    Popup(
        alignment = Alignment.BottomCenter,
        offset = IntOffset(x = 0, y = 0),
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        onDismissRequest = onCancel,
    ) {
        ColorPicker(initialColor, onColorConfirmed, supportAlpha, onCancel)
    }
}

@Composable
fun ColorPicker(
    initialColor: Color = Color(0xFF508F85),
    onColorConfirmed: (Color) -> Unit,
    supportAlpha: Boolean = false,
    onCancel: () -> Unit
) {

    // 初始化状态转换
    val hsv = initialColor.toHsv()
    var state by remember {
        mutableStateOf(
            ColorPickerState(
                hue = hsv[0],
                saturation = hsv[1],
                value = hsv[2],
                alpha = initialColor.alpha,
                supportAlpha = supportAlpha
            )
        )
    }

    Column(
        modifier = Modifier
            .width(280.dp)
            .padding(bottom = Dimensions.paddingDefault)
    ) {
        // 1. 顶部大颜色面板
        SaturationValuePanel(
            state = state,
            onStateChange = { state = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 中间控制区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 2. 左侧的当前颜色圆形预览
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(state.toColor(), CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 右侧包含两个 Slider 的控制列
            Column(modifier = Modifier.weight(1f)) {
                // Hue 彩虹滑块
                val hueBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Red, Color.Yellow, Color.Green,
                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                    )
                )
                ColorSliderBar(
                    value = state.hue,
                    onValueChange = { state = state.copy(hue = it) },
                    valueRange = 0f..360f,
                    backgroundBrush = hueBrush
                )

                Spacer(modifier = Modifier.height(8.dp))
                if (state.supportAlpha) {
                    // Alpha 透明度滑块（从透明到当前纯色）
                    val alphaBrush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.hsv(state.hue, state.saturation, state.value))
                    )
                    ColorSliderBar(
                        value = state.alpha,
                        onValueChange = { state = state.copy(alpha = it) },
                        valueRange = 0f..1f,
                        backgroundBrush = alphaBrush
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. HEX 文本输入/显示框
        HexColorTextField(
            state = state,
            onStateChange = { state = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. 底部按钮：确认与取消
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoldFilledButton(
                onClick = { onColorConfirmed(state.toColor()) },
                text = stringResource(MoldCommonStringRes.actionConfirm),
                size = MoldButtonSize.SMALL
            )

            MoldOutlinedButton(
                onClick = onCancel,
                text = stringResource(MoldCommonStringRes.actionCancel),
                size = MoldButtonSize.SMALL
            )
        }
    }
}