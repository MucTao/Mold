@file:Suppress("unused")

package org.muc.mold.utils.util

import android.os.Build

internal class FileHead(private val name: String?) {
    private val first = linkedMapOf<String, String>()
    private val last = linkedMapOf<String, String>()

    fun addFirst(key: String?, value: String?) = appendTo(first, key, value)

    fun append(extra: Map<String?, String?>?) {
        extra?.forEach { (key, value) -> append(key, value) }
    }

    fun append(key: String?, value: String?) = appendTo(last, key, value)

    private fun appendTo(host: MutableMap<String, String>, key: String?, value: String?) {
        if (key.isNullOrEmpty() || value.isNullOrEmpty()) return
        host[key.padEnd(19)] = value
    }

    val appended: String
        get() = buildString {
            last.forEach { (key, value) -> append(key).append(": ").append(value).append('\n') }
        }

    override fun toString(): String = buildString {
        val border = "************* $name Head ****************\n"
        append(border)
        first.forEach { (key, value) -> append(key).append(": ").append(value).append('\n') }
        append("Rom Info           : ").append(RomUtils.getRomInfo()).append('\n')
        append("Device Manufacturer: ").append(Build.MANUFACTURER).append('\n')
        append("Device Model       : ").append(Build.MODEL).append('\n')
        append("Android Version    : ").append(Build.VERSION.RELEASE).append('\n')
        append("Android SDK        : ").append(Build.VERSION.SDK_INT).append('\n')
        append("App VersionName    : ").append(AppUtils.appVersionName).append('\n')
        append("App VersionCode    : ").append(AppUtils.appVersionCode).append('\n')
        append(appended)
        append(border).append('\n')
    }
}
