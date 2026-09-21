@file:Suppress("unused","DEPRECATION")

package org.muc.mold.utils.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.runBlocking
import org.muc.mold.utils.util.ConvertUtils.toHashHex
import java.io.File
import kotlin.system.exitProcess

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/02
 * desc  : utils about app
 * </pre> *
 */
object AppUtils {

    /** The application's information. */
    data class AppInfo(
        val packageName: String? = null,
        val name: String? = null,
        val icon: Drawable? = null,
        val packagePath: String? = null,
        val versionName: String? = null,
        val versionCode: Long = 0,
        val minSdkVersion: Int = 0,
        val targetSdkVersion: Int = 0,
        val isSystem: Boolean = false,
    )

    /**
     * Register the status of application changed listener.
     *
     * @param listener The status of application changed listener
     */
    fun registerAppStatusChangedListener(listener: Utils.OnAppStatusChangedListener) {
        UtilsActivityLifecycleImpl.addOnAppStatusChangedListener(listener)
    }

    /**
     * Unregister the status of application changed listener.
     *
     * @param listener The status of application changed listener
     */
    fun unregisterAppStatusChangedListener(listener: Utils.OnAppStatusChangedListener) {
        UtilsActivityLifecycleImpl.removeOnAppStatusChangedListener(listener)
    }

    /**
     * Install the app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param filePath The path of file.
     */
    fun installApp(filePath: String?) {
        installApp(FileUtils.getFileByPath(filePath))
    }

    /**
     * Install the app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param file The file.
     */
    fun installApp(file: File?) {
        if (file == null) return
        val installAppIntent: Intent = IntentUtils.getInstallAppIntent(file) ?: return
        Utils.app.startActivity(installAppIntent)
    }

    /**
     * Install the app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_INSTALL_PACKAGES" />`
     *
     * @param uri The uri.
     */
    fun installApp(uri: Uri?) {
        val installAppIntent: Intent = IntentUtils.getInstallAppIntent(uri) ?: return
        Utils.app.startActivity(installAppIntent)
    }

    /**
     * Uninstall the app.
     *
     * Target APIs greater than 25 must hold `<uses-permission
     * android:name="android.permission.REQUEST_DELETE_PACKAGES" />`
     *
     * @param packageName The name of the package.
     */
    fun uninstallApp(packageName: String?) {
        if (packageName.isNullOrBlank()) return
        Utils.app.startActivity(IntentUtils.getUninstallAppIntent(packageName))
    }

    /**
     * Return whether the app is installed.
     *
     * @param pkgName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppInstalled(pkgName: String?): Boolean {
        if (pkgName.isNullOrBlank()) return false
        val pm: PackageManager = Utils.app.packageManager
        return runCatching {
            pm.getApplicationInfo(pkgName, 0).enabled
        }
            .getOrElse { false }
    }

    /**
     * Return whether the application with root permission.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isAppRoot: Boolean get() = runBlocking { ShellUtils.exec("echo root", true).isSuccess }

    /**
     * Return whether it is a debug application.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isAppDebug: Boolean
        get() = isAppDebug(Utils.app.packageName)

    /**
     * Return whether it is a debug application.
     *
     * @param packageName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppDebug(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val ai: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            return@runCatching (ai.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(false)
    }

    val isAppSystem: Boolean
        /**
         * Return whether it is a system application.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = isAppSystem(Utils.app.packageName)

    /**
     * Return whether it is a system application.
     *
     * @param packageName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppSystem(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val ai: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            return@runCatching (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(false)
    }

    val isAppForeground: Boolean
        /**
         * Return whether application is foreground.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = UtilsActivityLifecycleImpl.isAppForeground

    /**
     * Return whether application is foreground.
     *
     * Target APIs greater than 21 must hold `<uses-permission
     * android:name="android.permission.PACKAGE_USAGE_STATS" />`
     *
     * @param pkgName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppForeground(pkgName: String): Boolean {
        return pkgName.isNotBlank() && pkgName == ProcessUtils.foregroundProcessName
    }

    /**
     * Return whether application is running.
     *
     * @param pkgName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppRunning(pkgName: String?): Boolean {
        if (pkgName.isNullOrBlank()) return false
        val am: ActivityManager? =
            Utils.app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        if (am != null) {
            val taskInfo = am.getRunningTasks(Integer.MAX_VALUE)
            if (!taskInfo.isNullOrEmpty()) {
                for (aInfo in taskInfo) {
                    if (pkgName == aInfo.baseActivity?.packageName) {
                        return true
                    }
                }
            }
            val serviceInfo = am.getRunningServices(Integer.MAX_VALUE)
            if (!serviceInfo.isNullOrEmpty()) {
                for (aInfo in serviceInfo) {
                    if (pkgName == aInfo.service.packageName) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Launch the application.
     *
     * @param packageName The name of the package.
     */
    fun launchApp(packageName: String?) {
        if (packageName.isNullOrBlank()) return
        val launchAppIntent: Intent? = IntentUtils.getLaunchAppIntent(packageName)
        if (launchAppIntent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.")
            return
        }
        Utils.app.startActivity(launchAppIntent)
    }

    /** Relaunch the application. */
    @JvmOverloads
    fun relaunchApp(isKillProcess: Boolean = false) {
        val intent: Intent? = IntentUtils.getLaunchAppIntent(Utils.app.packageName)
        if (intent == null) {
            Log.e("AppUtils", "Didn't exist launcher activity.")
            return
        }
        intent.addFlags(
            (Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        Utils.app.startActivity(intent)
        if (!isKillProcess) return
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(0)
    }

    /** Launch the application's details settings. */
    @JvmOverloads
    fun launchAppDetailsSettings(pkgName: String? = Utils.app.packageName) {
        if (pkgName.isNullOrBlank()) return
        val intent: Intent = IntentUtils.getLaunchAppDetailsSettingsIntent(pkgName, true)
        if (!IntentUtils.isIntentAvailable(intent)) return
        Utils.app.startActivity(intent)
    }

    /**
     * Launch the application's details settings.
     *
     * @param activity The activity.
     * @param requestCode The requestCode.
     */
    fun launchAppDetailsSettings(activity: Activity?, requestCode: Int) {
        launchAppDetailsSettings(activity, requestCode, Utils.app.packageName)
    }

    /**
     * Launch the application's details settings.
     *
     * @param activity The activity.
     * @param requestCode The requestCode.
     * @param pkgName The name of the package.
     */
    fun launchAppDetailsSettings(activity: Activity?, requestCode: Int, pkgName: String?) {
        if (activity == null || pkgName.isNullOrBlank()) return
        val intent: Intent = IntentUtils.getLaunchAppDetailsSettingsIntent(pkgName, false)
        if (!IntentUtils.isIntentAvailable(intent)) return
        activity.startActivityForResult(intent, requestCode)
    }

    /** Exit the application. */
    fun exitApp() {
        ActivityUtils.finishAllActivities()
        exitProcess(0)
    }

    val appIcon: Drawable?
        /**
         * Return the application's icon.
         *
         * @return the application's icon
         */
        get() = getAppIcon(Utils.app.packageName)

    /**
     * Return the application's icon.
     *
     * @param packageName The name of the package.
     * @return the application's icon
     */
    fun getAppIcon(packageName: String?): Drawable? {
        if (packageName.isNullOrBlank()) return null
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo? = pm.getPackageInfo(packageName, 0)
            return@runCatching pi?.applicationInfo?.loadIcon(pm)
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(null)
    }

    val appIconId: Int
        /**
         * Return the application's icon resource identifier.
         *
         * @return the application's icon resource identifier
         */
        get() = getAppIconId(Utils.app.packageName)

    /**
     * Return the application's icon resource identifier.
     *
     * @param packageName The name of the package.
     * @return the application's icon resource identifier
     */
    fun getAppIconId(packageName: String?): Int {
        if (packageName.isNullOrBlank()) return 0
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo? = pm.getPackageInfo(packageName, 0)
            return@runCatching pi?.applicationInfo?.icon ?: 0
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrDefault(0)
    }

    val isFirstTimeInstall: Boolean
        /**
         * Return true if this is the first ever time that the application is installed on the
         * device.
         *
         * @return true if this is the first ever time that the application is installed on the
         *   device.
         */
        get() {
            return runCatching {
                val firstInstallTime: Long =
                    Utils.app
                        .packageManager
                        .getPackageInfo(appPackageName, 0)
                        .firstInstallTime
                val lastUpdateTime: Long =
                    Utils.app
                        .packageManager
                        .getPackageInfo(appPackageName, 0)
                        .lastUpdateTime
                return@runCatching firstInstallTime == lastUpdateTime
            }
                .getOrDefault(false)
        }

    val isAppUpgraded: Boolean
        /**
         * Return true if app was previously installed and this one is an update/upgrade to that
         * one, returns false if this is a fresh installation and not an update/upgrade.
         *
         * @return true if app was previously installed and this one is an update/upgrade to that
         *   one, returns false if this is a fresh installation and not an update/upgrade.
         */
        get() {
            return runCatching {
                val firstInstallTime: Long =
                    Utils.app
                        .packageManager
                        .getPackageInfo(appPackageName, 0)
                        .firstInstallTime
                val lastUpdateTime: Long =
                    Utils.app
                        .packageManager
                        .getPackageInfo(appPackageName, 0)
                        .lastUpdateTime
                return@runCatching firstInstallTime != lastUpdateTime
            }
                .getOrDefault(false)
        }

    /**
     * @return the application's package name
     */
    val appPackageName: String
        get() = Utils.app.packageName

    /**
     * @return the application's name
     */
    val appName: String
        get() = getAppName(Utils.app.packageName)

    /**
     * Return the application's name.
     *
     * @param packageName The name of the package.
     * @return the application's name
     */
    fun getAppName(packageName: String?): String {
        if (packageName.isNullOrBlank()) return ""
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo? = pm.getPackageInfo(packageName, 0)
            return@runCatching if (pi == null) ""
            else pi.applicationInfo?.loadLabel(pm).toString()
        }.onFailure { e ->
            e.printStackTrace()
        }
            .getOrDefault("")
    }

    val appPath: String
        /**
         * Return the application's path.
         *
         * @return the application's path
         */
        get() = getAppPath(Utils.app.packageName)

    /**
     * Return the application's path.
     *
     * @param packageName The name of the package.
     * @return the application's path
     */
    fun getAppPath(packageName: String?): String {
        if (packageName.isNullOrBlank()) return ""
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo? = pm.getPackageInfo(packageName, 0)
            return@runCatching pi?.applicationInfo?.sourceDir.toString()
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrDefault("")
    }

    val appVersionName: String
        /**
         * Return the application's version name.
         *
         * @return the application's version name
         */
        get() = getAppVersionName(Utils.app.packageName)

    /**
     * Return the application's version name.
     *
     * @param packageName The name of the package.
     * @return the application's version name
     */
    fun getAppVersionName(packageName: String?): String {
        if (packageName.isNullOrBlank()) return ""
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo? = pm.getPackageInfo(packageName, 0)
            return@runCatching pi?.versionName.toString()
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrDefault("")
    }

    val appVersionCode: Int
        /**
         * Return the application's version code.
         *
         * @return the application's version code
         */
        get() = getAppVersionCode(Utils.app.packageName)

    /**
     * Return the application's version code.
     *
     * @param packageName The name of the package.
     * @return the application's version code
     */
    fun getAppVersionCode(packageName: String?): Int {
        if (packageName.isNullOrBlank()) return -1
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo? = pm.getPackageInfo(packageName, 0)
            return@runCatching pi?.versionCode ?: -1
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(-1)
    }

    val appMinSdkVersion: Int
        /**
         * Return the application's minimum sdk version code.
         *
         * @return the application's minimum sdk version code
         */
        get() = getAppMinSdkVersion(Utils.app.packageName)

    /**
     * Return the application's minimum sdk version code.
     *
     * @param packageName The name of the package.
     * @return the application's minimum sdk version code
     */
    fun getAppMinSdkVersion(packageName: String?): Int {
        if (packageName.isNullOrBlank()) return -1
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return -1
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo = pm.getPackageInfo(packageName, 0) ?: return@runCatching -1
            val ai: ApplicationInfo? = pi.applicationInfo
            return@runCatching ai?.minSdkVersion ?: -1
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(-1)
    }

    val appTargetSdkVersion: Int
        /**
         * Return the application's target sdk version code.
         *
         * @return the application's target sdk version code
         */
        get() = getAppTargetSdkVersion(Utils.app.packageName)

    /**
     * Return the application's target sdk version code.
     *
     * @param packageName The name of the package.
     * @return the application's target sdk version code
     */
    fun getAppTargetSdkVersion(packageName: String?): Int {
        if (packageName.isNullOrBlank()) return -1
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            val pi: PackageInfo = pm.getPackageInfo(packageName, 0) ?: return@runCatching -1
            val ai: ApplicationInfo? = pi.applicationInfo
            return@runCatching ai?.targetSdkVersion ?: -1
        }
            .onFailure { e ->
                e.printStackTrace()
            }
            .getOrDefault(-1)
    }

    /**
     * @return the application's signature
     */
    val appSignatures: Array<Signature>
        get() = getAppSignatures(Utils.app.packageName)

    /**
     * @param packageName The name of the package.
     * @return the application's signature
     */
    fun getAppSignatures(packageName: String?): Array<Signature> {
        if (packageName.isNullOrBlank()) return emptyArray()
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val pi: PackageInfo =
                    pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES) ?: return emptyArray()
                val signingInfo: SigningInfo = pi.signingInfo ?: return emptyArray()
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                val pi: PackageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES) ?: return emptyArray()
                pi.signatures
            }
        }.onFailure { e ->
            e.printStackTrace()
        }.getOrDefault(emptyArray()) ?: emptyArray()
    }

    /**
     * Return the application's signature.
     *
     * @param file The file.
     * @return the application's signature
     */
    fun getAppSignatures(file: File?): Array<Signature> {
        if (file == null) return emptyArray()
        val pm: PackageManager = Utils.app.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val pi: PackageInfo? =
                pm.getPackageArchiveInfo(
                    file.absolutePath,
                    PackageManager.GET_SIGNING_CERTIFICATES,
                )
            if (pi == null) return emptyArray()

            val signingInfo: SigningInfo = pi.signingInfo ?: return emptyArray()
            return if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            val pi: PackageInfo =
                pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNATURES) ?: return emptyArray()
            return pi.signatures ?: emptyArray()
        }
    }

    val appSignaturesSHA1: List<String?>
        /**
         * Return the application's signature for SHA1 value.
         *
         * @return the application's signature for SHA1 value
         */
        get() = getAppSignaturesSHA1(Utils.app.packageName)

    /**
     * Return the application's signature for SHA1 value.
     *
     * @param packageName The name of the package.
     * @return the application's signature for SHA1 value
     */
    fun getAppSignaturesSHA1(packageName: String?): List<String?> {
        return getAppSignaturesHash(packageName, "SHA1")
    }

    val appSignaturesSHA256: List<String?>
        /**
         * Return the application's signature for SHA256 value.
         *
         * @return the application's signature for SHA256 value
         */
        get() = getAppSignaturesSHA256(Utils.app.packageName)

    /**
     * Return the application's signature for SHA256 value.
     *
     * @param packageName The name of the package.
     * @return the application's signature for SHA256 value
     */
    fun getAppSignaturesSHA256(packageName: String?): List<String?> {
        return getAppSignaturesHash(packageName, "SHA256")
    }

    val appSignaturesMD5: List<String?>
        /**
         * Return the application's signature for MD5 value.
         *
         * @return the application's signature for MD5 value
         */
        get() = getAppSignaturesMD5(Utils.app.packageName)

    /**
     * Return the application's signature for MD5 value.
     *
     * @param packageName The name of the package.
     * @return the application's signature for MD5 value
     */
    fun getAppSignaturesMD5(packageName: String?): List<String?> {
        return getAppSignaturesHash(packageName, "MD5")
    }

    val appUid: Int
        /**
         * Return the application's user-ID.
         *
         * @return the application's signature for MD5 value
         */
        get() = getAppUid(Utils.app.packageName)

    /**
     * Return the application's user-ID.
     *
     * @param pkgName The name of the package.
     * @return the application's signature for MD5 value
     */
    fun getAppUid(pkgName: String?): Int {
        if (pkgName.isNullOrBlank()) return -1
        return runCatching {
            return@runCatching Utils.app.packageManager.getApplicationInfo(pkgName, 0).uid
        }.onFailure { e -> e.printStackTrace() }.getOrDefault(-1)
    }

    private fun getAppSignaturesHash(packageName: String?, algorithm: String): List<String?> {
        val result: ArrayList<String?> = ArrayList()
        if (packageName.isNullOrBlank()) return result
        val signatures: Array<Signature> = getAppSignatures(packageName)
        if (signatures.isEmpty()) return result
        for (signature in signatures) {
            val hash: String =
                signature.toByteArray()
                    .toHashHex(algorithm)
                    .map { it.replace("(?<=[0-9A-F]{2})[0-9A-F]{2}", ":$0") }
                    .getOrDefault("")
            result.add(hash)
        }
        return result
    }

    val appInfo: AppInfo?
        /**
         * Return the application's information.
         *
         * * name of package
         * * icon
         * * name
         * * path of package
         * * version name
         * * version code
         * * minimum sdk version code
         * * target sdk version code
         * * is system
         *
         * @return the application's information
         */
        get() = getAppInfo(Utils.app.packageName)

    /**
     * Return the application's information.
     *
     * * name of package
     * * icon
     * * name
     * * path of package
     * * version name
     * * version code
     * * minimum sdk version code
     * * target sdk version code
     * * is system
     *
     * @param packageName The name of the package.
     * @return the application's information
     */
    fun getAppInfo(packageName: String = Utils.app.packageName): AppInfo? {
        return runCatching {
            val pm: PackageManager = Utils.app.packageManager ?: return@runCatching null
            return@runCatching getBean(pm, pm.getPackageInfo(packageName, 0))
        }.onFailure { e -> e.printStackTrace() }.getOrDefault(null)
    }

    val appsInfo: List<AppInfo?>
        /**
         * Return the applications' information.
         *
         * @return the applications' information
         */
        get() {
            val list = ArrayList<AppInfo>()
            val pm: PackageManager = Utils.app.packageManager ?: return list
            val installedPackages: List<PackageInfo?> = pm.getInstalledPackages(0)
            for (pi in installedPackages) {
                val ai: AppInfo = getBean(pm, pi) ?: continue
                list.add(ai)
            }
            return list
        }

    /**
     * Return the application's package information.
     *
     * @return the application's package information
     */
    fun getApkInfo(apkFile: File?): AppInfo? {
        if (apkFile == null || !apkFile.isFile() || !apkFile.exists()) return null
        return getApkInfo(apkFile.absolutePath)
    }

    /**
     * Return the application's package information.
     *
     * @return the application's package information
     */
    fun getApkInfo(apkFilePath: String?): AppInfo? {
        if (apkFilePath.isNullOrBlank()) return null
        val pm: PackageManager = Utils.app.packageManager ?: return null
        val pi: PackageInfo = pm.getPackageArchiveInfo(apkFilePath, 0) ?: return null
        val appInfo: ApplicationInfo? = pi.applicationInfo
        appInfo?.sourceDir = apkFilePath
        appInfo?.publicSourceDir = apkFilePath
        return getBean(pm, pi)
    }

    val isFirstTimeInstalled: Boolean
        /**
         * Return whether the application was first installed.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            return runCatching {
                val pi: PackageInfo =
                    Utils.app.packageManager.getPackageInfo(Utils.app.packageName, 0)
                return@runCatching pi.firstInstallTime == pi.lastUpdateTime
            }
                .onFailure { e ->
                    e.printStackTrace()
                }
                .getOrDefault(true)
        }

    private fun getBean(pm: PackageManager?, pi: PackageInfo?): AppInfo? {
        if (pm == null) return null
        if (pi == null) return null
        val versionName: String? = pi.versionName
        val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode else pi.versionCode.toLong()
        val packageName: String = pi.packageName
        val ai: ApplicationInfo = pi.applicationInfo ?: return AppInfo(packageName, "", null, "", versionName, versionCode, -1, -1, false)
        val name: String = ai.loadLabel(pm).toString()
        val icon: Drawable? = ai.loadIcon(pm)
        val packagePath: String? = ai.sourceDir
        var minSdkVersion = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            minSdkVersion = ai.minSdkVersion
        }
        val targetSdkVersion: Int = ai.targetSdkVersion
        val isSystem = (ApplicationInfo.FLAG_SYSTEM and ai.flags) != 0
        return AppInfo(
            packageName,
            name,
            icon,
            packagePath,
            versionName,
            versionCode,
            minSdkVersion,
            targetSdkVersion,
            isSystem,
        )
    }
}
