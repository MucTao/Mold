package org.muc.ui.snapshot

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.encodeToBytes(quality: Int): ByteArray {
    // 1. 将 Compose 的 ImageBitmap 转换为 Android 的 Bitmap
    val androidBitmap = this.asAndroidBitmap()

    // 2. 使用 Android 原生 API 压缩为 PNG 字节数组
    return ByteArrayOutputStream().use { stream ->
        androidBitmap.compress(Bitmap.CompressFormat.PNG, quality, stream)
        stream.toByteArray()
    }
}