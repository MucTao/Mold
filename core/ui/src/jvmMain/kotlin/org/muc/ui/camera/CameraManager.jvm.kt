package org.muc.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual fun createCameraManager(): CameraManager = DesktopCameraManager()

/** Desktop has no portable camera API in the UI module; callers receive an explicit failure. */
private class DesktopCameraManager : CameraManager {
    @Composable
    override fun Preview(modifier: Modifier) = Unit

    override suspend fun takePhoto(onFail: (String) -> Unit): String? {
        onFail("桌面平台暂不支持相机拍照")
        return null
    }

    override suspend fun startRecording(onFail: (String) -> Unit, onSuccess: (suspend () -> String) -> Unit) {
        onFail("桌面平台暂不支持相机录像")
    }

    override fun stopRecording() = Unit
    override fun release() = Unit
}
