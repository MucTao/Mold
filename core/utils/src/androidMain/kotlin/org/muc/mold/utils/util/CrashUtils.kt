@file:Suppress("unused")

package org.muc.mold.utils.util

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/09/27
 * desc  : utils about crash
 * </pre> *
 */
object CrashUtils {
    /**/
    /////////////////////////////////////////////////////////////////////// */ // interface
    /**/
    /////////////////////////////////////////////////////////////////////// */
    interface OnCrashListener {
        fun onCrash(crashInfo: CrashInfo?)
    }

    class CrashInfo(time: String?, val throwable: Throwable?) {
        private val mFileHeadProvider: FileHead = FileHead("Crash")

        init {
            mFileHeadProvider.addFirst("Time Of Crash", time)
        }

        fun addExtraHead(extraHead: Map<String?, String?>?) {
            mFileHeadProvider.append(extraHead)
        }

        fun addExtraHead(key: String?, value: String?) {
            mFileHeadProvider.append(key, value)
        }

        override
        fun toString(): String {
            return mFileHeadProvider.toString() + ThrowableUtils.getFullStackTrace(this.throwable)
        }
    }

    private val FILE_SEP: String = File.separator

    private val DEFAULT_UNCAUGHT_EXCEPTION_HANDLER: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()


    /**
     * Initialization
     *
     * @param crashDir The directory of saving crash information.
     * @param onCrashListener The crash listener.
     */
    fun init(crashDir: File, onCrashListener: OnCrashListener? = null) {
        init(crashDir.absolutePath, onCrashListener)
    }

    /**
     * Initialization
     *
     * @param crashDirPath The directory's path of saving crash information.
     * @param onCrashListener The crash listener.
     */
    fun init(
        crashDirPath: String?,
        onCrashListener: OnCrashListener? = null,
    ) {
        val dirPath = if (crashDirPath.isNullOrBlank()) {
            if (
                SDCardUtils.isSDCardEnableByEnvironment && Utils.app.getExternalFilesDir(null) != null
            ) {
                "${Utils.app.getExternalFilesDir(null)}${FILE_SEP}crash" + FILE_SEP
            } else {
                "${Utils.app.filesDir}${FILE_SEP}crash" + FILE_SEP
            }
        } else {
            if (crashDirPath.endsWith(FILE_SEP)) crashDirPath else crashDirPath + FILE_SEP
        }
        Thread.setDefaultUncaughtExceptionHandler(getUncaughtExceptionHandler(dirPath, onCrashListener))
    }

    private fun getUncaughtExceptionHandler(
        dirPath: String?,
        onCrashListener: OnCrashListener?,
    ): Thread.UncaughtExceptionHandler {
        return Thread.UncaughtExceptionHandler { t, e ->
            val time: String? = SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.getDefault()).format(Date())
            val info = CrashInfo(time, e)
            val crashFile = (dirPath + time) + ".txt"
            FileIOUtils.writeFileFromString(crashFile, info.toString(), true)

            DEFAULT_UNCAUGHT_EXCEPTION_HANDLER?.uncaughtException(t, e)
            onCrashListener?.onCrash(info)
        }
    }
}
