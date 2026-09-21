@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2018/05/15
 * desc  : utils about metadata
 * </pre> *
 */
object MetaDataUtils {
    /**
     * Return the value of metadata in application.
     *
     * @param key The key of metadata.
     * @return the value of metadata in application
     */
    fun getMetaDataInApp(key: String): Result<String> {
        val pm: PackageManager = Utils.app.packageManager
        val packageName: String = Utils.app.packageName
        return runCatching {
            val ai: ApplicationInfo =
                pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            ai.metaData.get(key).toString()
        }
    }

    /**
     * Return the value of metadata in activity.
     *
     * @param activity The activity.
     * @param key The key of metadata.
     * @return the value of metadata in activity
     */
    fun getMetaDataInActivity(
        activity: Activity,
        key: String,
    ) = getMetaDataInActivity(activity.javaClass, key)


    /**
     * Return the value of metadata in activity.
     *
     * @param clz The activity class.
     * @param key The key of metadata.
     * @return the value of metadata in activity
     */
    fun getMetaDataInActivity(
        clz: Class<out Activity>,
        key: String,
    ): Result<String> {
        val pm: PackageManager = Utils.app.packageManager
        val componentName = ComponentName(Utils.app, clz)
        return runCatching {
            val ai: ActivityInfo = pm.getActivityInfo(componentName, PackageManager.GET_META_DATA)
            ai.metaData.get(key).toString()
        }
    }

    /**
     * Return the value of metadata in service.
     *
     * @param service The service.
     * @param key The key of metadata.
     * @return the value of metadata in service
     */
    fun getMetaDataInService(service: Service, key: String) = getMetaDataInService(service.javaClass, key)

    /**
     * Return the value of metadata in service.
     *
     * @param clz The service class.
     * @param key The key of metadata.
     * @return the value of metadata in service
     */
    fun getMetaDataInService(
        clz: Class<out Service?>,
        key: String,
    ): Result<String> {
        val pm: PackageManager = Utils.app.packageManager
        val componentName = ComponentName(Utils.app, clz)
        return runCatching {
            val info: ServiceInfo = pm.getServiceInfo(componentName, PackageManager.GET_META_DATA)
            info.metaData.get(key).toString()
        }
    }

    /**
     * Return the value of metadata in receiver.
     *
     * @param receiver The receiver.
     * @param key The key of metadata.
     * @return the value of metadata in receiver
     */
    fun getMetaDataInReceiver(
        receiver: BroadcastReceiver,
        key: String,
    ) = getMetaDataInReceiver(receiver.javaClass, key)

    /**
     * Return the value of metadata in receiver.
     *
     * @param clz The receiver class.
     * @param key The key of metadata.
     * @return the value of metadata in receiver
     */
    fun getMetaDataInReceiver(
        clz: Class<out BroadcastReceiver?>,
        key: String,
    ): Result<String> {
        val pm: PackageManager = Utils.app.packageManager
        val componentName = ComponentName(Utils.app, clz)
        return runCatching {
            val info: ActivityInfo = pm.getReceiverInfo(componentName, PackageManager.GET_META_DATA)
            info.metaData.get(key).toString()
        }
    }
}
