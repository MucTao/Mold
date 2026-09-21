@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.muc.mold.utils.util.ImageUtils.calculateInSampleSize
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.Serializable
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.Locale

/**
 * 转换工具类 —— 提供 Android 开发中常用的类型 / 单位 / 文件 / 视图 / 编码等转换能力。
 *
 * ## 功能分类
 * | 分类 | 方法 |
 * |------|------|
 * | **单位转换** | `dp2px` / `px2dp` / `sp2px` / `px2sp` |
 * | **字节转换** | `byte2FitMemorySize` / `inputStream2Bytes` / `outputStream2Bytes` |
 * | **Bitmap ↔ Drawable ↔ Bytes ↔ Base64** | 双向互转 + 旋转 / 缩放 / 灰度 / 圆角 / 反射 |
 * | **View → Bitmap** | `view2Bitmap` / `scrollView2Bitmap` |
 * | **文件 / Uri / InputStream** | `drawable2File` / `bytes2File` / `uri2File` / `inputStream2File` |
 * | **颜色** | `int2Color` / `color2Int` / `string2Color` |
 * | **HTML** | `string2Html` / 文本变色 / 下划线 / 删除线 |
 * | **时间** | `millis2FitTimeSpan` / `string2Date` / `utc2Local` |
 * | **编码 / 摘要** | `bytes2Hex` / `hash2Hex` / `base64` / `md5` / `sha*` |
 * | **序列化** | `serializable2Bytes` / `bytes2Object` |
 * | **反射** | `byteArray2List` / `json2List` / `type2Class` |
 *
 * 原始作者：Muc
 * 优化：`runCatching` → `try/catch` Result<T>、协程化 IO 操作、补全注释、默认参数精简
 */
@SuppressLint("SimpleDateFormat")
object ConvertUtils {

    // ─────────────────────────────────────────────
    // 单位转换
    // ─────────────────────────────────────────────

    /**
     * dp → px。
     *
     * @param dpValue dp 值，默认 0f
     * @return 对应的 px 值
     */
    fun dp2px(dpValue: Float = 0f): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * px → dp。
     *
     * @param pxValue px 值，默认 0f
     * @return 对应的 dp 值
     */
    fun px2dp(pxValue: Float = 0f): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * sp → px。
     *
     * @param spValue sp 值，默认 0f
     * @return 对应的 px 值
     */
    fun sp2px(spValue: Float = 0f): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * px → sp。
     *
     * @param pxValue px 值，默认 0f
     * @return 对应的 sp 值
     */
    fun px2sp(pxValue: Float = 0f): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    // ─────────────────────────────────────────────
    // 字节 / 内存大小
    // ─────────────────────────────────────────────

    /**
     * 字节数 → 可读内存大小字符串（如 `"1.5 MB"`）。
     * @return 格式化后的字符串
     */
    fun Long.toFitMemorySize(
    ): String {
        return when {
            this < 0L -> "0 B"
            this < 1024L -> "$this B"
            this < 1048576L -> String.format(Locale.getDefault(), "%.1f KB", this / 1024.0)
            this < 1073741824L -> String.format(Locale.getDefault(), "%.1f MB", this / 1048576.0)
            else -> String.format(Locale.getDefault(), "%.1f GB", this / 1073741824.0)
        }
    }

    /**
     * InputStream → ByteArray（协程 IO 线程）。
     */
    suspend fun InputStream.toBytes(): Result<ByteArray> = withContext(Dispatchers.IO) { runCatching { readBytes() } }

    /**
     * ByteArray → InputStream。
     */
    fun ByteArray.toInputStream(): Result<InputStream> = runCatching { ByteArrayInputStream(this) }

    // ─────────────────────────────────────────────
    // Bitmap ↔ Bytes
    // ─────────────────────────────────────────────

    /**
     * Bitmap → ByteArray。
     *
     * @param format 压缩格式，默认 [Bitmap.CompressFormat.PNG]
     * @param quality 压缩质量 0~100，默认 100
     */
    fun Bitmap.toBytes(
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Result<ByteArray> = runCatching {
        ByteArrayOutputStream().use { baos ->
            compress(format, quality, baos)
            baos.toByteArray()
        }
    }

    /**
     * ByteArray → Bitmap。
     *
     * @param opts 解码选项，默认 null
     */
    fun ByteArray.toBitmap(
        opts: BitmapFactory.Options? = null
    ): Result<Bitmap> = runCatching {
        BitmapFactory.decodeByteArray(this, 0, this.size, opts)
    }

    // ─────────────────────────────────────────────
    // Bitmap ↔ Drawable
    // ─────────────────────────────────────────────

    /**
     * Bitmap → Drawable。
     */
    fun Bitmap.toDrawable(): Result<Drawable> = runCatching {
        BitmapDrawable(Resources.getSystem(), this)
    }

    /**
     * Drawable → Bitmap。
     */
    fun Drawable.toBitmap(): Result<Bitmap> = runCatching {
        if (this is BitmapDrawable) {
            bitmap ?: createBitmapFromDrawable(this)
        } else {
            createBitmapFromDrawable(this)
        }
    }

    /**
     * 从非 BitmapDrawable 创建 Bitmap（如 ColorDrawable / GradientDrawable）。
     */
    private fun createBitmapFromDrawable(drawable: Drawable): Bitmap {
        val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1
        val config = if (drawable.opacity != PixelFormat.OPAQUE)
            Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        val bitmap = createBitmap(w, h, config)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    // ─────────────────────────────────────────────
    // Drawable 资源 → Bitmap
    // ─────────────────────────────────────────────


    // ─────────────────────────────────────────────
    // Base64 ↔ Bytes / Bitmap
    // ─────────────────────────────────────────────

    /**
     * Bitmap → Base64 字符串。
     *
     * @param format  压缩格式，默认 PNG
     * @param quality 压缩质量，默认 100
     * @param flags   Base64 标志，默认 [Base64.DEFAULT]
     */
    fun Bitmap.toBase64(
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100,
        flags: Int = Base64.DEFAULT
    ): Result<String> =
        runCatching {
            ByteArrayOutputStream().use { baos ->
                this.compress(format, quality, baos)
                Base64.encodeToString(baos.toByteArray(), flags)
            }
        }


    /**
     * ByteArray → Base64。
     */
    fun ByteArray.toBase64(flags: Int = Base64.DEFAULT): Result<String> = runCatching { Base64.encodeToString(this, flags) }

    // ─────────────────────────────────────────────
    // Bitmap 变换
    // ─────────────────────────────────────────────

    /**
     * 旋转 Bitmap。
     *
     * @param degrees 旋转角度，默认 90f
     * @param px      旋转中心 x，默认 0.5f（相对）
     * @param py      旋转中心 y，默认 0.5f（相对）
     */
    fun Bitmap.rotate(
        @FloatRange(from = 0.0) degrees: Float = 90f,
        @FloatRange(from = 0.0, to = 1.0) px: Float = 0.5f,
        @FloatRange(from = 0.0, to = 1.0) py: Float = 0.5f
    ): Result<Bitmap> = runCatching {
        val matrix = Matrix()
        matrix.setRotate(degrees, this.width * px, this.height * py)
        Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }

    /**
     * 缩放 Bitmap。
     *
     * @param newWidth  新宽度，默认 -1（按高度等比）
     * @param newHeight 新高度，默认 -1（按宽度等比）
     */
    fun Bitmap.scale(
        newWidth: Int = -1,
        newHeight: Int = -1
    ): Result<Bitmap> = runCatching {
        val width = this.width
        val height = this.height
        val scaleWidth: Float
        val scaleHeight: Float
        when {
            newWidth > 0 && newHeight > 0 -> {
                scaleWidth = newWidth.toFloat() / width
                scaleHeight = newHeight.toFloat() / height
            }

            newWidth > 0 -> {
                scaleWidth = newWidth.toFloat() / width
                scaleHeight = scaleWidth
            }

            newHeight > 0 -> {
                scaleHeight = newHeight.toFloat() / height
                scaleWidth = scaleHeight
            }

            else -> return@runCatching this
        }

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    /**
     * 灰度化 Bitmap。
     */
    fun Bitmap.toGray(
    ): Result<Bitmap> = runCatching {
        val gray = createBitmap(this.width, this.height, this.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gray)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(this, 0f, 0f, paint)
        gray
    }

    /**
     * 圆角 Bitmap。
     *
     * @param radius 圆角半径（px），默认 20f
     */
    fun Bitmap.toRound(
        @FloatRange(from = 0.0) radius: Float = 20f
    ): Result<Bitmap> = runCatching {
        val w = this.width
        val h = this.height
        val round = createBitmap(w, h)
        val canvas = Canvas(round)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawRoundRect(
            0f, 0f, w.toFloat(), h.toFloat(),
            radius, radius, paint
        )
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, 0f, 0f, paint)
        round
    }

    /**
     * 垂直 / 水平反射 Bitmap。
     *
     * @param isHorizontal true=水平，false=垂直
     */
    fun Bitmap.reflect(
        isHorizontal: Boolean = true
    ): Result<Bitmap> = runCatching {
        val matrix = Matrix()
        if (isHorizontal) {
            matrix.setScale(-1f, 1f)
            matrix.postTranslate(this.width.toFloat(), 0f)
        } else {
            matrix.setScale(1f, -1f)
            matrix.postTranslate(0f, this.height.toFloat())
        }
        Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }

    // ─────────────────────────────────────────────
    // View → Bitmap
    // ─────────────────────────────────────────────

    /**
     * View → Bitmap。
     *
     * @param scale 缩放比例，默认 1f
     */
    fun View.toBitmap(
        @FloatRange(from = 0.0) scale: Float = 1f
    ): Result<Bitmap> = runCatching {
        this.isDrawingCacheEnabled = true
        this.buildDrawingCache()
        val drawingCache = this.drawingCache ?: return Result.failure(IllegalStateException("drawingCache is null"))
        val bitmap = Bitmap.createBitmap(drawingCache)
        this.isDrawingCacheEnabled = false
        if (scale != 1f) {
            bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt()).getOrThrow()
        } else {
            bitmap
        }
    }

    /**
     * ScrollView 内容 → Bitmap。
     *
     * 原理：通过 [View.draw] 将完整内容绘制到全高 Bitmap。
     */
    fun View.toLongBitmap(
        @FloatRange(from = 0.0) scale: Float = 1f
    ): Result<Bitmap> = runCatching {
        var h = 0
        for (i in (0 until (this as android.view.ViewGroup).childCount)) {
            h += this.getChildAt(i).height
        }
        val bitmap = createBitmap(this.width, h)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        if (scale != 1f) {
            bitmap.scale((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt()).getOrThrow()
        } else {
            bitmap
        }
    }
    // ─────────────────────────────────────────────
    // Drawable / Bitmap → File
    // ─────────────────────────────────────────────

    /**
     * Drawable → File（PNG）。
     *
     * @param dirPath 目标目录路径，默认应用缓存目录
     * @param fileName 文件名，默认 `"drawable2file_<timestamp>.png"`
     */
    suspend fun Drawable.toFile(
        dirPath: String = Utils.app.cacheDir.absolutePath,
        fileName: String = "drawable2file_${System.currentTimeMillis()}.png"
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = this@toFile.toBitmap().getOrThrow()
            val dir = File(dirPath)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            file
        }
    }

    /**
     * Bitmap → File。
     *
     * @param dirPath  目标目录，默认应用缓存目录
     * @param fileName 文件名，默认 `"bitmap2file_<timestamp>.png"`
     * @param format   压缩格式，默认 PNG
     * @param quality  压缩质量，默认 100
     */
    suspend fun Bitmap.toFile(
        dirPath: String = Utils.app.cacheDir.absolutePath,
        fileName: String = "bitmap2file_${System.currentTimeMillis()}.png",
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(dirPath)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { fos ->
                this@toFile.compress(format, quality, fos)
            }
            file
        }
    }

    fun File.toBitMap(): Result<Bitmap> = runCatching { BitmapFactory.decodeFile(absolutePath) }
    fun File.toBitMap(maxWidth: Int, maxHeight: Int): Result<Bitmap> = runCatching {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(absolutePath, options)
        options.inSampleSize =
            calculateInSampleSize(options, maxWidth, maxHeight)
        options.inJustDecodeBounds = false
        BitmapFactory.decodeFile(absolutePath, options)
    }

    // ─────────────────────────────────────────────
    // ByteArray → File
    // ─────────────────────────────────────────────

    /**
     * ByteArray → File。
     *
     * @param dirPath  目标目录，默认应用缓存目录
     * @param fileName 文件名，默认 `"bytes2file_<timestamp>"`
     * @param append   是否追加，默认覆盖
     */
    suspend fun ByteArray.toFile(
        dirPath: String = Utils.app.cacheDir.absolutePath,
        fileName: String = "bytes2file_${System.currentTimeMillis()}",
        append: Boolean = false
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(dirPath)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file, append).use { fos ->
                fos.write(this@toFile)
            }
            file
        }
    }

    /**
     * InputStream → File。
     *
     * @param dirPath  目标目录，默认应用缓存目录
     * @param fileName 文件名，默认 `"inputStream2file_<timestamp>"`
     */
    suspend fun InputStream.toFile(
        dirPath: String = Utils.app.cacheDir.absolutePath,
        fileName: String = "inputStream2file_${System.currentTimeMillis()}"
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(dirPath)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { fos ->
                this@toFile.copyTo(fos)
            }
            file
        }
    }


    // ─────────────────────────────────────────────
    // OutputStream 写入
    // ─────────────────────────────────────────────

    /**
     * ByteArray → OutputStream（协程 IO 线程）。
     */
    suspend fun OutputStream.toBytes(
        bytes: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            this@toBytes.write(bytes)
        }
    }

    // ─────────────────────────────────────────────
    // 编码 / 摘要
    // ─────────────────────────────────────────────

    /**
     * ByteArray → Hex 字符串（小写）。
     */
    fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    /**
     * ByteArray → 各种哈希摘要字符串。
     *
     * 支持的算法：`"MD5"`, `"SHA-1"`, `"SHA-224"`, `"SHA-256"`, `"SHA-384"`, `"SHA-512"`
     *
     * @param algorithm 哈希算法名称，默认 `"MD5"`
     */
    fun ByteArray.toHashHex(
        algorithm: String = "MD5"
    ): Result<String> = runCatching {
        val digest = MessageDigest.getInstance(algorithm)
        val hashBytes = digest.digest(this)
        hashBytes.toHex()
    }

    // 便捷哈希方法
    fun ByteArray.md2(): Result<String> = toHashHex("MD2")
    fun ByteArray.md5(): Result<String> = toHashHex("MD5")
    fun ByteArray.sha1(): Result<String> = toHashHex("SHA-1")
    fun ByteArray.sha224(): Result<String> = toHashHex("SHA-224")
    fun ByteArray.sha256(): Result<String> = toHashHex("SHA-256")
    fun ByteArray.sha384(): Result<String> = toHashHex("SHA-364")
    fun ByteArray.sha512(): Result<String> = toHashHex("SHA-512")
    fun ByteArray.hmacMD5(): Result<String> = toHashHex("HmacMD5")

    // ─────────────────────────────────────────────
    // 序列化
    // ─────────────────────────────────────────────

    /**
     * Serializable → ByteArray。
     */
    fun Serializable.toBytes(
    ): Result<ByteArray> = runCatching {
        ByteArrayOutputStream().use { baos ->
            ObjectOutputStream(baos).use { oos ->
                oos.writeObject(this)
            }
            baos.toByteArray()
        }
    }

    // ─────────────────────────────────────────────
    // 数值格式化
    // ─────────────────────────────────────────────

    /**
     * 数值 → 指定小数位字符串。
     *
     * @param pattern 格式化模式，默认 `"#.##"`（保留 2 位小数）
     */
    fun Number.decimal(pattern: String = "#.##"): String = DecimalFormat(pattern).format(this)
}