package org.muc.ui.snapshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64

class CaptureController(internal val graphicsLayer: GraphicsLayer, internal val scope: CoroutineScope) {
    var isRunning by mutableStateOf(false)

    // 用于标记当前是否正处于 capture 触发的录制帧中
    internal var isCapturingFrame by mutableStateOf(false)

    /**
     * 捕获当前组件状态并返回 ImageBitmap
     */
    fun capture(onSuccess: (ImageBitmap) -> Unit) {
        isRunning = true
        isCapturingFrame = true // 标记开始
        scope.launch {
            withFrameNanos { }
            val toImageBitmap = graphicsLayer.toImageBitmap()
            isRunning = false
            onSuccess(toImageBitmap)
        }
    }
}

@Composable
fun rememberCaptureController(): CaptureController {
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    return remember(graphicsLayer) { CaptureController(graphicsLayer, scope) }
}

/**
 * 核心封装：将组件绑定到控制器
 */
fun Modifier.capture(controller: CaptureController, onDrawRecord: (() -> Unit)? = null): Modifier = this.then(
    Modifier.drawWithContent {
        if (controller.isCapturingFrame) {
            // 触发回调：通知外部现在正在为截图进行绘制录制
            onDrawRecord?.invoke()
            // 恢复标记，确保只触发一次
            controller.isCapturingFrame = false
        }
        // 录制到 GraphicsLayer
        controller.graphicsLayer.record {
            this@drawWithContent.drawContent()
        }
        // 正常绘制到屏幕
        drawContent()
    }
)

expect fun ImageBitmap.encodeToBytes(quality: Int = 100): ByteArray


fun ImageBitmap.toBase64(quality: Int = 100): String {
    val bytes = encodeToBytes(quality)
    return Base64.encode(bytes)
}
