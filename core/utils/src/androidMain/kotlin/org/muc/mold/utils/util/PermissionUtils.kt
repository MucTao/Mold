@file:Suppress("unused")

package org.muc.mold.utils.util

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import org.muc.mold.utils.constant.PermissionConstants
import java.util.Collections

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2017/12/29
 * desc  : utils about permission
 * </pre> *
 */
object PermissionUtils {

    val permissions: List<String?>
        /**
         * @return the permissions used in application
         */
        get() = getPermissions(Utils.app.packageName)

    /**
     *
     * @param packageName The name of the package.
     * @return the permissions used in application
     */
    fun getPermissions(packageName: String): List<String> {
        val pm: PackageManager = Utils.app.packageManager
        runCatching {
            val permissions = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions ?: return emptyList()
            return permissions.toList()
        }.getOrElse { e ->
            e.printStackTrace()
            return Collections.emptyList()
        }
    }

    /**
     * Return whether *you* have been granted the permissions.
     *
     * @param permissions The permissions.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isGranted(permissions: Array<String>): Boolean {
        val requestAndDeniedPermissions = getRequestAndDeniedPermissions(permissions)
        val deniedPermissions = requestAndDeniedPermissions.second
        if (!deniedPermissions.isEmpty()) {
            return false
        }
        val requestPermissions = requestAndDeniedPermissions.first
        for (permission in requestPermissions) {
            if (!isGranted(permission)) {
                return false
            }
        }
        return true
    }

    private fun getRequestAndDeniedPermissions(permissionsParam: Array<String>): Pair<List<String>, List<String>> {
        val requestPermissions = ArrayList<String>()
        val deniedPermissions = ArrayList<String>()
        val appPermissions = permissions
        for (param in permissionsParam) {
            var isIncludeInManifest = false
            val permissions: Array<String> = PermissionConstants.getPermissions(param)
            for (permission in permissions) {
                if (appPermissions.contains(permission)) {
                    requestPermissions.add(permission)
                    isIncludeInManifest = true
                }
            }
            if (!isIncludeInManifest) {
                deniedPermissions.add(param)
                Log.e("PermissionUtils", "U should add the permission of $param in manifest.")
            }
        }
        return Pair(requestPermissions, deniedPermissions)
    }

    fun isGranted(permission: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(Utils.app, permission))
    }


    @get:RequiresApi(api = Build.VERSION_CODES.M)
    val isGrantedDrawOverlays: Boolean
        /**
         * Return whether the app can draw on top of other apps.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = Settings.canDrawOverlays(Utils.app)


    /** Launch the application's details settings. */
    fun launchAppDetailsSettings() {
        val intent: Intent = IntentUtils.getLaunchAppDetailsSettingsIntent(Utils.app.packageName, true)
        if (!IntentUtils.isIntentAvailable(intent)) return
        Utils.app.startActivity(intent)
    }

}
