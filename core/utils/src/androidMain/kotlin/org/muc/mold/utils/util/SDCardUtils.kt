@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import androidx.annotation.WorkerThread
import java.io.File
import java.lang.reflect.Method

/**
 * SD 卡工具类 —— 提供内外置存储路径、空间信息、挂载状态检测等能力。
 *
 * 支持 Android 全版本适配（包括分区存储 / SAF），通过反射兼容各厂商实现。
 *
 * 原始作者：Muc
 * 优化：补全 KDoc、[runCatching] → [Result]、默认参数合并重载
 */
object SDCardUtils {

    val isSDCardEnableByEnvironment get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    /**
     * 拼接 SD 卡根路径与指定相对路径。
     *
     * @param relativePath 相对路径（可为 `null`，表示仅取根路径）
     * @return 完整绝对路径
     */
    @JvmOverloads
    fun getSdCardPath(relativePath: String? = null): String {
        val rootPath = getSdCardRootPath()
        return if (relativePath.isNullOrEmpty()) {
            rootPath
        } else {
            "$rootPath${File.separator}$relativePath"
        }
    }

    /**
     * 获取外部存储根路径（如 `/storage/emulated/0`）。
     */
    fun getSdCardRootPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    // ═══════════════════════════════════════════════
    // 公开 API：存储路径（公有目录 / 私有目录）
    // ═══════════════════════════════════════════════

    /**
     * 获取外部公有目录路径。
     *
     * 示例：`SDCardUtils.getSdCardPublicDirPath(Environment.DIRECTORY_DCIM)` → `.../DCIM`
     *
     * @param dirType [Environment.DIRECTORY_*] 常量
     * @param relativePath 子路径（可选）
     */
    @JvmOverloads
    fun getSdCardPublicDirPath(
        dirType: String,
        relativePath: String? = null,
    ): String {
        val root = Environment.getExternalStoragePublicDirectory(dirType).absolutePath
        return if (relativePath.isNullOrEmpty()) root else "$root${File.separator}$relativePath"
    }

    /**
     * 获取外部私有目录路径（`Android/data/<pkg>/files`）。
     *
     * @param relativePath 子路径（可选）
     */
    @JvmOverloads
    fun getSdCardPrivateDirPath(relativePath: String? = null): String {
        return getSdCardPrivatePath(Utils.app, relativePath)
    }

    /**
     * 获取外部私有目录路径（指定 Context）。
     *
     * @param context      上下文
     * @param relativePath 子路径（可选）
     */
    @JvmOverloads
    fun getSdCardPrivatePath(
        context: Context,
        relativePath: String? = null,
    ): String {
        val root = context.getExternalFilesDir(null)?.absolutePath
            ?: "${getSdCardRootPath()}${File.separator}Android${File.separator}data${File.separator}${context.packageName}${File.separator}files"
        return if (relativePath.isNullOrEmpty()) root else "$root${File.separator}$relativePath"
    }

    /**
     * 获取外部私有缓存目录路径。
     *
     * @param context      上下文
     * @param relativePath 子路径（可选）
     */
    @JvmOverloads
    fun getSdCardPrivateCachePath(
        context: Context,
        relativePath: String? = null,
    ): String {
        val root = context.externalCacheDir?.absolutePath
            ?: "${getSdCardRootPath()}${File.separator}Android${File.separator}data${File.separator}${context.packageName}${File.separator}cache"
        return if (relativePath.isNullOrEmpty()) root else "$root${File.separator}$relativePath"
    }

    // ═══════════════════════════════════════════════
    // 公开 API：可移除 SD 卡路径（Result<T> 替代 runCatching）
    // ═══════════════════════════════════════════════

    /**
     * 获取可移除 SD 卡根路径（如 TF 卡）。
     *
     * 此方法通过 [StorageManager] + 反射获取路径，**可能较耗时**，
     * 建议在后台线程/协程中调用。
     *
     * @param context 上下文
     * @param isGetFullPath `true` 返回完整路径，`false` 仅返回卷名。默认 `true`。
     * @return [Result.success] 包含路径字符串；[Result.failure] 包含异常
     */
    @WorkerThread
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun getSdCardPathForRemovable(
        context: Context,
        isGetFullPath: Boolean = true,
    ): Result<String> {
        return runCatching {
            val sm = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")

            // 反射获取 getPath 方法
            val getPath: Method = storageVolumeClazz.getMethod("getPath")
            // 反射获取 isRemovable 方法
            val isRemovable: Method = storageVolumeClazz.getMethod("isRemovable")
            // 反射获取 getVolumeList 方法
            val getVolumeList: Method = sm.javaClass.getMethod("getVolumeList")
            val volumes = getVolumeList.invoke(sm) as Array<*>
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+：遍历 StorageVolume
                for (volume in volumes) {
                    val path = getPath.invoke(volume) as String
                    val removable = isRemovable.invoke(volume) as Boolean
                    if (removable && path.isNotEmpty()) {
                        return@runCatching if (isGetFullPath) path else path.substringAfterLast(File.separator)
                    }
                }
            } else {
                // Android 10 及以下：StorageVolume 可能没有 getPath，备用反射 getDirectory
                try {
                    val getDirectory: Method = storageVolumeClazz.getMethod("getDirectory")
                    for (volume in volumes) {
                        val removable = isRemovable.invoke(volume) as Boolean
                        if (removable) {
                            val dir = getDirectory.invoke(volume) as File
                            val path = dir.absolutePath
                            if (path.isNotEmpty()) {
                                return@runCatching if (isGetFullPath) path else path.substringAfterLast(File.separator)
                            }
                        }
                    }
                } catch (_: NoSuchMethodException) {
                    // 备用方案：检查 StorageVolume 子类
                }
                // 最终兜底：通过 getPath
                for (volume in volumes) {
                    val path = getPath.invoke(volume) as String
                    val removable = isRemovable.invoke(volume) as Boolean
                    if (removable && path.isNotEmpty()) {
                        return@runCatching if (isGetFullPath) path else path.substringAfterLast(File.separator)
                    }
                }
            }
            throw IllegalStateException("No removable SD card found")
        }
    }

    /**
     * 获取可移除 SD 卡的公有目录路径。
     *
     * @param dirType      [Environment.DIRECTORY_*] 常量
     * @param relativePath 子路径（可选）
     * @return 全路径字符串
     */
    @JvmOverloads
    fun getSdCardPathForRemovablePublicDirPath(
        context: Context,
        dirType: String,
        relativePath: String? = null,
    ): Result<String> {
        return getSdCardPathForRemovable(context, isGetFullPath = true).map { rootPath ->
            val base = "$rootPath${File.separator}$dirType"
            if (relativePath.isNullOrEmpty()) base else "$base${File.separator}$relativePath"
        }
    }

    /**
     * 获取可移除 SD 卡的私有目录路径。
     *
     * @param context      上下文
     * @param relativePath 子路径（可选）
     * @return 全路径字符串
     */
    @JvmOverloads
    fun getSdCardPathForRemovablePrivatePath(
        context: Context,
        relativePath: String? = null,
    ): Result<String> {
        val pkg = context.packageName
        return getSdCardPathForRemovable(context, isGetFullPath = true).map { rootPath ->
            val base = "$rootPath${File.separator}Android${File.separator}data${File.separator}$pkg${File.separator}files"
            if (relativePath.isNullOrEmpty()) base else "$base${File.separator}$relativePath"
        }
    }

    /**
     * 获取可移除 SD 卡的私有缓存路径。
     *
     * @param context      上下文
     * @param relativePath 子路径（可选）
     * @return 全路径字符串
     */
    @JvmOverloads
    fun getSdCardPathForRemovablePrivateCachePath(
        context: Context,
        relativePath: String? = null,
    ): Result<String> {
        val pkg = context.packageName
        return getSdCardPathForRemovable(context, isGetFullPath = true).map { rootPath ->
            val base = "$rootPath${File.separator}Android${File.separator}data${File.separator}$pkg${File.separator}cache"
            if (relativePath.isNullOrEmpty()) base else "$base${File.separator}$relativePath"
        }
    }

    // ═══════════════════════════════════════════════
    // 公开 API：存储信息
    // ═══════════════════════════════════════════════

    /**
     * 获取内置存储信息（总空间 / 可用空间 / 已用空间）。
     *
     * @param relativePath 相对路径（可选，指定具体目录）
     * @return 三元组 `Triple(total, free, used)`，单位字节。若路径不可用返回全 0。
     */
    @JvmOverloads
    fun getStorageInfo(relativePath: String? = null): Triple<Long, Long, Long> {
        val state = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED == state) {
            val path = getSdCardPath(relativePath)
            val stat = android.os.StatFs(path)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
            val used = total - free
            Triple(total, free, used)
        } else {
            Triple(0L, 0L, 0L)
        }
    }

    /**
     * 获取可移除 SD 卡存储信息（总空间 / 可用空间 / 已用空间）。
     *
     * @return [Result.success] 包含三元组；[Result.failure] 表示无可移除 SD 卡或读取失败
     */
    fun getStorageInfoForRemovable(context: Context): Result<Triple<Long, Long, Long>> {
        return getSdCardPathForRemovable(context, isGetFullPath = true).map { path ->
            val stat = android.os.StatFs(path)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
            val used = total - free
            Triple(total, free, used)
        }
    }

    // ═══════════════════════════════════════════════
    // 公开 API：挂载状态
    // ═══════════════════════════════════════════════

    /** 内置存储是否已挂载。 */
    val isSdCardMounted: Boolean
        get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    /**
     * 可移除 SD 卡是否已挂载。
     *
     * @param context 上下文
     * @return [Result.success] 包含布尔值；[Result.failure] 表示检测过程异常
     */
    fun isRemovableSdCardMounted(context: Context): Result<Boolean> {
        return getSdCardPathForRemovable(context, isGetFullPath = true).map { path ->
            val state = Environment.getStorageState(File(path))
            Environment.MEDIA_MOUNTED == state
        }
    }
}