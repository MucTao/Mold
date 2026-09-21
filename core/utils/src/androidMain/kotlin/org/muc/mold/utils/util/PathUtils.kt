@file:Suppress("unused")

package org.muc.mold.utils.util

import android.os.Build
import android.os.Environment
import java.io.File

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2018/04/15
 * desc  : utils about path
 * </pre> *
 */
object PathUtils {
    private val SEP: Char = File.separatorChar

    private fun getLegalSegment(segment: String): String {
        var st = -1
        var end = -1
        val charArray = segment.toCharArray()
        for (i in charArray.indices) {
            val c = charArray[i]
            if (c != SEP) {
                if (st == -1) {
                    st = i
                }
                end = i
            }
        }
        if (st in 0..end) {
            return segment.substring(st, end + 1)
        }
        throw IllegalArgumentException("segment of <$segment> is illegal")
    }

    val rootPath: String?
        /**
         * @return the path of /system
         */
        get() = getAbsolutePath(Environment.getRootDirectory())

    val dataPath: String?
        /**
         * @return the path of /data
         */
        get() = getAbsolutePath(Environment.getDataDirectory())

    val downloadCachePath: String?
        /**
         * @return the path of /cache
         */
        get() =
            getAbsolutePath(Environment.getDownloadCacheDirectory())

    val internalAppDataPath: String?
        /**
         * @return the path of /data/data/package
         */
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                return Utils.app.applicationInfo.dataDir
            }
            return getAbsolutePath(Utils.app.dataDir)
        }

    val internalAppCodeCacheDir: String?
        /**
         * Return the path of /data/data/package/code_cache.
         *
         * @return the path of /data/data/package/code_cache
         */
        get() {
            return getAbsolutePath(Utils.app.codeCacheDir)
        }

    val internalAppCachePath: String?
        /**
         * @return the path of /data/data/package/cache
         */
        get() = getAbsolutePath(Utils.app.cacheDir)

    val internalAppDbsPath: String
        /**
         *
         * @return the path of /data/data/package/databases
         */
        get() = Utils.app.applicationInfo.dataDir + "/databases"

    /**
     * @param name The name of database.
     * @return the path of /data/data/package/databases/name
     */
    fun getInternalAppDbPath(name: String?): String? {
        return getAbsolutePath(Utils.app.getDatabasePath(name))
    }

    val internalAppFilesPath: String?
        /**
         * @return the path of /data/data/package/files
         */
        get() = getAbsolutePath(Utils.app.filesDir)

    val internalAppSpPath: String
        /**
         * @return the path of /data/data/package/shared_prefs
         */
        get() = Utils.app.applicationInfo.dataDir + "/shared_prefs"

    val internalAppNoBackupFilesPath: String?
        /**
         * @return the path of /data/data/package/no_backup
         */
        get() {
            return getAbsolutePath(Utils.app.noBackupFilesDir)
        }

    val externalStoragePath: String?
        /**
         * Return the path of /storage/emulated/0.
         *
         * @return the path of /storage/emulated/0
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStorageDirectory()
            )
        }

    val externalMusicPath: String?
        /**
         * Return the path of /storage/emulated/0/Music.
         *
         * @return the path of /storage/emulated/0/Music
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            )
        }

    val externalPodcastsPath: String?
        /**
         * Return the path of /storage/emulated/0/Podcasts.
         *
         * @return the path of /storage/emulated/0/Podcasts
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)
            )
        }

    val externalRingtonesPath: String?
        /**
         * Return the path of /storage/emulated/0/Ringtones.
         *
         * @return the path of /storage/emulated/0/Ringtones
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
            )
        }

    val externalAlarmsPath: String?
        /**
         * Return the path of /storage/emulated/0/Alarms.
         *
         * @return the path of /storage/emulated/0/Alarms
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS)
            )
        }

    val externalNotificationsPath: String?
        /**
         * Return the path of /storage/emulated/0/Notifications.
         *
         * @return the path of /storage/emulated/0/Notifications
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS)
            )
        }

    val externalPicturesPath: String?
        /**
         * Return the path of /storage/emulated/0/Pictures.
         *
         * @return the path of /storage/emulated/0/Pictures
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            )
        }

    val externalMoviesPath: String?
        /**
         * Return the path of /storage/emulated/0/Movies.
         *
         * @return the path of /storage/emulated/0/Movies
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            )
        }

    val externalDownloadsPath: String?
        /**
         * Return the path of /storage/emulated/0/Download.
         *
         * @return the path of /storage/emulated/0/Download
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            )
        }

    val externalDimPath: String?
        /**
         * Return the path of /storage/emulated/0/DCIM.
         *
         * @return the path of /storage/emulated/0/DCIM
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            )
        }

    val externalDocumentsPath: String?
        /**
         * Return the path of /storage/emulated/0/Documents.
         *
         * @return the path of /storage/emulated/0/Documents
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            )
        }

    val externalAppDataPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package.
         *
         * @return the path of /storage/emulated/0/Android/data/package
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            val externalCacheDir: File = Utils.app.externalCacheDir ?: return ""
            return getAbsolutePath(
                externalCacheDir.getParentFile()
            )
        }

    val externalAppCachePath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/cache.
         *
         * @return the path of /storage/emulated/0/Android/data/package/cache
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.externalCacheDir
            )
        }

    val externalAppFilesPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(null)
            )
        }

    val externalAppMusicPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Music.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Music
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            )
        }

    val externalAppPodcastsPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Podcasts.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Podcasts
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_PODCASTS)
            )
        }

    val externalAppRingtonesPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Ringtones.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Ringtones
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_RINGTONES)
            )
        }

    val externalAppAlarmsPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Alarms.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Alarms
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_ALARMS)
            )
        }

    val externalAppNotificationsPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Notifications.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Notifications
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS)
            )
        }

    val externalAppPicturesPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Pictures.
         *
         * @return path of /storage/emulated/0/Android/data/package/files/Pictures
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
        }

    val externalAppMoviesPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Movies.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Movies
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            )
        }

    val externalAppDownloadPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Download.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Download
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            )
        }

    val externalAppDimPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/DCIM.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/DCIM
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_DCIM)
            )
        }

    val externalAppDocumentsPath: String?
        /**
         * Return the path of /storage/emulated/0/Android/data/package/files/Documents.
         *
         * @return the path of /storage/emulated/0/Android/data/package/files/Documents
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(
                Utils.app.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            )
        }

    val externalAppObbPath: String?
        /**
         * @return the path of /storage/emulated/0/Android/obb/package
         */
        get() {
            if (!SDCardUtils.isSDCardEnableByEnvironment) return ""
            return getAbsolutePath(Utils.app.obbDir)
        }

    val rootPathExternalFirst: String?
        get() {
            var rootPath: String? = externalStoragePath
            if (rootPath.isNullOrEmpty()) {
                rootPath = PathUtils.rootPath
            }
            return rootPath
        }

    val appDataPathExternalFirst: String?
        get() {
            var appDataPath: String? = externalAppDataPath
            if (appDataPath.isNullOrEmpty()) {
                appDataPath = internalAppDataPath
            }
            return appDataPath
        }

    val filesPathExternalFirst: String?
        get() {
            var filePath: String? = externalAppFilesPath
            if (filePath.isNullOrEmpty()) {
                filePath = internalAppFilesPath
            }
            return filePath
        }

    val cachePathExternalFirst: String?
        get() {
            var appPath: String? = externalAppCachePath
            if (appPath.isNullOrEmpty()) {
                appPath = internalAppCachePath
            }
            return appPath
        }

    private fun getAbsolutePath(file: File?): String? {
        if (file == null) return ""
        return file.absolutePath
    }
}
