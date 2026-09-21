@file:Suppress("unused")

package org.muc.mold.utils.util

import android.Manifest.permission.KILL_BACKGROUND_PROCESSES
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.muc.mold.utils.util.ProcessUtils.foregroundProcessName
import org.muc.mold.utils.util.ProcessUtils.foregroundProcessNameAsync
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.Serializable
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/10/18
 * desc  : utils about process
 * </pre>
 */
object ProcessUtils {

    private const val TAG = "ProcessUtils"

    /** UsageStats 查询窗口：向前回溯 7 天。 */
    private const val USAGE_STATS_WINDOW_MS = 86_400_000L * 7

    // ====================================================================================
    //  前台进程
    // ====================================================================================

    /**
     * Return the foreground process name.
     *
     * Target APIs greater than 21 must hold
     * `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`
     *
     * 同步版本，内部有多次 binder 调用，UI 线程慎用；建议改用 [foregroundProcessNameAsync]。
     *
     * @return the foreground process name；查询不到时返回 `""`
     */
    val foregroundProcessName
        @RequiresPermission(anyOf = ["android.permission.PACKAGE_USAGE_STATS"])
        get() = getForegroundProcessNameInternal()

    /**
     * 协程版 [foregroundProcessName]，在 [Dispatchers.IO] 执行。
     *
     * @return the foreground process name；查询不到时返回 `""`
     */
    @RequiresPermission(anyOf = ["android.permission.PACKAGE_USAGE_STATS"])
    suspend fun foregroundProcessNameAsync() = withContext(Dispatchers.IO) { getForegroundProcessNameInternal() }

    @RequiresPermission(anyOf = ["android.permission.PACKAGE_USAGE_STATS"])
    private fun getForegroundProcessNameInternal(): Serializable? {
        val am = Utils.app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // 1. 先尝试 RunningAppProcesses（低成本路径）
        am.runningAppProcesses?.firstOrNull {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }?.let { return it.processName }

        // 2. API 22+ 走 UsageStatsManager
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) return ""

        val pm = Utils.app.packageManager
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        val list: List<ResolveInfo> =
            pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Log.i(TAG, list.toString())
        if (list.isEmpty()) {
            Log.i(TAG, "getForegroundProcessName: noun of access to usage information.")
            return ""
        }

        return runCatching {
            val info: ApplicationInfo =
                pm.getApplicationInfo(Utils.app.packageName, 0)
            val aom = Utils.app.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

            // 统一一次权限检查
            val opAllowed = aom.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                info.uid,
                info.packageName,
            ) == AppOpsManager.MODE_ALLOWED

            if (!opAllowed) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Utils.app.startActivity(intent)
                Log.i(TAG, "getForegroundProcessName: refuse to device usage stats.")
                return@runCatching ""
            }

            val usm = Utils.app.getSystemService(Context.USAGE_STATS_SERVICE)
                    as? UsageStatsManager
                ?: return@runCatching ""

            val endTime = System.currentTimeMillis()
            val beginTime = endTime - USAGE_STATS_WINDOW_MS
            val usageStatsList = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                beginTime,
                endTime,
            )
            if (usageStatsList.isNullOrEmpty()) return@runCatching ""

            usageStatsList.maxByOrNull(UsageStats::getLastTimeUsed)?.packageName.orEmpty()
        }
    }

    // ====================================================================================
    //  后台进程
    // ====================================================================================

    /**
     * Return all background processes.
     *
     * Must hold `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
     *
     * @return all background processes
     */
    @get:RequiresPermission(KILL_BACKGROUND_PROCESSES)
    val allBackgroundProcesses: Set<String>
        get() = buildSet {
            val am = Utils.app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.runningAppProcesses?.forEach { addAll(it.pkgList) }
        }

    /**
     * Kill all background processes.
     *
     * Must hold `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
     *
     * @return background processes were killed
     */
    @RequiresPermission(KILL_BACKGROUND_PROCESSES)
    fun killAllBackgroundProcesses(): Set<String> {
        val am = Utils.app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val killed = HashSet<String>()

        am.runningAppProcesses?.forEach { proc ->
            proc.pkgList.forEach { pkg ->
                am.killBackgroundProcesses(pkg)
                killed += pkg
            }
        }

        // 复查，移出已被系统彻底杀掉的
        am.runningAppProcesses?.forEach { proc ->
            proc.pkgList.forEach(killed::remove)
        }
        return killed
    }

    /**
     * Kill background processes.
     *
     * Must hold `<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />`
     *
     * @param packageName The name of the package.
     * @return `true`: success<br></br>`false`: fail
     */
    @RequiresPermission(KILL_BACKGROUND_PROCESSES)
    fun killBackgroundProcesses(packageName: String): Boolean {
        val am = Utils.app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        am.runningAppProcesses?.takeIf { it.isNotEmpty() }?.forEach { proc ->
            if (packageName in proc.pkgList) {
                am.killBackgroundProcesses(packageName)
            }
        }

        val after = am.runningAppProcesses
        if (after.isNullOrEmpty()) return true
        return after.none { packageName in it.pkgList }
    }

    // ====================================================================================
    //  当前进程
    // ====================================================================================

    /**
     * Return whether app running in the main process.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isMainProcess: Boolean
        get() = Utils.app.packageName == currentProcessName.getOrNull()

    /**
     * Return the name of current process.
     *
     * 依次尝试三条路径：`/proc/<pid>/cmdline` → ActivityManager → 反射，任一成功即返回。
     *
     * @return the name of current process
     */
    val currentProcessName
        get() = getCurrentProcessNameByFile().takeIf { it.getOrNull().isNullOrBlank().not() }
            ?: getCurrentProcessNameByAms().takeIf { it.getOrNull().isNullOrBlank().not() }
            ?: getCurrentProcessNameByReflect()

    /**
     * 通过读取 `/proc/<pid>/cmdline` 获取当前进程名。
     * 这是最可靠、权限要求最低的方案，但部分定制 ROM 上会失败。
     */
    private fun getCurrentProcessNameByFile() = runCatching {
        val file = File("/proc/${android.os.Process.myPid()}/cmdline")
        BufferedReader(FileReader(file)).use { reader ->
            reader.readLine()?.trim().orEmpty()
        }
    }

    /**
     * 通过 ActivityManager 遍历所有进程，匹配当前 pid 得到进程名。
     */
    private fun getCurrentProcessNameByAms() = runCatching {
        val am = Utils.app.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return@runCatching ""
        val pid = android.os.Process.myPid()
        am.runningAppProcesses
            ?.firstOrNull { it.pid == pid }
            ?.processName
            .orEmpty()
    }

    /**
     * 通过反射链 `Application.mLoadedApk.mActivityThread.getProcessName()` 获取进程名。
     */
    private fun getCurrentProcessNameByReflect() = runCatching {
        val app: Application = Utils.app

        val loadedApkField: Field = app.javaClass.getField("mLoadedApk")
            .apply { isAccessible = true }
        val loadedApk: Any = loadedApkField.get(app) ?: return@runCatching ""

        val activityThreadField: Field =
            loadedApk.javaClass.getDeclaredField("mActivityThread")
                .apply { isAccessible = true }
        val activityThread: Any = activityThreadField.get(loadedApk) ?: return@runCatching ""

        val getProcessName: Method =
            activityThread.javaClass.getDeclaredMethod("getProcessName")
        getProcessName.invoke(activityThread) as? String ?: ""
    }
}