@file:Suppress("unused", "DEPRECATION")
package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * 服务工具类 —— 提供服务运行状态查询、启动/停止、ServiceManager 反射访问等能力。
 *
 * 所有返回布尔的方法现在返回 [Result<Boolean>]（替代 [runCatching]），语义更明确。
 *
 * 原始作者：Muc
 * 优化：补全 KDoc、[runCatching] → [Result]、默认参数合并重载
 */
object ServiceUtils {

    // ═══════════════════════════════════════════════
    // 内部工具：获取 ActivityManager
    // ═══════════════════════════════════════════════

    private val activityManager: ActivityManager
        get() = Utils.app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // ═══════════════════════════════════════════════
    // 公开 API：服务运行状态
    // ═══════════════════════════════════════════════

    /**
     * 判断某服务是否正在运行。
     *
     * @param clazz 服务类
     * @return [Result.success] 包含 Boolean；[Result.failure] 包含异常
     */
    fun isServiceRunning(clazz: Class<*>): Result<Boolean> = runCatching {
        val className = clazz.name
        activityManager.getRunningServices(Integer.MAX_VALUE)
            ?.any { it.service.className == className }
            ?: false
    }


    /**
     * 获取所有正在运行的服务列表。
     *
     * @param maxNum 最大返回条数，默认 [Integer.MAX_VALUE]
     * @return [Result.success] 包含列表；[Result.failure] 包含异常
     */
    fun getAllRunningServiceList(
        maxNum: Int = Integer.MAX_VALUE,
    ): Result<List<RunningServiceInfo>> = runCatching {
        activityManager.getRunningServices(maxNum) ?: emptyList()
    }

    // ═══════════════════════════════════════════════
    // 公开 API：启动 / 停止服务
    // ═══════════════════════════════════════════════

    /**
     * 启动服务。
     *
     * @param clazz 服务类
     */
    fun startService(clazz: Class<*>) {
        val intent = Intent(Utils.app, clazz)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Utils.app.startForegroundService(intent)
        } else {
            Utils.app.startService(intent)
        }
    }

    /**
     * 停止服务。
     *
     * @param clazz 服务类
     * @return [Result.success] 包含是否成功停止；[Result.failure] 包含异常
     */
    fun stopService(clazz: Class<*>): Result<Boolean> = runCatching {
        val intent = Intent(Utils.app, clazz)
        Utils.app.stopService(intent)
    }

    // ═══════════════════════════════════════════════
    // 公开 API：ServiceManager 反射（系统级）
    // ═══════════════════════════════════════════════

    /**
     * 通过反射获取 ServiceManager 中注册的系统服务。
     *
     * ⚠️ 需 root 或系统签名，普通应用调用通常失败。
     *
     * @param serviceName 服务名（如 `"activity"`）
     * @return [Result.success] 包含服务对象；[Result.failure] 包含异常
     */
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun getService(serviceName: String): Result<Any?> = runCatching {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
        getServiceMethod.invoke(null, serviceName)
    }

    /**
     * 通过反射获取 ServiceManager 中注册的系统服务列表。
     *
     * ⚠️ 需 root 或系统签名，普通应用调用通常失败。
     *
     * @return [Result.success] 包含服务名列表；[Result.failure] 包含异常
     */
    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun getAllServiceList(): Result<List<String>> = runCatching {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val listServicesMethod = serviceManagerClass.getMethod("listServices")
        val services = listServicesMethod.invoke(null) as Array<*>
        services.filterIsInstance<String>()
    }
}