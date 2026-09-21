@file:Suppress("unused")

package org.muc.mold.utils.util

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * ZIP 压缩 / 解压工具类 —— 支持文件、文件夹、多文件、多文件保留结构。
 *
 * ## 核心能力
 * - 压缩：文件 → zip、文件夹 → zip、多文件 → zip、多文件保留结构 → zip
 * - 解压：zip → 目标目录
 * - 查询：获取 zip 内文件列表、获取注释
 *
 * ## 使用示例
 * ```kotlin
 * // 压缩文件夹
 * val result = ZipUtils.zipFiles(listOf(File("/sdcard/Documents")), File("/sdcard/backup.zip"))
 *
 * // 解压
 * ZipUtils.unzipFile(File("/sdcard/backup.zip"), File("/sdcard/extracted"))
 *
 * // 获取 zip 内文件列表
 * ZipUtils.getFilesPath(File("/sdcard/backup.zip")).onSuccess { paths -> ... }
 * ```
 *
 * 原始作者：Muc
 * 优化：`ThreadUtils` / `runCatching` → `suspend fun` + `Result<T>`、补全注释、默认参数
 */
object ZipUtils {

    /** 默认缓冲区大小：8 KB。 */
    private const val DEFAULT_BUFFER_SIZE: Int = 8 * 1024

    // ── 公开 API：压缩 ────────────────────────────

    /**
     * 压缩文件或文件夹为 ZIP。
     *
     * @param srcFiles   源文件或文件夹列表
     * @param destZip    目标 ZIP 文件
     * @param comment    ZIP 注释，默认无
     * @param bufferSize 缓冲区大小（字节），默认 8192
     * @return [Result.success] 包含 `true`；[Result.failure] 包含异常
     */
    suspend fun zipFiles(
        srcFiles: List<File>,
        destZip: File,
        comment: String? = null,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            zipFilesInternal(srcFiles, destZip, comment, bufferSize)
        }
    }

    /**
     * 压缩多个文件保留目录结构。
     *
     * @param srcFiles   源文件列表
     * @param destZip    目标 ZIP 文件
     * @param comment    ZIP 注释，默认无
     * @param bufferSize 缓冲区大小，默认 8192
     * @return [Result.success] 包含 `true`；[Result.failure] 包含异常
     */
    suspend fun zipFilesWithStructure(
        srcFiles: List<File>,
        destZip: File,
        comment: String? = null,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            zipFilesInternal(srcFiles, destZip, comment, bufferSize, keepDirStructure = true)
        }
    }

    /**
     * 压缩单个文件。
     *
     * @param srcFile    源文件
     * @param destZip    目标 ZIP 文件
     * @param comment    ZIP 注释，默认无
     * @param bufferSize 缓冲区大小，默认 8192
     * @return [Result.success] 包含 `true`；[Result.failure] 包含异常
     */
    suspend fun zipFile(
        srcFile: File,
        destZip: File,
        comment: String? = null,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Result<Unit> = zipFiles(listOf(srcFile), destZip, comment, bufferSize)

    // ── 公开 API：解压 ────────────────────────────

    /**
     * 解压 ZIP 到指定目录。
     *
     * 自动适配 API：≥ 24 使用 `ZipFile`（性能更好），< 24 使用 `ZipInputStream`。
     *
     * @param srcZip     源 ZIP 文件
     * @param destDir    目标目录（不存在会自动创建）
     * @param bufferSize 缓冲区大小，默认 8192
     * @return [Result.success] 包含 `true`；[Result.failure] 包含异常
     */
    suspend fun unzipFile(
        srcZip: File,
        destDir: File,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                unzipFileByZipFile(srcZip, destDir)
            } else {
                unzipFileByZipInputStream(srcZip, destDir, bufferSize)
            }
        }
    }

    /**
     * 解压 ZIP 中指定文件到目标目录。
     *
     * @param srcZip     源 ZIP 文件
     * @param destDir    目标目录
     * @param filePath   要解压的文件在 ZIP 中的路径（如 `"images/photo.jpg"`）
     * @param bufferSize 缓冲区大小，默认 8192
     * @return [Result.success] 包含解压出的文件列表；[Result.failure] 包含异常
     */
    suspend fun unzipFileByPath(
        srcZip: File,
        destDir: File,
        filePath: String?,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                unzipFileByZipFileFiltered(srcZip, destDir, filePath)
            } else {
                unzipFileByZipInputStreamFiltered(srcZip, destDir, filePath, bufferSize)
            }
        }
    }

    // ── 公开 API：查询 ────────────────────────────

    /**
     * 获取 ZIP 中所有文件的路径列表。
     *
     * @param srcZip 源 ZIP 文件
     * @return [Result.success] 包含路径列表；[Result.failure] 包含异常
     */
    suspend fun getFilesPath(
        srcZip: File
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val paths = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ZipFile(srcZip).use { zipFile ->
                    val entries = zipFile.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (!entry.isDirectory) {
                            paths.add(entry.name)
                        }
                    }
                }
            } else {
                FileInputStream(srcZip).use { fis ->
                    ZipInputStream(BufferedInputStream(fis)).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                paths.add(entry.name)
                            }
                            entry = zis.nextEntry
                        }
                    }
                }
            }
            paths
        }
    }

    /**
     * 获取 ZIP 文件的注释。
     *
     * @param srcZip 源 ZIP 文件
     * @return [Result.success] 包含注释字符串（可能为空）；[Result.failure] 包含异常
     */
    suspend fun getComment(
        srcZip: File
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val comment: String = ZipFile(srcZip).use { zipFile ->
                val comment = zipFile.comment
                if (comment != null)
                    comment
                else {
                    val comments = ArrayList<String>()
                    val entries = zipFile.entries()
                    while (entries.hasMoreElements()) {
                        val entry = (entries.nextElement() as ZipEntry)
                        comments.add(entry.comment)
                    }
                    comments.joinToString(",")
                }
            }
            comment
        }
    }

    // ── 内部实现：压缩 ────────────────────────────

    /**
     * 核心压缩逻辑。
     *
     * @param srcFiles          源文件列表
     * @param destZip           目标 ZIP
     * @param comment           ZIP 注释（可空）
     * @param bufferSize        缓冲区大小
     * @param keepDirStructure  是否保留目录结构，默认 false（直接放文件）
     */
    private fun zipFilesInternal(
        srcFiles: List<File>,
        destZip: File,
        comment: String?,
        bufferSize: Int,
        keepDirStructure: Boolean = false
    ) {
        // 确保父目录存在
        val parentDir = destZip.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        FileOutputStream(destZip).buffered().use { fos ->
            ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                comment?.let { zos.setComment(it) }

                for (srcFile in srcFiles) {
                    if (!srcFile.exists()) continue

                    if (srcFile.isDirectory) {
                        zipDirectory(srcFile, "", zos, keepDirStructure, bufferSize)
                    } else {
                        val entryName = if (keepDirStructure) srcFile.name else srcFile.name
                        writeFileToZip(srcFile, entryName, zos, bufferSize)
                    }
                }
            }
        }
    }

    /**
     * 递归压缩目录。
     *
     * @param dir               目录
     * @param parentPath        父路径前缀
     * @param zos               ZipOutputStream
     * @param keepDirStructure  是否保留目录结构
     * @param bufferSize        缓冲区大小
     */
    private fun zipDirectory(
        dir: File,
        parentPath: String,
        zos: ZipOutputStream,
        keepDirStructure: Boolean,
        bufferSize: Int
    ) {
        val files = dir.listFiles() ?: return

        for (file in files) {
            val entryName = if (keepDirStructure) {
                if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
            } else {
                file.name
            }

            if (file.isDirectory) {
                // 目录本身也要写入（保留空目录）
                if (keepDirStructure) {
                    val dirEntry = ZipEntry("$entryName/")
                    zos.putNextEntry(dirEntry)
                    zos.closeEntry()
                }
                zipDirectory(file, entryName, zos, keepDirStructure, bufferSize)
            } else {
                writeFileToZip(file, entryName, zos, bufferSize)
            }
        }
    }

    /**
     * 将单个文件写入 ZipOutputStream。
     *
     * @param file       源文件
     * @param entryName  ZIP 中条目名
     * @param zos        ZipOutputStream
     * @param bufferSize 缓冲区大小
     */
    @Throws(IOException::class)
    private fun writeFileToZip(
        file: File,
        entryName: String,
        zos: ZipOutputStream,
        bufferSize: Int
    ) {
        val entry = ZipEntry(entryName)
        entry.time = file.lastModified()
        zos.putNextEntry(entry)

        FileInputStream(file).buffered().use { fis ->
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                zos.write(buffer, 0, bytesRead)
            }
        }
        zos.closeEntry()
    }

    // ── 内部实现：解压（全部） ─────────────────────

    /**
     * API ≥ 24：使用 ZipFile 解压全部。
     */
    @Throws(IOException::class)
    private fun unzipFileByZipFile(srcZip: File, destDir: File) {
        ZipFile(srcZip).use { zipFile ->
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name
                val destFile = File(destDir, entryName)

                if (entry.isDirectory) {
                    destFile.mkdirs()
                } else {
                    destFile.parentFile?.mkdirs()
                    zipFile.getInputStream(entry).use { input ->
                        FileOutputStream(destFile).buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    /**
     * API < 24：使用 ZipInputStream 解压全部。
     */
    @Throws(IOException::class)
    private fun unzipFileByZipInputStream(
        srcZip: File,
        destDir: File,
        bufferSize: Int
    ) {
        FileInputStream(srcZip).use { fis ->
            ZipInputStream(BufferedInputStream(fis)).use { zis ->
                var entry = zis.nextEntry
                val buffer = ByteArray(bufferSize)

                while (entry != null) {
                    val entryName = entry.name
                    val destFile = File(destDir, entryName)

                    if (entry.isDirectory) {
                        destFile.mkdirs()
                    } else {
                        destFile.parentFile?.mkdirs()
                        FileOutputStream(destFile).buffered().use { fos ->
                            var bytesRead: Int
                            while (zis.read(buffer).also { bytesRead = it } != -1) {
                                fos.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    entry = zis.nextEntry
                }
            }
        }
    }

    // ── 内部实现：解压（指定路径） ─────────────────

    /**
     * API ≥ 24：使用 ZipFile 按路径过滤解压。
     *
     * @param filePath 要解压的文件路径（可空表示全部）
     * @return 解压出的文件列表
     */
    @Throws(IOException::class)
    private fun unzipFileByZipFileFiltered(
        srcZip: File,
        destDir: File,
        filePath: String?
    ): List<File> {
        val extractedFiles = mutableListOf<File>()

        ZipFile(srcZip).use { zipFile ->
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name

                // 过滤：如果指定了路径且不匹配，跳过
                if (filePath != null && entryName != filePath) continue

                val destFile = File(destDir, entryName)
                if (entry.isDirectory) {
                    destFile.mkdirs()
                } else {
                    destFile.parentFile?.mkdirs()
                    zipFile.getInputStream(entry).use { input ->
                        FileOutputStream(destFile).buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                    extractedFiles.add(destFile)
                }
            }
        }
        return extractedFiles
    }

    /**
     * API < 24：使用 ZipInputStream 按路径过滤解压。
     *
     * @param filePath 要解压的文件路径（可空表示全部）
     * @return 解压出的文件列表
     */
    @Throws(IOException::class)
    private fun unzipFileByZipInputStreamFiltered(
        srcZip: File,
        destDir: File,
        filePath: String?,
        bufferSize: Int
    ): List<File> {
        val extractedFiles = mutableListOf<File>()

        FileInputStream(srcZip).use { fis ->
            ZipInputStream(BufferedInputStream(fis)).use { zis ->
                var entry = zis.nextEntry
                val buffer = ByteArray(bufferSize)

                while (entry != null) {
                    val entryName = entry.name

                    // 过滤
                    if (filePath == null || entryName == filePath) {
                        val destFile = File(destDir, entryName)
                        if (entry.isDirectory) {
                            destFile.mkdirs()
                        } else {
                            destFile.parentFile?.mkdirs()
                            FileOutputStream(destFile).buffered().use { fos ->
                                var bytesRead: Int
                                while (zis.read(buffer).also { bytesRead = it } != -1) {
                                    fos.write(buffer, 0, bytesRead)
                                }
                            }
                            extractedFiles.add(destFile)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
        }
        return extractedFiles
    }
}