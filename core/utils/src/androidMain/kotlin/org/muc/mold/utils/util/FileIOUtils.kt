@file:Suppress("unused")

package org.muc.mold.utils.util

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2017/06/22
 * desc  : utils about file io
 * </pre> *
 */
object FileIOUtils {

    interface OnProgressUpdateListener {
        fun onProgressUpdate(progress: Double)
    }

    private var sBufferSize = 524288

    /**/
    /////////////////////////////////////////////////////////////////////// */
    // writeFileFromIS without progress
    /**
     * Write file from input stream.
     *
     * @param filePath The path of file.
     * @param ins The input stream.
     * @param append True to append, false otherwise.
     * @param listener The progress update listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromIS(
        filePath: String?,
        ins: InputStream?,
        append: Boolean = false,
        listener: OnProgressUpdateListener? = null,
    ): Boolean = writeFileFromIS(FileUtils.getFileByPath(filePath), ins, append, listener)

    /**
     * Write file from input stream.
     *
     * @param file The file.
     * @param ins The input stream.
     * @param append True to append, false otherwise.
     * @param listener The progress update listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromIS(
        file: File?,
        ins: InputStream?,
        append: Boolean = false,
        listener: OnProgressUpdateListener? = null,
    ): Boolean {
        if (ins == null || !FileUtils.createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }
        return runCatching {
            ins.use { inputStream ->
                BufferedOutputStream(FileOutputStream(file, append), sBufferSize).use { os ->
                    if (listener == null) {
                        val data = ByteArray(sBufferSize)
                        var len: Int
                        while ((inputStream.read(data).also { len = it }) != -1) {
                            os.write(data, 0, len)
                        }
                    } else {
                        val totalSize: Int = inputStream.available()
                        var curSize = 0
                        listener.onProgressUpdate(0.0)
                        val data = ByteArray(sBufferSize)
                        var len: Int
                        while ((inputStream.read(data).also { len = it }) != -1) {
                            os.write(data, 0, len)
                            curSize += len
                            listener.onProgressUpdate(curSize / totalSize.toDouble())
                        }
                    }
                }
            }
            true
        }
            .getOrDefault(false)
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */
    // writeFileFromBytesByStream without progress
    /**
     * Write file from bytes by stream.
     *
     * @param filePath The path of file.
     * @param bytes The bytes. @ return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByStream(filePath: String?, bytes: ByteArray?): Boolean {
        return writeFileFromBytesByStream(FileUtils.getFileByPath(filePath), bytes, false, null)
    }

    /**
     * Write file from bytes by stream.
     *
     * @param filePath The path of file.
     * @param bytes The bytes.
     * @param append True to append, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByStream(
        filePath: String?,
        bytes: ByteArray?,
        append: Boolean,
    ): Boolean {
        return writeFileFromBytesByStream(FileUtils.getFileByPath(filePath), bytes, append, null)
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */
    // writeFileFromBytesByStream with progress
    /**
     * Write file from bytes by stream.
     *
     * @param filePath The path of file.
     * @param bytes The bytes.
     * @param listener The progress update listener. @ return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByStream(
        filePath: String?,
        bytes: ByteArray?,
        listener: OnProgressUpdateListener?,
    ): Boolean {
        return writeFileFromBytesByStream(FileUtils.getFileByPath(filePath), bytes, false, listener)
    }

    /**
     * Write file from bytes by stream.
     *
     * @param filePath The path of file.
     * @param bytes The bytes.
     * @param append True to append, false otherwise.
     * @param listener The progress update listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByStream(
        filePath: String?,
        bytes: ByteArray?,
        append: Boolean,
        listener: OnProgressUpdateListener?,
    ): Boolean {
        return writeFileFromBytesByStream(
            FileUtils.getFileByPath(filePath),
            bytes,
            append,
            listener,
        )
    }

    /**
     * Write file from bytes by stream.
     *
     * @param file The file.
     * @param bytes The bytes.
     * @param listener The progress update listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByStream(
        file: File?,
        bytes: ByteArray?,
        listener: OnProgressUpdateListener?,
    ): Boolean {
        return writeFileFromBytesByStream(file, bytes, false, listener)
    }

    /**
     * Write file from bytes by stream.
     *
     * @param file The file.
     * @param bytes The bytes.
     * @param append True to append, false otherwise.
     * @param listener The progress update listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByStream(
        file: File?,
        bytes: ByteArray?,
        append: Boolean = false,
        listener: OnProgressUpdateListener? = null,
    ): Boolean {
        if (bytes == null) return false
        return writeFileFromIS(file, ByteArrayInputStream(bytes), append, listener)
    }

    /**
     * Write file from bytes by channel.
     *
     * @param filePath The path of file.
     * @param bytes The bytes.
     * @param isForce 是否写入文件
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByChannel(
        filePath: String?,
        bytes: ByteArray?,
        isForce: Boolean,
    ): Boolean {
        return writeFileFromBytesByChannel(FileUtils.getFileByPath(filePath), bytes, false, isForce)
    }

    /**
     * Write file from bytes by channel.
     *
     * @param filePath The path of file.
     * @param bytes The bytes.
     * @param append True to append, false otherwise.
     * @param isForce True to force write file, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByChannel(
        filePath: String?,
        bytes: ByteArray?,
        append: Boolean,
        isForce: Boolean,
    ): Boolean {
        return writeFileFromBytesByChannel(
            FileUtils.getFileByPath(filePath),
            bytes,
            append,
            isForce,
        )
    }

    /**
     * Write file from bytes by channel.
     *
     * @param file The file.
     * @param bytes The bytes.
     * @param isForce True to force write file, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByChannel(
        file: File?,
        bytes: ByteArray?,
        isForce: Boolean,
    ): Boolean {
        return writeFileFromBytesByChannel(file, bytes, false, isForce)
    }

    /**
     * Write file from bytes by channel.
     *
     * @param file The file.
     * @param bytes The bytes.
     * @param append True to append, false otherwise.
     * @param isForce True to force write file, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByChannel(
        file: File?,
        bytes: ByteArray?,
        append: Boolean,
        isForce: Boolean,
    ): Boolean {
        if (bytes == null) {
            Log.e("FileIOUtils", "bytes is null.")
            return false
        }
        if (!FileUtils.createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }

        return runCatching {
            FileOutputStream(file, append).getChannel().use { fc ->
                if (fc == null) {
                    Log.e("FileIOUtils", "fc is null.")
                    return@runCatching false
                }
                fc.position(fc.size())
                fc.write(ByteBuffer.wrap(bytes))
                if (isForce) fc.force(true)
                return@runCatching true
            }
        }
            .onFailure { e ->
                if (e !is IOException) throw e
                e.printStackTrace()
            }
            .getOrDefault(false)
    }

    /**
     * Write file from bytes by map.
     *
     * @param filePath The path of file.
     * @param bytes The bytes.
     * @param isForce True to force write file, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByMap(
        filePath: String?,
        bytes: ByteArray?,
        isForce: Boolean,
    ): Boolean {
        return writeFileFromBytesByMap(filePath, bytes, false, isForce)
    }

    /**
     * Write file from bytes by map.
     *
     * @param filePath The path of file.
     * @param bytes The bytes.
     * @param append True to append, false otherwise.
     * @param isForce True to force write file, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByMap(
        filePath: String?,
        bytes: ByteArray?,
        append: Boolean,
        isForce: Boolean,
    ): Boolean {
        return writeFileFromBytesByMap(FileUtils.getFileByPath(filePath), bytes, append, isForce)
    }

    /**
     * Write file from bytes by map.
     *
     * @param file The file.
     * @param bytes The bytes.
     * @param isForce True to force write file, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByMap(
        file: File?,
        bytes: ByteArray?,
        isForce: Boolean,
    ): Boolean {
        return writeFileFromBytesByMap(file, bytes, false, isForce)
    }

    /**
     * Write file from bytes by map.
     *
     * @param file The file.
     * @param bytes The bytes.
     * @param append True to append, false otherwise.
     * @param isForce True to force write file, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromBytesByMap(
        file: File?,
        bytes: ByteArray?,
        append: Boolean,
        isForce: Boolean,
    ): Boolean {
        if (bytes == null || !FileUtils.createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }

        return runCatching {
            RandomAccessFile(file, "rw").use { randomAccess ->
                if (!append) randomAccess.setLength(0)
                randomAccess.channel.use { fc ->
                    val mbb =
                        fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), bytes.size.toLong())
                    mbb.put(bytes)
                    if (isForce) mbb.force()
                    true
                }
            }
        }
            .onFailure { e ->
                if (e !is IOException) throw e
                e.printStackTrace()
            }
            .getOrDefault(false)
    }

    /**
     * Write file from string.
     *
     * @param filePath The path of file.
     * @param content The string of content.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromString(filePath: String?, content: String?): Boolean {
        return writeFileFromString(FileUtils.getFileByPath(filePath), content, false)
    }

    /**
     * Write file from string.
     *
     * @param filePath The path of file.
     * @param content The string of content.
     * @param append True to append, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromString(
        filePath: String?,
        content: String?,
        append: Boolean,
    ): Boolean {
        return writeFileFromString(FileUtils.getFileByPath(filePath), content, append)
    }

    /**
     * Write file from string.
     *
     * @param file The file.
     * @param content The string of content.
     * @param append True to append, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromString(
        file: File?,
        content: String?,
        append: Boolean = false,
    ): Boolean {
        if (file == null || content == null) return false
        if (!FileUtils.createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }

        return runCatching {
            BufferedWriter(FileWriter(file, append)).use { bw ->
                bw.write(content)
                return@runCatching true
            }
        }
            .onFailure { e ->
                if (e !is IOException) throw e
                e.printStackTrace()
            }
            .getOrDefault(false)
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ // the divide line of
    // write and read
    /**
     * Return the lines in file.
     *
     * @param filePath The path of file. @ return the lines in file
     */
    fun readFile2List(filePath: String?): List<String?>? {
        return readFile2List(FileUtils.getFileByPath(filePath), null)
    }

    /**
     * Return the lines in file.
     *
     * @param filePath The path of file.
     * @param charsetName The name of charset.
     * @return the lines in file
     */
    fun readFile2List(filePath: String?, charsetName: String?): List<String?>? {
        return readFile2List(FileUtils.getFileByPath(filePath), charsetName)
    }

    /**
     * Return the lines in file.
     *
     * @param file The file.
     * @return the lines in file
     */
    fun readFile2List(file: File?): List<String?>? {
        return readFile2List(file, 0, 0x7FFFFFFF, null)
    }

    /**
     * Return the lines in file.
     *
     * @param file The file.
     * @param charsetName The name of charset.
     * @return the lines in file
     */
    fun readFile2List(file: File?, charsetName: String?): List<String?>? {
        return readFile2List(file, 0, 0x7FFFFFFF, charsetName)
    }

    /**
     * Return the lines in file.
     *
     * @param filePath The path of file.
     * @param st The line's index of start.
     * @param end The line's index of end.
     * @return the lines in file
     */
    fun readFile2List(filePath: String?, st: Int, end: Int): List<String?>? {
        return readFile2List(FileUtils.getFileByPath(filePath), st, end, null)
    }

    /**
     * Return the lines in file.
     *
     * @param filePath The path of file.
     * @param st The line's index of start.
     * @param end The line's index of end.
     * @param charsetName The name of charset.
     * @return the lines in file
     */
    fun readFile2List(
        filePath: String?,
        st: Int,
        end: Int,
        charsetName: String?,
    ): List<String?>? {
        return readFile2List(FileUtils.getFileByPath(filePath), st, end, charsetName)
    }

    /**
     * Return the lines in file.
     *
     * @param file The file.
     * @param st The line's index of start.
     * @param end The line's index of end.
     * @param charsetName The name of charset.
     * @return the lines in file
     */
    fun readFile2List(
        file: File?,
        st: Int,
        end: Int,
        charsetName: String? = null,
    ): List<String?>? {
        if (!FileUtils.isFileExists(file)) return null
        if (st > end) return null
        return runCatching {
            FileInputStream(file).use { input ->
                val charset =
                    if (charsetName.isNullOrBlank()) {
                        java.nio.charset.Charset.defaultCharset()
                    } else {
                        java.nio.charset.Charset.forName(charsetName)
                    }
                input.bufferedReader(charset).use { reader ->
                    val lines = mutableListOf<String?>()
                    var lineNumber = 1
                    while (lineNumber <= end) {
                        val line = reader.readLine() ?: break
                        if (lineNumber >= st) lines.add(line)
                        lineNumber++
                    }
                    lines
                }
            }
        }
            .onFailure { error ->
                if (error !is IOException) throw error
                error.printStackTrace()
            }
            .getOrDefault(null)
    }

    /**
     * Return the string in file.
     *
     * @param filePath The path of file.
     * @return the string in file
     */
    fun readFile2String(filePath: String?): String? {
        return readFile2String(FileUtils.getFileByPath(filePath), null)
    }

    /**
     * Return the string in file.
     *
     * @param filePath The path of file.
     * @param charsetName The name of charset.
     * @return the string in file
     */
    fun readFile2String(filePath: String?, charsetName: String?): String? {
        return readFile2String(FileUtils.getFileByPath(filePath), charsetName)
    }

    /**
     * Return the string in file.
     *
     * @param file The file.
     * @param charsetName The name of charset.
     * @return the string in file
     */
    fun readFile2String(
        file: File?,
        charsetName: String? = null,
    ): String? {
        val bytes: ByteArray = readFile2BytesByStream(file) ?: return null
        if (charsetName.isNullOrBlank()) {
            return String(bytes)
        } else {
            runCatching {
                return String(bytes, java.nio.charset.Charset.forName(charsetName))
            }
                .getOrElse { e ->
                    if (e !is UnsupportedEncodingException) throw e
                    e.printStackTrace()
                    return ""
                }
        }
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ //
    // readFile2BytesByStream without progress
    /**
     * Return the bytes in file by stream.
     *
     * @param filePath The path of file. @ return the bytes in file
     */
    fun readFile2BytesByStream(filePath: String?): ByteArray? {
        return readFile2BytesByStream(FileUtils.getFileByPath(filePath), null)
    }

    /**/
    /////////////////////////////////////////////////////////////////////// */ //
    // readFile2BytesByStream with progress
    /**
     * Return the bytes in file by stream.
     *
     * @param filePath The path of file.
     * @param listener The progress update listener. @ return the bytes in file
     */
    fun readFile2BytesByStream(
        filePath: String?,
        listener: OnProgressUpdateListener?,
    ): ByteArray? {
        return readFile2BytesByStream(FileUtils.getFileByPath(filePath), listener)
    }

    /**
     * Return the bytes in file by stream.
     *
     * @param file The file.
     * @param listener The progress update listener.
     * @return the bytes in file
     */
    fun readFile2BytesByStream(
        file: File?,
        listener: OnProgressUpdateListener? = null,
    ): ByteArray? {
        if (!FileUtils.isFileExists(file)) return null
        return runCatching {
            val ins: InputStream = BufferedInputStream(FileInputStream(file), sBufferSize)
            runCatching {
                ins.use { ins ->
                    ByteArrayOutputStream().use { os ->
                        val b = ByteArray(sBufferSize)
                        var len: Int
                        if (listener == null) {
                            while ((ins.read(b, 0, sBufferSize).also { len = it }) != -1) {
                                os.write(b, 0, len)
                            }
                        } else {
                            val totalSize: Double = ins.available().toDouble()
                            var curSize = 0
                            listener.onProgressUpdate(0.0)
                            while ((ins.read(b, 0, sBufferSize).also { len = it }) != -1) {
                                os.write(b, 0, len)
                                curSize += len
                                listener.onProgressUpdate(curSize / totalSize)
                            }
                        }
                        return@runCatching os.toByteArray()
                    }
                }
            }
                .getOrElse { e ->
                    if (e !is IOException) throw e
                    e.printStackTrace()
                    return@runCatching null
                }
        }
            .onFailure { e ->
                if (e !is FileNotFoundException) throw e
                e.printStackTrace()
            }
            .getOrDefault(null)
    }

    /**
     * Return the bytes in file by channel.
     *
     * @param filePath The path of file.
     * @return the bytes in file
     */
    fun readFile2BytesByChannel(filePath: String?): ByteArray? {
        return readFile2BytesByChannel(FileUtils.getFileByPath(filePath))
    }

    /**
     * Return the bytes in file by channel.
     *
     * @param file The file.
     * @return the bytes in file
     */
    fun readFile2BytesByChannel(file: File?): ByteArray? {
        if (!FileUtils.isFileExists(file)) return null

        return runCatching {
            RandomAccessFile(file, "r").channel.use { fc ->
                if (fc == null) {
                    Log.e("FileIOUtils", "fc is null.")
                    return@runCatching ByteArray(0)
                }
                val byteBuffer: ByteBuffer = ByteBuffer.allocate(fc.size().toInt())
                while (true) {
                    if ((fc.read(byteBuffer)) <= 0) break
                }
                return@runCatching byteBuffer.array()
            }
        }
            .onFailure { e ->
                if (e !is IOException) throw e
                e.printStackTrace()
            }
            .getOrDefault(null)
    }

    /**
     * Return the bytes in file by map.
     *
     * @param filePath The path of file.
     * @return the bytes in file
     */
    fun readFile2BytesByMap(filePath: String?): ByteArray? {
        return readFile2BytesByMap(FileUtils.getFileByPath(filePath))
    }

    /**
     * Return the bytes in file by map.
     *
     * @param file The file.
     * @return the bytes in file
     */
    fun readFile2BytesByMap(file: File?): ByteArray? {
        if (!FileUtils.isFileExists(file)) return null

        return runCatching {
            RandomAccessFile(file, "r").channel.use { fc ->
                if (fc == null) {
                    Log.e("FileIOUtils", "fc is null.")
                    return@runCatching ByteArray(0)
                }
                val size = fc.size().toInt()
                val mbb: MappedByteBuffer =
                    fc.map(FileChannel.MapMode.READ_ONLY, 0, size.toLong()).load()
                val result = ByteArray(size)
                mbb.get(result, 0, size)
                return@runCatching result
            }
        }
            .onFailure { e ->
                if (e !is IOException) throw e
                e.printStackTrace()
            }
            .getOrDefault(null)
    }

    /**
     * Set the buffer's size.
     *
     * Default size equals 8192 bytes.
     *
     * @param bufferSize The buffer's size.
     */
    fun setBufferSize(bufferSize: Int) {
        sBufferSize = bufferSize
    }
}
