package org.muc.ui.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    // 1. 关键：创建状态并跳过半展开，直接全屏
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // 强制全屏，无中间态
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide() // 协程关闭
                onDismiss()
            }
        },
        // 2. 修饰符：占满屏幕
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        // 3. 形状：矩形（无圆角）
        shape = RectangleShape,
        // 4. 背景色（可选）
        containerColor = BottomSheetDefaults.ContainerColor,
        // 5. 隐藏默认拖拽条（可选）
        dragHandle = null,
        // 6. 关键：禁用系统内边距（占满状态栏/导航栏）
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        // 7. 全屏遮罩（可选）
        scrimColor = BottomSheetDefaults.ScrimColor
    ) {
        // 内容区：添加状态栏内边距避免被遮挡
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}