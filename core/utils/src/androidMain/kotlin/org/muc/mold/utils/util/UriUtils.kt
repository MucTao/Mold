@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * URI 工具类 —— 统一处理 `file://` / `content://` / MediaStore / DocumentsProvider 等 URI 格式。
 *
 * ## 核心能力
 * - `uri2File(uri)`：任意 URI → `File`（自动分辨类型，支持协程）
 * - `file2Uri(file)`：`File` → `FileProvider` URI（Android 7.0+）
 * - `filePath2Uri(path)`：绝对路径 → `FileProvider` URI
 * - `res2Uri(resId)`：`android.resource://` 资源 URI
 *
 * 原始作者：Muc
 * 优化：协程替代 `runCatching`、补全注释、精简重载、默认参数
 */
object UriUtils {

    // ── file → URI ───────────────────────────────

    /**
     * 将 [File] 转换为 content URI。
     *
     * Android 7.0 及以上使用 [FileProvider]，
     * 低于 7.0 使用 [Uri.fromFile]（已弃用）。
     *
     * @param file      目标文件
     * @param authority FileProvider authority，默认 [Utils.app.packageName.utilcode.fileprovider]
     * @return content URI
     */
    fun file2Uri(
        file: File,
        authority: String = Utils.app.packageName + ".utilcode.fileprovider"
    ): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(Utils.app, authority, file)
        } else {
            @Suppress("DEPRECATION")
            Uri.fromFile(file)
        }
    }

    /**
     * 将绝对路径转换为 content URI。
     *
     * @param filePath  文件绝对路径
     * @param authority FileProvider authority，默认自动获取
     * @return content URI；路径无效时返回 `Uri.EMPTY`
     */
    fun filePath2Uri(
        filePath: String?,
        authority: String = Utils.app.packageName + ".utilcode.fileprovider"
    ): Uri {
        if (filePath.isNullOrEmpty()) return Uri.EMPTY
        val file = File(filePath)
        return if (file.exists()) file2Uri(file, authority) else Uri.EMPTY
    }

    /**
     * 将 Android 资源 ID 转换为 `android.resource://` URI。
     *<p>res2Uri([res type]/[res name]) -> res2Uri(drawable/icon), res2Uri(raw/icon)</p>
     *<p>res2Uri([resource id]) -> res2Uri(R.drawable.icon)</p>
     *
     * @param resPath 资源 ID（如 `R.raw.xxx`.toString()）
     * @return 资源 URI
     */
    fun res2Uri(resPath: String) = "android.resource://${Utils.app.packageName}/$resPath".toUri()


    // ── URI → file ───────────────────────────────

    /**
     * 将任意 [Uri] 转换为 `File`。
     *
     * 自动识别 URI 类型：
     * - `file://`            → 直接构造 File
     * - `content://` + `MediaStore`      → 查询 `_data`
     * - `content://` + `MediaStore` (API ≥ 29) → `contentUri.copyToTempFile()`
     * - `content://` + `DocumentsProvider` → DocumentContract 查询
     * - `content://` + 其他  → `contentUri.copyToTempFile()`
     *
     * **这是挂起函数，请在协程中调用。**
     *
     * @return [Result.success] 包含解析出的 `File`；[Result.failure] 包含异常
     */
    suspend fun Uri.toFile(): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            when (scheme) {
                "file" -> {
                    val path = this@toFile.path ?: throw IllegalArgumentException("file URI has null path")
                    File(path)
                }

                "content" -> handleContentUri(this@toFile)
                else -> throw IllegalArgumentException("unsupported URI scheme: ${this@toFile.scheme}")
            }
        }
    }

    /**
     * 从 Uri 获取文件名。
     */
    fun Uri.getFileName(): String? {
        var name: String? = null
        if (scheme == "content") {
            Utils.app.contentResolver.query(this, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = cursor.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = path?.let { path -> File(path).name }
        }
        return name
    }

    /**
     * Uri → 真实文件路径（适用于 file:// 或 media content）。
     */
    fun Uri.realPath(): Result<String> = runCatching {
        var path: String? = null
        if (scheme == "file") {
            path = this@realPath.path
        } else if (scheme == "content") {
            Utils.app.contentResolver.query(this@realPath, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                    if (index >= 0) {
                        path = cursor.getString(index)
                    }
                }
            }
        }
        path!!
    }
    // ── 内部实现 ─────────────────────────────────

    /**
     * 处理 `content://` scheme。
     */
    private suspend fun handleContentUri(uri: Uri): File = withContext(Dispatchers.IO) {
        when {
            // DocumentsProvider
            DocumentsContract.isDocumentUri(Utils.app, uri) -> handleDocumentUri(uri)
            // MediaStore（非 Downloads）
            uri.authority == "media" -> handleMediaUri(uri)
            // Downloads
            isDownloadsDocumentInternal(uri) -> handleDownloadsUri(uri)
            // MediaStore ≥ API 29 或未知 content provider
            else -> copyContentUriToTempFile(uri)
        }
    }

    /**
     * 处理 DocumentsProvider URI。
     *
     * 支持的 documentAuthority：
     * - `com.android.externalstorage.documents`
     * - `com.android.providers.downloads.documents`
     * - `com.android.providers.media.documents`
     * - `com.google.android.apps.photos.content`
     */
    private suspend fun handleDocumentUri(uri: Uri): File {
        val docId = DocumentsContract.getDocumentId(uri) ?: ""
        val authority = uri.authority ?: ""

        when {
            // 外置存储
            authority == "com.android.externalstorage.documents" -> {
                val split = docId.split(":").dropLastWhile { it.isEmpty() }
                if (split.size < 2) throw IllegalArgumentException("invalid docId: $docId")
                val type = split[0]
                val relativePath = split[1]
                val storageDir = when (type) {
                    "primary" -> Environment.getExternalStorageDirectory().absolutePath
                    else -> "/storage/$type"
                }
                return File("$storageDir/$relativePath")
            }

            // Downloads
            authority == "com.android.providers.downloads.documents" -> {
                if (docId.startsWith("raw:")) {
                    return File(docId.removePrefix("raw:"))
                }
                val id = docId.toLongOrNull()
                    ?: throw IllegalArgumentException("invalid download id: $docId")
                return queryAndCopy(uri, "_id=?", arrayOf(id.toString()))
            }

            // Media
            authority == "com.android.providers.media.documents" -> {
                val split = docId.split(":").dropLastWhile { it.isEmpty() }
                if (split.size < 2) throw IllegalArgumentException("invalid docId: $docId")
                val type = split[0]
                val id = split[1]
                val contentUri: Uri = when (type) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    "document" -> MediaStore.Files.getContentUri("external")
                    else -> throw IllegalArgumentException("unknown document type: $type")
                }
                return queryAndCopy(contentUri, "_id=?", arrayOf(id))
            }

            // Google Photos
            authority.contains("com.google.android.apps.photos.content") -> {
                if (uri.path != null) {
                    val isImage = uri.path!!.contains("image")
                    val contentUri = if (isImage) {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    // Google Photos content URI 会过期，直接复制
                    return copyContentUriToTempFile(contentUri)
                }
                throw IllegalArgumentException("Google Photos URI missing path")
            }

            else -> return copyContentUriToTempFile(uri)
        }
    }

    /**
     * 处理 MediaStore URI（直接查询 `_data` 列）。
     */
    private fun handleMediaUri(uri: Uri): File {
        val path = getDataColumn(uri, null, null)
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) return file
        }
        throw IllegalArgumentException("MediaStore file not found: $uri")
    }

    /**
     * 处理 Downloads URI。
     */
    private suspend fun handleDownloadsUri(uri: Uri): File {
        val id = ContentUris.parseId(uri)
        if (id > 0) {
            return queryAndCopy(
                MediaStore.Files.getContentUri("external"),
                "${MediaStore.MediaColumns._ID}=?",
                arrayOf(id.toString())
            )
        }
        val path = getDataColumn(uri, null, null)
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) return file
        }
        return copyContentUriToTempFile(uri)
    }

    /**
     * 通过 ContentResolver 查询 `_data` 列。
     *
     * @param uri           查询 URI
     * @param selection     WHERE 子句（可空）
     * @param selectionArgs 参数（可空）
     * @return `_data` 列的值，不存在返回 null
     */
    private fun getDataColumn(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        return try {
            cursor = Utils.app.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.DATA),
                selection,
                selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                cursor.getString(index)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    /**
     * 查询并复制：先从 content provider 查询 `_data`，
     * 若文件存在则直接返回，否则复制到临时文件。
     */
    private suspend fun queryAndCopy(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): File = withContext(Dispatchers.IO) {
        val path = getDataColumn(uri, selection, selectionArgs)
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) return@withContext file
        }
        copyContentUriToTempFile(uri)
    }

    /**
     * 将 content URI 的内容复制到临时文件。
     *
     * @param uri content URI
     * @return 临时文件
     */
    private suspend fun copyContentUriToTempFile(uri: Uri): File = withContext(Dispatchers.IO) {
        val context = Utils.app
        val tempDir = File(context.cacheDir, "uri_utils_temp")
        if (!tempDir.exists()) tempDir.mkdirs()

        val fileName = getFileNameFromUri(uri)
        val tempFile = File(tempDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")

        tempFile
    }

    /**
     * 从 content URI 获取文件名。
     */
    private fun getFileNameFromUri(uri: Uri): String {
        var name: String? = null
        var cursor: Cursor? = null
        try {
            cursor = Utils.app.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = cursor.getString(index)
                }
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        if (name.isNullOrEmpty()) {
            name = uri.lastPathSegment
        }
        if (name.isNullOrEmpty()) {
            name = "${System.currentTimeMillis()}.tmp"
        }
        return name
    }

    /**
     * 判断是否为 Downloads document URI（兼容所有已知格式）。
     */
    private fun isDownloadsDocumentInternal(uri: Uri): Boolean {
        val authority = uri.authority ?: return false
        return authority == "com.android.providers.downloads.documents" ||
                authority == "com.android.providers.downloads" ||
                uri.toString().contains("com.android.providers.downloads")
    }
}