package org.muc.ui.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import coil3.Image
import coil3.toBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image as SkiaImage

actual fun Image.toImageBitmap(): ImageBitmap? = SkiaImage.makeFromBitmap(this.toBitmap())
    .encodeToData(EncodedImageFormat.PNG)?.bytes?.decodeToImageBitmap()
