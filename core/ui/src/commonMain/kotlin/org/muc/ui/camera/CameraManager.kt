package org.muc.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface CameraManager {
    /** 打开相机预览，返回一个可嵌入 Compose 的 UI */
    @Composable
    fun Preview(modifier: Modifier = Modifier)
    suspend fun takePhoto(onFail: (String) -> Unit): String?
    suspend fun startRecording(onFail: (String) -> Unit, onSuccess: (suspend ()-> String) -> Unit)
     fun stopRecording()
    fun release()
}

// 工厂函数
expect fun createCameraManager(): CameraManager