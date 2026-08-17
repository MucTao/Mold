package org.muc.ui.colorpicker

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HexColorTextField(
    state: ColorPickerState,
    onStateChange: (ColorPickerState) -> Unit,
    modifier: Modifier = Modifier
) {
    // 内部维护一个 String 状态，方便用户自由输入
    var textValue by remember { mutableStateOf(state.toHexStr()) }
    val focusManager = LocalFocusManager.current

    // 当外部面板滑动导致颜色变化时，同步更新输入框文字（仅在未获取焦点时同步，避免打断用户输入）
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        if (!isFocused) {
            textValue = state.toHexStr()
        }
    }
    val enabledLength = if (state.supportAlpha) {
        listOf(5, 9)
    } else {
        listOf(4, 7) // #RRGGBB
    }
    val maxLength = if (state.supportAlpha) {
        9 // #AARRGGBB
    } else {
        7 // #RRGGBB
    }
    // 解析并提交解析结果的函数
    val submitHex = {
        val cleanHex = textValue.replace("#", "").trim()
        runCatching {
            // 支持 6 位 (RRGGBB) 或 8 位 (AARRGGBB)
            val parsedColor = when (cleanHex.length) {
                3 -> {
                    // 3位颜色格式（如 #F0F）转6位：每个字符重复一次 -> FF00FF
                    val sixDigitHex = buildString {
                        cleanHex.forEach { char ->
                            append(char)
                            append(char)
                        }
                    }
                    // 补全不透明度（FF），转为Long后解析
                    Color(sixDigitHex.toLong(16) or 0xFF000000L)
                }

                4 -> {
                    // #ARGB → #AARRGGBB，每一位重复
                    val eightDigitHex = buildString {
                        cleanHex.forEach { char ->
                            append(char)
                            append(char)
                        }
                    }
                    if (state.supportAlpha)
                        Color(eightDigitHex.toLong(16))
                    else
                        Color(eightDigitHex.toLong(16) or 0xFF000000L)
                }

                6 -> Color(cleanHex.toLong(16) or 0xFF000000L) // 补全不透明度
                8 -> if (state.supportAlpha) Color(cleanHex.toLong(16)) else Color(cleanHex.toLong(16) or 0xFF000000L)
                else -> throw IllegalArgumentException()
            }

            // 将解析出的 Color 转换回 HSV 状态
            val hsv = parsedColor.toHsv()
            onStateChange(
                ColorPickerState(
                    hue = hsv[0],
                    saturation = hsv[1],
                    value = hsv[2],
                    alpha = parsedColor.alpha
                )
            )
        }.getOrElse {
            // 如果解析失败（用户胡乱输入），失焦时重置回当前正确的值
            textValue = state.toHexStr()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = textValue,
            onValueChange = { input ->
                // 过滤输入：必须以 # 开头，后面只能是 0-9, a-f, A-F，且总长度不超过 9 位 (# + 8位ARGB)
                val upperInput = input.uppercase()
                if (upperInput.startsWith("#") && upperInput.length <= maxLength) {
                    val remains = upperInput.substring(1)
                    if (remains.all { it in '0'..'9' || it in 'A'..'F' }) {
                        textValue = upperInput
                    }
                } else if (!upperInput.startsWith("#") && upperInput.length <= (maxLength - 1)) {
                    // 容错：如果用户删除了 #，自动补上
                    if (upperInput.all { it in '0'..'9' || it in 'A'..'F' }) {
                        textValue = "#$upperInput"
                    }
                }
                if (input.length in enabledLength) {
                    submitHex()
                }
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color(0xFF2196F3)),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done // 键盘右下角显示“完成”
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    submitHex()
                    focusManager.clearFocus() // 收起键盘并失焦
                }
            ),
            modifier = Modifier
                .width(180.dp)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (!focusState.isFocused) {
                        // 失去焦点时（比如点击了别的地方），提交并检查输入
                        submitHex()
                    }
                }
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "HEX", color = Color.Gray, fontSize = 11.sp)
    }
}