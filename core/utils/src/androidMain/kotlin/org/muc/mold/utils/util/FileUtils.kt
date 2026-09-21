@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.StatFs
import android.text.TextUtils
import androidx.core.net.toUri
import org.muc.mold.utils.util.ConvertUtils.toFitMemorySize
import org.muc.mold.utils.util.ConvertUtils.toHex
import java.io.BufferedInputStream
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.net.URL
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.Collections
import javax.net.ssl.HttpsURLConnection

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/05/03
 * desc  : utils about file
 * </pre> *
 */
object FileUtils {

    /**/
    /////////////////////////////////////////////////////////////////////// */
    // interface
    /**/
    /////////////////////////////////////////////////////////////////////// */
    interface OnReplaceListener {
        fun onReplace(srcFile: File?, destFile: File?): Boolean
    }

    private val LINE_SEP: String = System.lineSeparator()

    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    fun getFileByPath(filePath: String?): File? =
        if (filePath.isNullOrBlank()) null else File(filePath)

    /**
     * Return whether the file exists.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFileExists(file: File?): Boolean {
        if (file == null) return false
        if (file.exists()) return true
        return isFileExists(file.absolutePath)
    }

    /**
     * Return whether the file exists.
     *
     * @param filePath The path of file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFileExists(filePath: String?): Boolean {
        val file: File = getFileByPath(filePath) ?: return false
        if (file.exists()) return true
        return isFileExistsApi29(filePath)
    }

    private fun isFileExistsApi29(filePath: String?): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            return runCatching {
                val uri: Uri = Uri.parse(filePath)
                val cr: ContentResolver = Utils.app.contentResolver
                cr.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
            }
                .getOrDefault(false)
        }
        return false
    }

    /**
     * Rename the file.
     *
     * @param filePath The path of file.
     * @param newName The new name of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun rename(filePath: String?, newName: String): Boolean {
        return rename(getFileByPath(filePath), newName)
    }

    /**
     * Rename the file.
     *
     * @param file The file.
     * @param newName The new name of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun rename(file: File?, newName: String): Boolean {
        // file is null then return false
        if (file == null) return false
        // file doesn't exist then return false
        if (!file.exists()) return false
        // the new name is space then return false
        if (newName.isBlank()) return false
        // the new name equals old name then return true
        if (newName == file.getName()) return true
        val parent = file.getParent() ?: return false
        val newFile = File(parent + File.separator + newName)
        // the new name of file exists then return false
        return !newFile.exists() && file.renameTo(newFile)
    }

    /**
     * Return whether it is a directory.
     *
     * @param dirPath The path of directory.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isDir(dirPath: String?): Boolean = isDir(getFileByPath(dirPath))

    /**
     * Return whether it is a directory.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isDir(file: File?) = file != null && file.exists() && file.isDirectory()

    /**
     * Return whether it is a file.
     *
     * @param filePath The path of file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFile(filePath: String?) = isFile(getFileByPath(filePath))

    /**
     * Return whether it is a file.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFile(file: File?): Boolean {
        return file != null && file.exists() && file.isFile()
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param dirPath The path of directory.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsDir(dirPath: String?) = createOrExistsDir(getFileByPath(dirPath))

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsDir(file: File?) =
        file != null && (if (file.exists()) file.isDirectory() else file.mkdirs())

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param filePath The path of file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsFile(filePath: String?) = createOrExistsFile(getFileByPath(filePath))

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        if (file.exists()) return file.isFile()
        if (!createOrExistsDir(file.getParentFile())) return false
        return runCatching {
            file.createNewFile()
        }
            .getOrDefault(false)
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param filePath The path of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun createFileByDeleteOldFile(filePath: String?) =
        createFileByDeleteOldFile(getFileByPath(filePath))

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun createFileByDeleteOldFile(file: File?): Boolean {
        if (file == null) return false
        // file exists and unsuccessfully delete then return false
        if (file.exists() && !file.delete()) return false
        if (!createOrExistsDir(file.getParentFile())) return false
        return runCatching {
            file.createNewFile()
        }
            .getOrDefault(false)
    }

    /**
     * Copy the directory or file.
     *
     * @param srcPath The path of source.
     * @param destPath The path of destination.
     * @param listener The copy listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun copy(
        srcPath: String?,
        destPath: String?,
        listener: OnReplaceListener? = null,
    ) =
        copy(
            getFileByPath(srcPath),
            getFileByPath(destPath),
            listener,
        )

    /**
     * Copy the directory or file.
     *
     * @param src The source.
     * @param dest The destination.
     * @param listener The copy listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun copy(
        src: File?,
        dest: File?,
        listener: OnReplaceListener? = null,
    ): Boolean {
        if (src == null) return false
        if (src.isDirectory()) {
            return copyDir(src, dest, listener)
        }
        return copyFile(src, dest, listener)
    }

    private fun copyDir(
        srcDir: File?,
        destDir: File?,
        listener: OnReplaceListener? = null,
    ) = copyOrMoveDir(srcDir, destDir, listener, false)

    private fun copyFile(
        srcFile: File?,
        destFile: File?,
        listener: OnReplaceListener?,
    ) = copyOrMoveFile(srcFile, destFile, listener, false)

    /**
     * Move the directory or file.
     *
     * @param srcPath The path of source.
     * @param destPath The path of destination.
     * @param listener The move listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun move(
        srcPath: String?,
        destPath: String?,
        listener: OnReplaceListener? = null,
    ): Boolean {
        return move(
            getFileByPath(srcPath),
            getFileByPath(destPath),
            listener,
        )
    }

    /**
     * Move the directory or file.
     *
     * @param src The source.
     * @param dest The destination.
     * @param listener The move listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun move(
        src: File?,
        dest: File?,
        listener: OnReplaceListener? = null,
    ): Boolean {
        if (src == null) return false
        if (src.isDirectory()) {
            return moveDir(src, dest, listener)
        }
        return moveFile(src, dest, listener)
    }

    /**
     * Move the directory.
     *
     * @param srcDir The source directory.
     * @param destDir The destination directory.
     * @param listener The move listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun moveDir(
        srcDir: File?,
        destDir: File?,
        listener: OnReplaceListener? = null,
    ): Boolean {
        return copyOrMoveDir(srcDir, destDir, listener, true)
    }

    /**
     * Move the file.
     *
     * @param srcFile The source file.
     * @param destFile The destination file.
     * @param listener The move listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun moveFile(
        srcFile: File?,
        destFile: File?,
        listener: OnReplaceListener? = null,
    ): Boolean {
        return copyOrMoveFile(srcFile, destFile, listener, true)
    }

    private fun copyOrMoveDir(
        srcDir: File?,
        destDir: File?,
        listener: OnReplaceListener?,
        isMove: Boolean,
    ): Boolean {
        if (srcDir == null || destDir == null) return false
        // destDir's path locate in srcDir's path then return false
        val srcPath: String = srcDir.path + File.separator
        val destPath: String = destDir.path + File.separator
        if (destPath.contains(srcPath)) return false
        if (!srcDir.exists() || !srcDir.isDirectory()) return false
        if (!createOrExistsDir(destDir)) return false
        val files: Array<File>? = srcDir.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                val oneDestFile = File(destPath + file.getName())
                if (file.isFile()) {
                    if (!copyOrMoveFile(file, oneDestFile, listener, isMove)) return false
                } else if (file.isDirectory()) {
                    if (!copyOrMoveDir(file, oneDestFile, listener, isMove)) return false
                }
            }
        }
        return !isMove || deleteDir(srcDir)
    }

    private fun copyOrMoveFile(
        srcFile: File?,
        destFile: File?,
        listener: OnReplaceListener?,
        isMove: Boolean,
    ): Boolean {
        if (srcFile == null || destFile == null) return false
        // srcFile equals destFile then return false
        if (srcFile == destFile) return false
        // srcFile doesn't exist or isn't a file then return false
        if (!srcFile.exists() || !srcFile.isFile()) return false
        if (destFile.exists()) {
            if (
                listener == null || listener.onReplace(srcFile, destFile)
            ) { // require delete the old file
                if (!destFile.delete()) { // unsuccessfully delete then return false
                    return false
                }
            } else {
                return true
            }
        }
        if (!createOrExistsDir(destFile.getParentFile())) return false
        return runCatching {
            return@runCatching FileIOUtils.writeFileFromIS(
                destFile.absolutePath,
                FileInputStream(srcFile),
            ) && !(isMove && !deleteFile(srcFile))
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(false)
    }

    /**
     * Delete the directory.
     *
     * @param filePath The path of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun delete(filePath: String?): Boolean {
        return delete(getFileByPath(filePath))
    }

    /**
     * Delete the directory.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun delete(file: File?): Boolean {
        if (file == null) return false
        if (file.isDirectory()) {
            return deleteDir(file)
        }
        return deleteFile(file)
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun deleteDir(dir: File?): Boolean {
        if (dir == null) return false
        // dir doesn't exist then return true
        if (!dir.exists()) return true
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false
        val files: Array<File>? = dir.listFiles()
        if (files != null && files.size > 0) {
            for (file in files) {
                if (file.isFile()) {
                    if (!file.delete()) return false
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false
                }
            }
        }
        return dir.delete()
    }

    /**
     * Delete the file.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun deleteFile(file: File?): Boolean {
        return file != null && (!file.exists() || file.isFile() && file.delete())
    }

    /**
     * Delete the all in directory.
     *
     * @param dirPath The path of directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteAllInDir(dirPath: String?): Boolean {
        return deleteAllInDir(getFileByPath(dirPath))
    }

    /**
     * Delete the all in directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteAllInDir(dir: File?): Boolean {
        return deleteFilesInDirWithFilter(dir) { true }
    }

    /**
     * Delete all files in directory.
     *
     * @param dirPath The path of directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDir(dirPath: String?): Boolean {
        return deleteFilesInDir(getFileByPath(dirPath))
    }

    /**
     * Delete all files in directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDir(dir: File?): Boolean {
        return deleteFilesInDirWithFilter(dir) { pathname -> pathname.isFile() }
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dirPath The path of directory.
     * @param filter The filter.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDirWithFilter(
        dirPath: String?,
        filter: FileFilter?,
    ): Boolean {
        return deleteFilesInDirWithFilter(getFileByPath(dirPath), filter)
    }

    /**
     * Delete all files that satisfy the filter in directory.
     *
     * @param dir The directory.
     * @param filter The filter.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFilesInDirWithFilter(dir: File?, filter: FileFilter?): Boolean {
        if (dir == null || filter == null) return false
        // dir doesn't exist then return true
        if (!dir.exists()) return true
        // dir isn't a directory then return false
        if (!dir.isDirectory()) return false
        val files = dir.listFiles()
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                if (filter.accept(file)) {
                    if (file.isFile()) {
                        if (!file.delete()) return false
                    } else if (file.isDirectory()) {
                        if (!deleteDir(file)) return false
                    }
                }
            }
        }
        return true
    }

    /**
     * Return the files that satisfy the filter in directory.
     *
     * @param dirPath The path of directory.
     * @param filter The filter.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @param comparator The comparator to determine the order of the list.
     * @return the files that satisfy the filter in directory
     */
    fun listFilesInDir(
        dirPath: String?,
        filter: FileFilter = { true },
        isRecursive: Boolean = false,
        comparator: Comparator<File>? = null,
    ): List<File> {
        return listFilesInDir(getFileByPath(dirPath), filter, isRecursive, comparator)
    }

    /**
     * Return the files that satisfy the filter in directory.
     *
     * @param dir The directory.
     * @param filter The filter.
     * @param isRecursive True to traverse subdirectories, false otherwise.
     * @param comparator The comparator to determine the order of the list.
     * @return the files that satisfy the filter in directory
     */
    fun listFilesInDir(
        dir: File?,
        filter: FileFilter = { true },
        isRecursive: Boolean = false,
        comparator: Comparator<File>? = null,
    ): List<File> {
        val files = listFilesInDirWithFilterInner(dir, filter, isRecursive)
        if (comparator != null) {
            Collections.sort(files, comparator)
        }
        return files
    }

    private fun listFilesInDirWithFilterInner(
        dir: File?,
        filter: FileFilter,
        isRecursive: Boolean,
    ): List<File> {
        val list = ArrayList<File>()
        if (!isDir(dir)) return list
        val files = dir?.listFiles()
        if (files != null && files.size > 0) {
            for (file in files) {
                if (filter.accept(file)) {
                    list.add(file)
                }
                if (isRecursive && file.isDirectory()) {
                    list.addAll(listFilesInDirWithFilterInner(file, filter, true))
                }
            }
        }
        return list
    }

    /**
     * Return the time that the file was last modified.
     *
     * @param filePath The path of file.
     * @return the time that the file was last modified
     */
    fun getFileLastModified(filePath: String?): Long {
        return getFileLastModified(getFileByPath(filePath))
    }

    /**
     * Return the time that the file was last modified.
     *
     * @param file The file.
     * @return the time that the file was last modified
     */
    fun getFileLastModified(file: File?): Long {
        if (file == null) return -1
        return file.lastModified()
    }

    /**
     * Return the charset of file simply.
     *
     * @param filePath The path of file.
     * @return the charset of file simply
     */
    fun getFileCharsetSimple(filePath: String?): String {
        return getFileCharsetSimple(getFileByPath(filePath))
    }

    /**
     * Return the charset of file simply.
     *
     * @param file The file.
     * @return the charset of file simply
     */
    fun getFileCharsetSimple(file: File?): String {
        if (file == null) return ""
        if (isUtf8(file)) return "UTF-8"
        var p = 0

        runCatching {
            BufferedInputStream(FileInputStream(file)).use { `is` ->
                p = (`is`.read() shl 8) + `is`.read()
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
        return when (p) {
            0xfffe -> "Unicode"
            0xfeff -> "UTF-16BE"
            else -> "GBK"
        }
    }

    /**
     * Return whether the charset of file is utf8.
     *
     * @param filePath The path of file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isUtf8(filePath: String?): Boolean {
        return isUtf8(getFileByPath(filePath))
    }

    /**
     * Return whether the charset of file is utf8.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isUtf8(file: File?): Boolean {
        if (file == null) return false

        runCatching {
            val bytes = ByteArray(24)
            BufferedInputStream(FileInputStream(file)).use { `is` ->
                val read: Int = `is`.read(bytes)
                if (read != -1) {
                    val readArr = ByteArray(read)
                    System.arraycopy(bytes, 0, readArr, 0, read)
                    return isUtf8(readArr) == 100
                } else {
                    return false
                }
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
        return false
    }

    /**
     * UTF-8编码方式
     * ----------------------------------------------
     * 0xxxxxxx 110xxxxx 10xxxxxx 1110xxxx 10xxxxxx 10xxxxxx 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
     */
    private fun isUtf8(raw: ByteArray): Int {
        var utf8 = 0
        var ascii = 0
        if (raw.size > 3) {
            if (
                (raw[0] == 0xEF.toByte()) && (raw[1] == 0xBB.toByte()) && (raw[2] == 0xBF.toByte())
            ) {
                return 100
            }
        }
        val len: Int = raw.size
        var child = 0
        var i = 0
        while (i < len) {
            // UTF-8 byte shouldn't be FF and FE
            if (
                (raw[i].toInt() and 0xFF.toByte().toInt()) == 0xFF.toByte().toInt() ||
                (raw[i].toInt() and 0xFE.toByte().toInt()) == 0xFE.toByte().toInt()
            ) {
                return 0
            }
            if (child == 0) {
                // ASCII format is 0x0*******
                if (
                    (raw[i].toInt() and 0x7F.toByte().toInt()) == raw[i].toInt() &&
                    raw[i].toInt() != 0
                ) {
                    ascii++
                } else if ((raw[i].toInt() and 0xC0.toByte().toInt()) == 0xC0.toByte().toInt()) {
                    // 0x11****** maybe is UTF-8
                    for (bit in 0..7) {
                        if (
                            (((0x80 shr bit).toByte()).toInt() and raw[i].toInt()) ==
                            ((0x80 shr bit).toByte()).toInt()
                        ) {
                            child = bit
                        } else {
                            break
                        }
                    }
                    utf8++
                }
                i++
            } else {
                child = if (raw.size - i > child) child else (raw.size - i)
                var currentNotUtf8 = false
                for (children in 0..<child) {
                    // format must is 0x10******
                    if (
                        (raw[i + children].toInt() and (0x80.toByte()).toInt()) !=
                        (0x80.toByte()).toInt()
                    ) {
                        if (
                            (raw[i + children].toInt() and 0x7F.toByte().toInt()) ==
                            raw[i + children].toInt() && raw[i].toInt() != 0
                        ) {
                            // ASCII format is 0x0*******
                            ascii++
                        }
                        currentNotUtf8 = true
                    }
                }
                if (currentNotUtf8) {
                    utf8--
                    i++
                } else {
                    utf8 += child
                    i += child
                }
                child = 0
            }
        }
        // UTF-8 contains ASCII
        if (ascii == len) {
            return 100
        }
        return (100 * ((utf8 + ascii).toFloat() / len.toFloat())).toInt()
    }

    /**
     * Return the number of lines of file.
     *
     * @param filePath The path of file.
     * @return the number of lines of file
     */
    fun getFileLines(filePath: String?): Int {
        return getFileLines(getFileByPath(filePath))
    }

    /**
     * Return the number of lines of file.
     *
     * @param file The file.
     * @return the number of lines of file
     */
    fun getFileLines(file: File?): Int {
        var count = 1

        runCatching {
            BufferedInputStream(FileInputStream(file)).use { `is` ->
                val buffer = ByteArray(1024)
                var readChars: Int
                if (LINE_SEP.endsWith("\n")) {
                    while ((`is`.read(buffer, 0, 1024).also { readChars = it }) != -1) {
                        for (i in 0..<readChars) {
                            if (buffer[i] == '\n'.code.toByte()) ++count
                        }
                    }
                } else {
                    while ((`is`.read(buffer, 0, 1024).also { readChars = it }) != -1) {
                        for (i in 0..<readChars) {
                            if (buffer[i] == '\r'.code.toByte()) ++count
                        }
                    }
                }
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
        return count
    }

    /**
     * Return the size.
     *
     * @param filePath The path of file.
     * @return the size
     */
    fun getSize(filePath: String?): String? {
        return getSize(getFileByPath(filePath))
    }

    /**
     * Return the size.
     *
     * @param file The directory.
     * @return the size
     */
    fun getSize(file: File?): String? {
        if (file == null) return ""
        if (file.isDirectory()) {
            return getDirSize(file)
        }
        return getFileSize(file)
    }

    /**
     * Return the size of directory.
     *
     * @param dir The directory.
     * @return the size of directory
     */
    private fun getDirSize(dir: File?): String? {
        val len: Long = getDirLength(dir)
        return if (len == -1L) "" else len.toFitMemorySize()
    }

    /**
     * Return the size of file.
     *
     * @param file The file.
     * @return the length of file
     */
    private fun getFileSize(file: File?): String? {
        val len: Long = getFileLength(file)
        return if (len == -1L) "" else len.toFitMemorySize()
    }

    /**
     * Return the length.
     *
     * @param filePath The path of file.
     * @return the length
     */
    fun getLength(filePath: String?): Long {
        return getLength(getFileByPath(filePath))
    }

    /**
     * Return the length.
     *
     * @param file The file.
     * @return the length
     */
    fun getLength(file: File?): Long {
        if (file == null) return 0
        if (file.isDirectory()) {
            return getDirLength(file)
        }
        return getFileLength(file)
    }

    /**
     * Return the length of directory.
     *
     * @param dir The directory.
     * @return the length of directory
     */
    private fun getDirLength(dir: File?): Long {
        if (!isDir(dir)) return 0
        var len: Long = 0
        val files = dir?.listFiles()
        if (files != null && files.size > 0) {
            for (file in files) {
                len += if (file.isDirectory()) {
                    getDirLength(file)
                } else {
                    file.length()
                }
            }
        }
        return len
    }

    /**
     * Return the length of file.
     *
     * @param filePath The path of file.
     * @return the length of file
     */
    fun getFileLength(filePath: String): Long {
        val isURL = filePath.matches(Regex("[a-zA-z]+://\\S*"))
        if (isURL) {
            runCatching {
                val conn: HttpsURLConnection = URL(filePath).openConnection() as HttpsURLConnection
                conn.setRequestProperty("Accept-Encoding", "identity")
                conn.connect()
                if (conn.getResponseCode() == 200) {
                    return conn.getContentLength().toLong()
                }
                return -1
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
        return getFileLength(getFileByPath(filePath))
    }

    /**
     * Return the length of file.
     *
     * @param file The file.
     * @return the length of file
     */
    private fun getFileLength(file: File?): Long {
        if (!isFile(file)) return -1
        return file?.length() ?: -1
    }

    /**
     * Return the MD5 of file.
     *
     * @param filePath The path of file.
     * @return the md5 of file
     */
    fun getFileMD5ToString(filePath: String?): String {
        val file: File? = if (filePath.isNullOrBlank()) null else File(filePath)
        return getFileMD5ToString(file)
    }

    /**
     * Return the MD5 of file.
     *
     * @param file The file.
     * @return the md5 of file
     */
    fun getFileMD5ToString(file: File?): String = getFileMD5(file)?.toHex() ?: ""


    /**
     * Return the MD5 of file.
     *
     * @param filePath The path of file.
     * @return the md5 of file
     */
    fun getFileMD5(filePath: String?): ByteArray? {
        return getFileMD5(getFileByPath(filePath))
    }

    /**
     * Return the MD5 of file.
     *
     * @param file The file.
     * @return the md5 of file
     */
    fun getFileMD5(file: File?): ByteArray? {
        if (file == null) return null

        runCatching {
            val fis = FileInputStream(file)
            var md: MessageDigest = MessageDigest.getInstance("MD5")
            DigestInputStream(fis, md).use { dis ->
                val buffer = ByteArray(1024 * 256)
                while (true) {
                    if (dis.read(buffer) <= 0) break
                }
                md = dis.messageDigest
                return md.digest()
            }
        }.onFailure { e ->
            e.printStackTrace()
        }
        return null
    }

    /**
     * Return the file's path of directory.
     *
     * @param file The file.
     * @return the file's path of directory
     */
    fun getDirName(file: File?): String? {
        if (file == null) return ""
        return getDirName(file.absolutePath)
    }

    /**
     * Return the file's path of directory.
     *
     * @param filePath The path of file.
     * @return the file's path of directory
     */
    fun getDirName(filePath: String): String? {
        if (filePath.isBlank()) return ""
        val lastSep: Int = filePath.lastIndexOf(File.separator)
        return if (lastSep == -1) "" else filePath.substring(0, lastSep + 1)
    }

    /**
     * Return the name of file.
     *
     * @param file The file.
     * @return the name of file
     */
    fun getFileName(file: File?): String? {
        if (file == null) return ""
        return getFileName(file.absolutePath)
    }

    /**
     * Return the name of file.
     *
     * @param filePath The path of file.
     * @return the name of file
     */
    fun getFileName(filePath: String): String? {
        if (filePath.isBlank()) return ""
        val lastSep: Int = filePath.lastIndexOf(File.separator)
        return if (lastSep == -1) filePath else filePath.substring(lastSep + 1)
    }

    /**
     * Return the name of file without extension.
     *
     * @param file The file.
     * @return the name of file without extension
     */
    fun getFileNameNoExtension(file: File?): String? {
        if (file == null) return ""
        return getFileNameNoExtension(file.path)
    }

    /**
     * Return the name of file without extension.
     *
     * @param filePath The path of file.
     * @return the name of file without extension
     */
    fun getFileNameNoExtension(filePath: String): String? {
        if (filePath.isBlank()) return ""
        val lastPoi = filePath.lastIndexOf('.')
        val lastSep: Int = filePath.lastIndexOf(File.separator)
        if (lastSep == -1) {
            return (if (lastPoi == -1) filePath else filePath.substring(0, lastPoi))
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1)
        }
        return filePath.substring(lastSep + 1, lastPoi)
    }

    /**
     * Return the extension of file.
     *
     * @param file The file.
     * @return the extension of file
     */
    fun getFileExtension(file: File?): String {
        if (file == null) return ""
        return getFileExtension(file.path)
    }

    /**
     * Return the extension of file.
     *
     * @param filePath The path of file.
     * @return the extension of file
     */
    fun getFileExtension(filePath: String): String {
        if (filePath.isBlank()) return ""
        val lastPoi = filePath.lastIndexOf('.')
        val lastSep: Int = filePath.lastIndexOf(File.separator)
        if (lastPoi == -1 || lastSep >= lastPoi) return ""
        return filePath.substring(lastPoi + 1)
    }

    /**
     * Notify system to scan the file.
     *
     * @param filePath The path of file.
     */
    fun notifySystemToScan(filePath: String?) {
        notifySystemToScan(getFileByPath(filePath))
    }

    /**
     * Notify system to scan the file.
     *
     * @param file The file.
     */
    fun notifySystemToScan(file: File?) {
        if (file == null || !file.exists()) return
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.setData(("file://" + file.absolutePath).toUri())
        Utils.app.sendBroadcast(intent)
    }

    /**
     * Return the total size of file system.
     *
     * @param anyPathInFs Any path in file system.
     * @return the total size of file system
     */
    fun getFsTotalSize(anyPathInFs: String?): Long {
        if (anyPathInFs.isNullOrBlank()) return 0
        val statFs = StatFs(anyPathInFs)
        val blockSize: Long = statFs.blockSizeLong
        val totalSize: Long = statFs.blockCountLong
        return blockSize * totalSize
    }

    /**
     * Return the available size of file system.
     *
     * @param anyPathInFs Any path in file system.
     * @return the available size of file system
     */
    fun getFsAvailableSize(anyPathInFs: String?): Long {
        if (TextUtils.isEmpty(anyPathInFs)) return 0
        val statFs = StatFs(anyPathInFs)
        val blockSize: Long = statFs.blockSizeLong
        val availableSize: Long = statFs.availableBlocksLong
        return blockSize * availableSize
    }
}
