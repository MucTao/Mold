package org.muc.ui.snapshot

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.encodeToBytes(quality: Int): ByteArray {
    val skiaBitmap = this.asSkiaBitmap()
    val skiaImage = Image.makeFromBitmap(skiaBitmap)
    val encodedData = skiaImage.encodeToData(EncodedImageFormat.PNG, quality)
        ?: throw IllegalStateException("Failed to encode image")
    val bytes = encodedData.bytes
    return bytes
}