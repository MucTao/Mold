@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.Manifest
import android.content.ContentValues
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import org.muc.mold.utils.util.ConvertUtils.toHex
import org.muc.mold.utils.util.UriUtils.toFile
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/12
 * desc  : utils about image
 * </pre> *
 */
object ImageUtils {
    enum class ImageType(val value: String) {
        TYPE_JPG("jpg"),
        TYPE_PNG("png"),
        TYPE_GIF("gif"),
        TYPE_TIFF("tiff"),
        TYPE_BMP("bmp"),
        TYPE_WEBP("webp"),
        TYPE_ICO("ico"),
        TYPE_UNKNOWN("unknown");
    }


    /**
     * Return bitmap.
     *
     * @param file The file.
     * @return bitmap
     */
    fun getBitmap(file: File?): Bitmap? {
        if (file == null) return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    /**
     * Return bitmap.
     *
     * @param file The file.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return bitmap
     */
    fun getBitmap(file: File?, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (file == null) return null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)
        options.inSampleSize =
            calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    /**
     * Return bitmap.
     *
     * @param filePath The path of file.
     * @return bitmap
     */
    fun getBitmap(filePath: String?): Bitmap? {
        if (filePath.isNullOrBlank()) return null
        return BitmapFactory.decodeFile(filePath)
    }

    /**
     * Return bitmap.
     *
     * @param filePath The path of file.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return bitmap
     */
    fun getBitmap(filePath: String?, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (filePath.isNullOrBlank()) return null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    /**
     * Return bitmap.
     *
     * @param is The input stream.
     * @return bitmap
     */
    fun getBitmap(`is`: InputStream?): Bitmap? {
        if (`is` == null) return null
        return BitmapFactory.decodeStream(`is`)
    }

    /**
     * Return bitmap.
     *
     * @param is The input stream.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return bitmap
     */
    fun getBitmap(`is`: InputStream?, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (`is` == null) return null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(`is`, null, options)
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeStream(`is`, null, options)
    }

    /**
     * Return bitmap.
     *
     * @param data The data.
     * @param offset The offset.
     * @return bitmap
     */
    fun getBitmap(data: ByteArray, offset: Int): Bitmap? {
        if (data.isEmpty()) return null
        return BitmapFactory.decodeByteArray(data, offset, data.size)
    }

    /**
     * Return bitmap.
     *
     * @param data The data.
     * @param offset The offset.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return bitmap
     */
    fun getBitmap(
        data: ByteArray,
        offset: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): Bitmap? {
        if (data.isEmpty()) return null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, offset, data.size, options)
        options.inSampleSize =
            calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(data, offset, data.size, options)
    }

    /**
     * Return bitmap.
     *
     * @param resId The resource id.
     * @return bitmap
     */
    fun getBitmap(@DrawableRes resId: Int): Bitmap? {
        val drawable: Drawable = ContextCompat.getDrawable(Utils.app, resId) ?: return null
        val canvas = Canvas()
        val bitmap =
            createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Return bitmap.
     *
     * @param resId The resource id.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return bitmap
     */
    fun getBitmap(
        @DrawableRes resId: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): Bitmap {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        val resources: Resources? = Utils.app.resources
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, resId, options)
        options.inSampleSize =
            calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    /**
     * Return bitmap.
     *
     * @param fd The file descriptor.
     * @return bitmap
     */
    fun getBitmap(fd: FileDescriptor?): Bitmap? {
        if (fd == null) return null
        return BitmapFactory.decodeFileDescriptor(fd)
    }

    /**
     * Return bitmap.
     *
     * @param fd The file descriptor
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return bitmap
     */
    fun getBitmap(
        fd: FileDescriptor?,
        maxWidth: Int,
        maxHeight: Int,
    ): Bitmap? {
        if (fd == null) return null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFileDescriptor(fd, null, options)
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFileDescriptor(fd, null, options)
    }

    /**
     * Return the bitmap with the specified color.
     *
     * @param src The source of bitmap.
     * @param color The color.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the bitmap with the specified color
     */
    fun drawColor(
        src: Bitmap,
        @ColorInt color: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (isEmptyBitmap(src)) return null
        val ret: Bitmap = if (recycle) src else src.getConfig()?.let { src.copy(it, true) } ?: return null
        val canvas = Canvas(ret)
        canvas.drawColor(color, PorterDuff.Mode.DARKEN)
        return ret
    }

    /**
     * Return the scaled bitmap.
     *
     * @param src The source of bitmap.
     * @param newWidth The new width.
     * @param newHeight The new height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the scaled bitmap
     */
    fun scale(
        src: Bitmap?,
        newWidth: Int,
        newHeight: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val ret: Bitmap = Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the scaled bitmap
     *
     * @param src The source of bitmap.
     * @param scaleWidth The scale of width.
     * @param scaleHeight The scale of height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the scaled bitmap
     */
    fun scale(
        src: Bitmap?,
        scaleWidth: Float,
        scaleHeight: Float,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val matrix = Matrix()
        matrix.setScale(scaleWidth, scaleHeight)
        val ret: Bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true)
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the clipped bitmap.
     *
     * @param src The source of bitmap.
     * @param x The x coordinate of the first pixel.
     * @param y The y coordinate of the first pixel.
     * @param width The width.
     * @param height The height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the clipped bitmap
     */
    fun clip(
        src: Bitmap?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val ret: Bitmap = Bitmap.createBitmap(src, x, y, width, height)
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the skewed bitmap.
     *
     * @param src The source of bitmap.
     * @param kx The skew factor of x.
     * @param ky The skew factor of y.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the skewed bitmap
     */
    fun skew(
        src: Bitmap?,
        kx: Float,
        ky: Float,
        recycle: Boolean,
    ): Bitmap? {
        return skew(src, kx, ky, 0f, 0f, recycle)
    }

    /**
     * Return the skewed bitmap.
     *
     * @param src The source of bitmap.
     * @param kx The skew factor of x.
     * @param ky The skew factor of y.
     * @param px The x coordinate of the pivot point.
     * @param py The y coordinate of the pivot point.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the skewed bitmap
     */
    fun skew(
        src: Bitmap?,
        kx: Float,
        ky: Float,
        px: Float = 0f,
        py: Float = 0f,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val matrix = Matrix()
        matrix.setSkew(kx, ky, px, py)
        val ret: Bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true)
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the rotated bitmap.
     *
     * @param src The source of bitmap.
     * @param degrees The number of degrees.
     * @param px The x coordinate of the pivot point.
     * @param py The y coordinate of the pivot point.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the rotated bitmap
     */
    fun rotate(
        src: Bitmap?,
        degrees: Float,
        px: Float,
        py: Float,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        if (degrees == 0f) return src
        val matrix = Matrix()
        matrix.setRotate(degrees, px, py)
        val ret: Bitmap =
            Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true)
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the rotated degree.
     *
     * @param filePath The path of file.
     * @return the rotated degree
     */
    fun getRotateDegree(filePath: String?): Int {
        return runCatching {
            if (filePath.isNullOrBlank()) return 0
            val exifInterface = ExifInterface(filePath)
            val orientation: Int =
                exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return@runCatching 90
                ExifInterface.ORIENTATION_ROTATE_180 -> return@runCatching 180
                ExifInterface.ORIENTATION_ROTATE_270 -> return@runCatching 270
                else -> return@runCatching 0
            }
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrDefault(-1)
    }

    /**
     * Return the round bitmap.
     *
     * @param src The source of bitmap.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the round bitmap
     */
    fun toRound(src: Bitmap?, recycle: Boolean): Bitmap? {
        return toRound(src, 0, 0, recycle)
    }

    /**
     * Return the round bitmap.
     *
     * @param src The source of bitmap.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @param borderSize The size of border.
     * @param borderColor The color of border.
     * @return the round bitmap
     */
    fun toRound(
        src: Bitmap?,
        @IntRange(from = 0) borderSize: Int = 0,
        @ColorInt borderColor: Int = 0,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val width: Int = src.getWidth()
        val height: Int = src.getHeight()
        val size: Int = width.coerceAtMost(height)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val ret: Bitmap = src.getConfig()?.let { createBitmap(width, height, it) } ?: return null
        val center = size / 2f
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        rectF.inset((width - size) / 2f, (height - size) / 2f)
        val matrix = Matrix()
        matrix.setTranslate(rectF.left, rectF.top)
        if (width != height) {
            matrix.preScale(size.toFloat() / width, size.toFloat() / height)
        }
        val shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        shader.setLocalMatrix(matrix)
        paint.setShader(shader)
        val canvas = Canvas(ret)
        canvas.drawRoundRect(rectF, center, center, paint)
        if (borderSize > 0) {
            paint.setShader(null)
            paint.setColor(borderColor)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderSize.toFloat()
            val radius = center - borderSize / 2f
            canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        }
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the round corner bitmap.
     *
     * @param src The source of bitmap.
     * @param radius The radius of corner.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the round corner bitmap
     */
    fun toRoundCorner(
        src: Bitmap?,
        radius: Float,
        recycle: Boolean,
    ): Bitmap? {
        return toRoundCorner(src, radius, 0f, 0, recycle)
    }

    /**
     * Return the round corner bitmap.
     *
     * @param src The source of bitmap.
     * @param radius The radius of corner.
     * @param borderSize The size of border.
     * @param borderColor The color of border.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the round corner bitmap
     */
    fun toRoundCorner(
        src: Bitmap?,
        radius: Float,
        @FloatRange(from = 0.0) borderSize: Float = 0f,
        @ColorInt borderColor: Int = 0,
        recycle: Boolean = false,
    ): Bitmap? {
        val radii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        return toRoundCorner(
            src,
            radii,
            borderSize,
            borderColor,
            recycle,
        )
    }

    /**
     * Return the round corner bitmap.
     *
     * @param src The source of bitmap.
     * @param radii Array of 8 values, 4 pairs of [X,Y] radii
     * @param borderSize The size of border.
     * @param borderColor The color of border.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the round corner bitmap
     */
    fun toRoundCorner(
        src: Bitmap?,
        radii: FloatArray?,
        @FloatRange(from = 0.0) borderSize: Float,
        @ColorInt borderColor: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (radii == null) return null
        if (isEmptyBitmap(src)) return null
        val width: Int = src.getWidth()
        val height: Int = src.getHeight()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val ret: Bitmap = src.getConfig()?.let { createBitmap(width, height, it) } ?: return null
        val shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.setShader(shader)
        val canvas = Canvas(ret)
        val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val halfBorderSize = borderSize / 2f
        rectF.inset(halfBorderSize, halfBorderSize)
        val path = Path()
        path.addRoundRect(rectF, radii, Path.Direction.CW)
        canvas.drawPath(path, paint)
        if (borderSize > 0) {
            paint.setShader(null)
            paint.setColor(borderColor)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderSize
            paint.strokeCap = Paint.Cap.ROUND
            canvas.drawPath(path, paint)
        }
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the round corner bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @param cornerRadius The radius of corner.
     * @return the round corner bitmap with border
     */
    fun addCornerBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
        @FloatRange(from = 0.0) cornerRadius: Float,
    ): Bitmap? {
        return addBorder(
            src,
            borderSize,
            color,
            false,
            cornerRadius,
            false,
        )
    }

    /**
     * Return the round corner bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @param radii Array of 8 values, 4 pairs of [X,Y] radii
     * @return the round corner bitmap with border
     */
    fun addCornerBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
        radii: FloatArray?,
    ): Bitmap? {
        return addBorder(
            src,
            borderSize,
            color,
            false,
            radii,
            false,
        )
    }

    /**
     * Return the round corner bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @param radii Array of 8 values, 4 pairs of [X,Y] radii
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the round corner bitmap with border
     */
    fun addCornerBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
        radii: FloatArray?,
        recycle: Boolean,
    ): Bitmap? {
        return addBorder(
            src,
            borderSize,
            color,
            false,
            radii,
            recycle,
        )
    }

    /**
     * Return the round corner bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @param cornerRadius The radius of corner.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the round corner bitmap with border
     */
    fun addCornerBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
        @FloatRange(from = 0.0) cornerRadius: Float,
        recycle: Boolean,
    ): Bitmap? {
        return addBorder(
            src,
            borderSize,
            color,
            false,
            cornerRadius,
            recycle,
        )
    }

    /**
     * Return the round bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @return the round bitmap with border
     */
    fun addCircleBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
    ): Bitmap? {
        return addBorder(src, borderSize, color, true, 0f, false)
    }

    /**
     * Return the round bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the round bitmap with border
     */
    fun addCircleBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
        recycle: Boolean,
    ): Bitmap? {
        return addBorder(
            src,
            borderSize,
            color,
            true,
            0f,
            recycle,
        )
    }

    /**
     * Return the bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @param isCircle True to draw circle, false to draw corner.
     * @param cornerRadius The radius of corner.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the bitmap with border
     */
    private fun addBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
        isCircle: Boolean,
        cornerRadius: Float,
        recycle: Boolean,
    ): Bitmap? {
        val radii =
            floatArrayOf(
                cornerRadius,
                cornerRadius,
                cornerRadius,
                cornerRadius,
                cornerRadius,
                cornerRadius,
                cornerRadius,
                cornerRadius,
            )
        return addBorder(
            src,
            borderSize,
            color,
            isCircle,
            radii,
            recycle,
        )
    }

    /**
     * Return the bitmap with border.
     *
     * @param src The source of bitmap.
     * @param borderSize The size of border.
     * @param color The color of border.
     * @param isCircle True to draw circle, false to draw corner.
     * @param radii Array of 8 values, 4 pairs of [X,Y] radii
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the bitmap with border
     */
    private fun addBorder(
        src: Bitmap?,
        @FloatRange(from = 1.0) borderSize: Float,
        @ColorInt color: Int,
        isCircle: Boolean,
        radii: FloatArray?,
        recycle: Boolean,
    ): Bitmap? {
        if (src == null) return null

        if (isEmptyBitmap(src)) return null
        val ret: Bitmap = if (recycle) src else src.getConfig()?.let { src.copy(it, true) } ?: return null
        val width: Int = ret.getWidth()
        val height: Int = ret.getHeight()
        val canvas = Canvas(ret)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setColor(color)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderSize
        if (isCircle) {
            val radius: Float = width.coerceAtMost(height) / 2f - borderSize / 2f
            canvas.drawCircle(width / 2f, height / 2f, radius, paint)
        } else {
            val rectF = RectF(0F, 0F, width.toFloat(), height.toFloat())
            val halfBorderSize = borderSize / 2f
            rectF.inset(halfBorderSize, halfBorderSize)
            val path = Path()
            if (radii != null)
                path.addRoundRect(rectF, radii, Path.Direction.CW)
            canvas.drawPath(path, paint)
        }
        return ret
    }

    /**
     * Return the bitmap with reflection.
     *
     * @param src The source of bitmap.
     * @param reflectionHeight The height of reflection.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the bitmap with reflection
     */
    fun addReflection(
        src: Bitmap?,
        reflectionHeight: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val REFLECTION_GAP = 0
        val srcWidth: Int = src.getWidth()
        val srcHeight: Int = src.getHeight()
        val matrix = Matrix()
        matrix.preScale(1F, -1f)
        val reflectionBitmap: Bitmap =
            Bitmap.createBitmap(
                src,
                0,
                srcHeight - reflectionHeight,
                srcWidth,
                reflectionHeight,
                matrix,
                false,
            )
        val ret: Bitmap =
            src.getConfig()?.let { createBitmap(srcWidth, srcHeight + reflectionHeight, it) } ?: return null
        val canvas = Canvas(ret)
        canvas.drawBitmap(src, 0f, 0f, null)
        canvas.drawBitmap(reflectionBitmap, 0f, srcHeight.toFloat() + REFLECTION_GAP, null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val shader =
            LinearGradient(
                0f,
                srcHeight.toFloat(),
                0f,
                ret.getHeight().toFloat() + REFLECTION_GAP,
                0x70FFFFFF,
                0x00FFFFFF,
                Shader.TileMode.MIRROR,
            )
        paint.setShader(shader)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawRect(0f, srcHeight.toFloat() + REFLECTION_GAP, srcWidth.toFloat(), ret.getHeight().toFloat(), paint)
        if (!reflectionBitmap.isRecycled) reflectionBitmap.recycle()
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the bitmap with text watermarking.
     *
     * @param src The source of bitmap.
     * @param content The content of text.
     * @param textSize The size of text.
     * @param color The color of text.
     * @param x The x coordinate of the first pixel.
     * @param y The y coordinate of the first pixel.
     * @return the bitmap with text watermarking
     */
    fun addTextWatermark(
        src: Bitmap?,
        content: String?,
        textSize: Int,
        @ColorInt color: Int,
        x: Float,
        y: Float,
    ): Bitmap? {
        return addTextWatermark(
            src,
            content,
            textSize.toFloat(),
            color,
            x,
            y,
            false,
        )
    }

    /**
     * Return the bitmap with text watermarking.
     *
     * @param src The source of bitmap.
     * @param content The content of text.
     * @param textSize The size of text.
     * @param color The color of text.
     * @param x The x coordinate of the first pixel.
     * @param y The y coordinate of the first pixel.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the bitmap with text watermarking
     */
    fun addTextWatermark(
        src: Bitmap?,
        content: String?,
        textSize: Float,
        @ColorInt color: Int,
        x: Float,
        y: Float,
        recycle: Boolean,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src) || content == null) return null
        val ret: Bitmap = src.getConfig()?.let { src.copy(it, true) } ?: return null
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val canvas = Canvas(ret)
        paint.setColor(color)
        paint.textSize = textSize
        val bounds = Rect()
        paint.getTextBounds(content, 0, content.length, bounds)
        canvas.drawText(content, x, y + textSize, paint)
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the bitmap with image watermarking.
     *
     * @param src The source of bitmap.
     * @param watermark The image watermarking.
     * @param x The x coordinate of the first pixel.
     * @param y The y coordinate of the first pixel.
     * @param alpha The alpha of watermark.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the bitmap with image watermarking
     */
    fun addImageWatermark(
        src: Bitmap?,
        watermark: Bitmap?,
        x: Float,
        y: Float,
        alpha: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        if (watermark == null) return src
        val ret: Bitmap = src.getConfig()?.let { src.copy(it, true) } ?: return null
        if (!isEmptyBitmap(watermark)) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val canvas = Canvas(ret)
            paint.setAlpha(alpha)
            canvas.drawBitmap(watermark, x, y, paint)
        }
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the alpha bitmap.
     *
     * @param src The source of bitmap.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the alpha bitmap
     */
    fun toAlpha(
        src: Bitmap,
        recycle: Boolean = false,
    ): Bitmap? {
        if (isEmptyBitmap(src)) return null
        val ret: Bitmap = src.extractAlpha()
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the gray bitmap.
     *
     * @param src The source of bitmap.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the gray bitmap
     */
    fun toGray(
        src: Bitmap?,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val ret: Bitmap = src.getConfig()?.let { createBitmap(src.getWidth(), src.getHeight(), it) } ?: return null
        val canvas = Canvas(ret)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.setColorFilter(colorMatrixColorFilter)
        canvas.drawBitmap(src, 0f, 0f, paint)
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }

    /**
     * Return the blur bitmap fast.
     *
     * zoom out, blur, zoom in
     *
     * @param src The source of bitmap.
     * @param scale The scale(0...1).
     * @param radius The radius(0...25).
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @param isReturnScale True to return the scale blur bitmap, false otherwise.
     * @return the blur bitmap
     */
    fun fastBlur(
        src: Bitmap?,
        @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) scale: Float,
        @FloatRange(from = .0, to = 25.0, fromInclusive = false) radius: Float,
        recycle: Boolean = false,
        isReturnScale: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val width: Int = src.getWidth()
        val height: Int = src.getHeight()
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        val scaleBitmap: Bitmap =
            Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        val canvas = Canvas()
        val filter = PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP)
        paint.setColorFilter(filter)
        canvas.scale(scale, scale)
        canvas.drawBitmap(scaleBitmap, 0f, 0f, paint)
        val scaleBitmapR = renderScriptBlur(scaleBitmap, radius, recycle)
        if (scale == 1f || isReturnScale) {
            if (recycle && !src.isRecycled && scaleBitmapR != src) src.recycle()
            return scaleBitmapR
        }
        if (scaleBitmapR == null) return scaleBitmap
        val ret: Bitmap = Bitmap.createScaledBitmap(scaleBitmapR, width, height, true)
        if (!scaleBitmapR.isRecycled) scaleBitmapR.recycle()
        if (recycle && !src.isRecycled && ret != src) src.recycle()
        return ret
    }


    /**
     * Return the blur bitmap using render script.
     *
     * @param src The source of bitmap.
     * @param radius The radius(0...25).
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the blur bitmap
     */
    fun renderScriptBlur(
        src: Bitmap,
        @FloatRange(from = .0, to = 25.0, fromInclusive = false) radius: Float,
        recycle: Boolean = false,
    ): Bitmap? {
        var rs: RenderScript? = null
        val ret: Bitmap = if (recycle) src else src.getConfig()?.let { src.copy(it, true) } ?: return null
        try {
            rs = RenderScript.create(Utils.app)
            rs.messageHandler = RenderScript.RSMessageHandler()
            val input: Allocation =
                Allocation.createFromBitmap(
                    rs,
                    ret,
                    Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT,
                )
            val output: Allocation = Allocation.createTyped(rs, input.type)
            val blurScript: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            blurScript.setInput(input)
            blurScript.setRadius(radius)
            blurScript.forEach(output)
            output.copyTo(ret)
        } finally {
            rs?.destroy()
        }
        return ret
    }

    /**
     * Return the blur bitmap using stack.
     *
     * @param src The source of bitmap.
     * @param radius The radius(0...25).
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the blur bitmap
     */
    fun stackBlur(
        src: Bitmap,
        radius: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        var radius = radius
        val ret: Bitmap = if (recycle) src else src.getConfig()?.let { src.copy(it, true) } ?: return null
        if (radius < 1) {
            radius = 1
        }
        val w: Int = ret.getWidth()
        val h: Int = ret.getHeight()

        val pix = IntArray(w * h)
        ret.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = (i / divsum)
            i++
        }

        yi = 0
        yw = yi

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = (x + radius + 1).coerceAtMost(wm)
                }
                p = pix[yw + vmin[x]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[(stackpointer) % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x

                sir = stack[i + radius]

                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]

                rbs = r1 - Math.abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] =
                    (-0x1000000 and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }
        ret.setPixels(pix, 0, w, 0, 0, w, h)
        return ret
    }


    /**
     * Save the bitmap.
     *
     * @param src The source of bitmap.
     * @param filePath The path of file.
     * @param format The format of the image.
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
     *   compress for max quality. Some formats, like PNG which is lossless, will ignore the quality
     *   setting
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun save(
        src: Bitmap?,
        filePath: String?,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100,
        recycle: Boolean = false,
    ): Boolean {
        return save(src, FileUtils.getFileByPath(filePath), format, quality, recycle)
    }

    /**
     * Save the bitmap.
     *
     * @param src The source of bitmap.
     * @param file The file.
     * @param format The format of the image.
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
     *   compress for max quality. Some formats, like PNG which is lossless, will ignore the quality
     *   setting
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun save(
        src: Bitmap?,
        file: File?,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100,
        recycle: Boolean = false,
    ): Boolean {
        if (src == null) return false
        if (isEmptyBitmap(src)) {
            Log.e("ImageUtils", "bitmap is empty.")
            return false
        }
        if (src.isRecycled) {
            Log.e("ImageUtils", "bitmap is recycled.")
            return false
        }
        if (!FileUtils.createFileByDeleteOldFile(file)) {
            Log.e("ImageUtils", "create or delete file <$file> failed.")
            return false
        }

        var ret = false
        runCatching {
            BufferedOutputStream(FileOutputStream(file)).use { os ->
                ret = src.compress(format, quality, os)
                if (recycle && !src.isRecycled) src.recycle()
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
        return ret
    }


    /**
     * @param src The source of bitmap.
     * @param dirName The name of directory.
     * @param format The format of the image.
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning
     *   compress for max quality. Some formats, like PNG which is lossless, will ignore the quality
     *   setting
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the file if save success, otherwise return null.
     */
    suspend fun save2Album(
        src: Bitmap,
        dirName: String? = null,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100,
        recycle: Boolean = false,
    ): File? {
        val safeDirName = if (dirName.isNullOrBlank()) Utils.app.packageName else dirName
        val suffix: String? = if (Bitmap.CompressFormat.JPEG == format) "JPG" else format.name
        val fileName = "${System.currentTimeMillis()}_" + quality + "." + suffix
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.e("ImageUtils", "save to album need storage permission")
                return null
            }
            val picDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val destFile = File(picDir, "$safeDirName/$fileName")
            if (!save(src, destFile, format, quality, recycle)) {
                return null
            }
            FileUtils.notifySystemToScan(destFile)
            return destFile
        } else {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            val contentUri = if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            }
            contentValues.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_DCIM + "/" + safeDirName,
            )
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1)
            val uri: Uri = Utils.app.contentResolver.insert(contentUri, contentValues) ?: return null

            runCatching {
                Utils.app.contentResolver.openOutputStream(uri).use { os ->
                    if (os != null) {
                        src.compress(format, quality, os)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    Utils.app.contentResolver.update(uri, contentValues, null, null)
                    return uri.toFile().getOrElse {
                        Utils.app.contentResolver.delete(uri, null, null)
                        null
                    }
                }
            }.getOrElse { e ->
                Utils.app.contentResolver.delete(uri, null, null)
                e.printStackTrace()
                return null
            }
        }
    }

    /**
     * Return whether it is a image according to the file name.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isImage(file: File?): Boolean {
        if (file == null || !file.exists()) {
            return false
        }
        return isImage(file.getPath())
    }

    /**
     * Return whether it is a image according to the file name.
     *
     * @param filePath The path of file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isImage(filePath: String?): Boolean {
        return runCatching {
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            return@runCatching options.outWidth > 0 && options.outHeight > 0
        }
            .onFailure { e ->
                if (e !is Exception) throw e
            }
            .getOrDefault(false)
    }

    /**
     * Return the type of image.
     *
     * @param filePath The path of file.
     * @return the type of image
     */
    fun getImageType(filePath: String?): ImageType? {
        return getImageType(FileUtils.getFileByPath(filePath))
    }

    /**
     * Return the type of image.
     *
     * @param file The file.
     * @return the type of image
     */
    fun getImageType(file: File?): ImageType? {
        if (file == null) return null

        runCatching {
            FileInputStream(file).use { `is` ->
                val type: ImageType? = getImageType(`is`)
                if (type != null) {
                    return type
                }
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
        return null
    }

    private fun getImageType(`is`: InputStream?): ImageType? {
        if (`is` == null) return null
        return runCatching {
            val bytes = ByteArray(12)
            return@runCatching if (`is`.read(bytes) != -1)
                getImageType(bytes)
            else null
        }.onFailure { e -> e.printStackTrace() }.getOrDefault(null)
    }

    private fun getImageType(bytes: ByteArray?): ImageType {
        val type: String = bytes?.toHex()?.uppercase() ?: return ImageType.TYPE_UNKNOWN
        if (type.contains("FFD8FF")) {
            return ImageType.TYPE_JPG
        } else if (type.contains("89504E47")) {
            return ImageType.TYPE_PNG
        } else if (type.contains("47494638")) {
            return ImageType.TYPE_GIF
        } else if (type.contains("49492A00") || type.contains("4D4D002A")) {
            return ImageType.TYPE_TIFF
        } else if (type.contains("424D")) {
            return ImageType.TYPE_BMP
        } else if (
            type.startsWith("52494646") && type.endsWith("57454250")
        ) { // 524946461c57000057454250-12个字节
            return ImageType.TYPE_WEBP
        } else if (type.contains("00000100") || type.contains("00000200")) {
            return ImageType.TYPE_ICO
        } else {
            return ImageType.TYPE_UNKNOWN
        }
    }

    private fun isJPEG(b: ByteArray): Boolean {
        return b.size >= 2 && (b[0] == 0xFF.toByte()) && (b[1] == 0xD8.toByte())
    }

    private fun isGIF(b: ByteArray): Boolean {
        return b.size >= 6 &&
                b[0] == 'G'.code.toByte() &&
                b[1] == 'I'.code.toByte() &&
                b[2] == 'F'.code.toByte() &&
                b[3] == '8'.code.toByte() &&
                (b[4] == '7'.code.toByte() || b[4] == '9'.code.toByte()) &&
                b[5] == 'a'.code.toByte()
    }

    private fun isPNG(b: ByteArray): Boolean {
        return b.size >= 8 &&
                (b[0] == 137.toByte() &&
                        b[1] == 80.toByte() &&
                        b[2] == 78.toByte() &&
                        b[3] == 71.toByte() &&
                        b[4] == 13.toByte() &&
                        b[5] == 10.toByte() &&
                        b[6] == 26.toByte() &&
                        b[7] == 10.toByte())
    }

    private fun isBMP(b: ByteArray): Boolean {
        return b.size >= 2 && (b[0].toInt() == 0x42) && (b[1].toInt() == 0x4d)
    }

    private fun isEmptyBitmap(src: Bitmap?): Boolean {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ // about compress
    /**
     * Return the compressed bitmap using scale.
     *
     * @param src The source of bitmap.
     * @param newWidth The new width.
     * @param newHeight The new height. @ return the compressed bitmap
     */
    fun compressByScale(
        src: Bitmap?,
        newWidth: Int,
        newHeight: Int,
    ): Bitmap? {
        return scale(src, newWidth, newHeight, false)
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src The source of bitmap.
     * @param newWidth The new width.
     * @param newHeight The new height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    fun compressByScale(
        src: Bitmap?,
        newWidth: Int,
        newHeight: Int,
        recycle: Boolean,
    ): Bitmap? {
        return scale(src, newWidth, newHeight, recycle)
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src The source of bitmap.
     * @param scaleWidth The scale of width.
     * @param scaleHeight The scale of height.
     * @return the compressed bitmap
     */
    fun compressByScale(
        src: Bitmap?,
        scaleWidth: Float,
        scaleHeight: Float,
    ): Bitmap? {
        return scale(src, scaleWidth, scaleHeight, false)
    }

    /**
     * Return the compressed bitmap using scale.
     *
     * @param src The source of bitmap.
     * @param scaleWidth The scale of width.
     * @param scaleHeight The scale of height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return he compressed bitmap
     */
    fun compressByScale(
        src: Bitmap?,
        scaleWidth: Float,
        scaleHeight: Float,
        recycle: Boolean,
    ): Bitmap? {
        return scale(src, scaleWidth, scaleHeight, recycle)
    }

    /**
     * Return the compressed data using quality.
     *
     * @param src The source of bitmap.
     * @param quality The quality.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the compressed data using quality
     */
    fun compressByQuality(
        src: Bitmap?,
        @IntRange(from = 0, to = 100) quality: Int,
        recycle: Boolean = false,
    ): ByteArray? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        ByteArrayOutputStream().use { baos ->
            src.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val bytes: ByteArray? = baos.toByteArray()
            if (recycle && !src.isRecycled) src.recycle()
            return bytes
        }
    }

    /**
     * Return the compressed data using quality.
     *
     * @param src The source of bitmap.
     * @param maxByteSize The maximum size of byte.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the compressed data using quality
     */
    fun compressByQuality(
        src: Bitmap?,
        maxByteSize: Long,
        recycle: Boolean = false,
    ): ByteArray? {
        if (src == null) return null
        if (isEmptyBitmap(src) || maxByteSize <= 0)
            return ByteArray(0)
        ByteArrayOutputStream().use { baos ->
            src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val bytes: ByteArray?
            if (baos.size() <= maxByteSize) {
                bytes = baos.toByteArray()
            } else {
                baos.reset()
                src.compress(Bitmap.CompressFormat.JPEG, 0, baos)
                if (baos.size() >= maxByteSize) {
                    bytes = baos.toByteArray()
                } else {
                    // find the best quality using binary search
                    var st = 0
                    var end = 100
                    var mid = 0
                    while (st < end) {
                        mid = (st + end) / 2
                        baos.reset()
                        src.compress(Bitmap.CompressFormat.JPEG, mid, baos)
                        val len: Int = baos.size()
                        if (len.toLong() == maxByteSize) {
                            break
                        } else if (len > maxByteSize) {
                            end = mid - 1
                        } else {
                            st = mid + 1
                        }
                    }
                    if (end == mid - 1) {
                        baos.reset()
                        src.compress(Bitmap.CompressFormat.JPEG, st, baos)
                    }
                    bytes = baos.toByteArray()
                }
            }
            if (recycle && !src.isRecycled) src.recycle()
            return bytes
        }
    }

    /**
     * Return the compressed bitmap using sample size.
     *
     * @param src The source of bitmap.
     * @param sampleSize The sample size.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    fun compressBySampleSize(
        src: Bitmap?,
        sampleSize: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inSampleSize = sampleSize
        ByteArrayOutputStream().use { baos ->
            src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val bytes: ByteArray = baos.toByteArray()
            if (recycle && !src.isRecycled) src.recycle()
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        }
    }

    /**
     * Return the compressed bitmap using sample size.
     *
     * @param src The source of bitmap.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @param recycle True to recycle the source of bitmap, false otherwise.
     * @return the compressed bitmap
     */
    fun compressBySampleSize(
        src: Bitmap?,
        maxWidth: Int,
        maxHeight: Int,
        recycle: Boolean = false,
    ): Bitmap? {
        if (src == null) return null
        if (isEmptyBitmap(src)) return null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        ByteArrayOutputStream().use { baos ->
            src.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val bytes: ByteArray = baos.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            options.inSampleSize =
                calculateInSampleSize(
                    options,
                    maxWidth,
                    maxHeight,
                )
            options.inJustDecodeBounds = false
            if (recycle && !src.isRecycled()) src.recycle()
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        }
    }

    /**
     * Return the size of bitmap.
     *
     * @param filePath The path of file.
     * @return the size of bitmap
     */
    fun getSize(filePath: String?): IntArray {
        return getSize(FileUtils.getFileByPath(filePath))
    }

    /**
     * Return the size of bitmap.
     *
     * @param file The file.
     * @return the size of bitmap
     */
    fun getSize(file: File?): IntArray {
        if (file == null) return intArrayOf(0, 0)
        val opts: BitmapFactory.Options = BitmapFactory.Options()
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, opts)
        return intArrayOf(opts.outWidth, opts.outHeight)
    }

    /**
     * Return the sample size.
     *
     * @param options The options.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return the sample size
     */
    fun calculateInSampleSize(
        options: BitmapFactory.Options,
        maxWidth: Int,
        maxHeight: Int,
    ): Int {
        var height: Int = options.outHeight
        var width: Int = options.outWidth
        var inSampleSize = 1
        while (height > maxHeight || width > maxWidth) {
            height = height shr 1
            width = width shr 1
            inSampleSize = inSampleSize shl 1
        }
        return inSampleSize
    }
}
