@file:Suppress("unused")

package org.muc.mold.utils.util

import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Properties

/**
 * <pre>
 *     author: Muc
 *     blog  : http://Muc.com
 *     time  : 2018/07/04
 *     desc  : utils about rom
 * </pre>
 */
object RomUtils {

    private val ROM_HUAWEI = arrayOf("huawei")
    private val ROM_VIVO = arrayOf("vivo")
    private val ROM_XIAOMI = arrayOf("xiaomi")
    private val ROM_OPPO = arrayOf("oppo")
    private val ROM_LEECO = arrayOf("leeco", "letv")
    private val ROM_360 = arrayOf("360", "qiku")
    private val ROM_ZTE = arrayOf("zte")
    private val ROM_ONEPLUS = arrayOf("oneplus")
    private val ROM_NUBIA = arrayOf("nubia")
    private val ROM_COOLPAD = arrayOf("coolpad", "yulong")
    private val ROM_LG = arrayOf("lg", "lge")
    private val ROM_GOOGLE = arrayOf("google")
    private val ROM_SAMSUNG = arrayOf("samsung")
    private val ROM_MEIZU = arrayOf("meizu")
    private val ROM_LENOVO = arrayOf("lenovo")
    private val ROM_SMARTISAN = arrayOf("smartisan", "deltainno")
    private val ROM_HTC = arrayOf("htc")
    private val ROM_SONY = arrayOf("sony")
    private val ROM_GIONEE = arrayOf("gionee", "amigo")
    private val ROM_MOTOROLA = arrayOf("motorola")

    private const val VERSION_PROPERTY_HUAWEI = "ro.build.version.emui"
    private const val VERSION_PROPERTY_VIVO = "ro.vivo.os.build.display.id"
    private const val VERSION_PROPERTY_XIAOMI = "ro.build.version.incremental"
    private const val VERSION_PROPERTY_OPPO = "ro.build.version.opporom"
    private const val VERSION_PROPERTY_LEECO = "ro.letv.release.version"
    private const val VERSION_PROPERTY_360 = "ro.build.uiversion"
    private const val VERSION_PROPERTY_ZTE = "ro.build.MiFavor_version"
    private const val VERSION_PROPERTY_ONEPLUS = "ro.rom.version"
    private const val VERSION_PROPERTY_NUBIA = "ro.build.rom.id"
    private const val UNKNOWN = "unknown"

    private val bean: RomInfo by lazy { createRomInfo() }

    // --- ROM 判断方法 ---
    @JvmStatic
    fun isHuawei() = ROM_HUAWEI[0] == bean.name
    @JvmStatic
    fun isVivo() = ROM_VIVO[0] == bean.name
    @JvmStatic
    fun isXiaomi() = ROM_XIAOMI[0] == bean.name
    @JvmStatic
    fun isOppo() = ROM_OPPO[0] == bean.name
    @JvmStatic
    fun isLeeco() = ROM_LEECO[0] == bean.name
    @JvmStatic
    fun is360() = ROM_360[0] == bean.name
    @JvmStatic
    fun isZte() = ROM_ZTE[0] == bean.name
    @JvmStatic
    fun isOneplus() = ROM_ONEPLUS[0] == bean.name
    @JvmStatic
    fun isNubia() = ROM_NUBIA[0] == bean.name
    @JvmStatic
    fun isCoolpad() = ROM_COOLPAD[0] == bean.name
    @JvmStatic
    fun isLg() = ROM_LG[0] == bean.name
    @JvmStatic
    fun isGoogle() = ROM_GOOGLE[0] == bean.name
    @JvmStatic
    fun isSamsung() = ROM_SAMSUNG[0] == bean.name
    @JvmStatic
    fun isMeizu() = ROM_MEIZU[0] == bean.name
    @JvmStatic
    fun isLenovo() = ROM_LENOVO[0] == bean.name
    @JvmStatic
    fun isSmartisan() = ROM_SMARTISAN[0] == bean.name
    @JvmStatic
    fun isHtc() = ROM_HTC[0] == bean.name
    @JvmStatic
    fun isSony() = ROM_SONY[0] == bean.name
    @JvmStatic
    fun isGionee() = ROM_GIONEE[0] == bean.name
    @JvmStatic
    fun isMotorola() = ROM_MOTOROLA[0] == bean.name

    @JvmStatic
    fun getRomInfo(): RomInfo = bean

    private fun createRomInfo(): RomInfo {
        val brand = getBrand()
        val manufacturer = getManufacturer()

        return RomInfo().apply {
            when {
                isRightRom(brand, manufacturer, *ROM_HUAWEI) -> {
                    name = ROM_HUAWEI[0]
                    val v = getRomVersion(VERSION_PROPERTY_HUAWEI)
                    version = v.split("_").let { if (it.size > 1) it[1] else v }
                }

                isRightRom(brand, manufacturer, *ROM_VIVO) -> {
                    name = ROM_VIVO[0]
                    version = getRomVersion(VERSION_PROPERTY_VIVO)
                }

                isRightRom(brand, manufacturer, *ROM_XIAOMI) -> {
                    name = ROM_XIAOMI[0]
                    version = getRomVersion(VERSION_PROPERTY_XIAOMI)
                }

                isRightRom(brand, manufacturer, *ROM_OPPO) -> {
                    name = ROM_OPPO[0]
                    version = getRomVersion(VERSION_PROPERTY_OPPO)
                }

                isRightRom(brand, manufacturer, *ROM_LEECO) -> {
                    name = ROM_LEECO[0]
                    version = getRomVersion(VERSION_PROPERTY_LEECO)
                }

                isRightRom(brand, manufacturer, *ROM_360) -> {
                    name = ROM_360[0]
                    version = getRomVersion(VERSION_PROPERTY_360)
                }

                isRightRom(brand, manufacturer, *ROM_ZTE) -> {
                    name = ROM_ZTE[0]
                    version = getRomVersion(VERSION_PROPERTY_ZTE)
                }

                isRightRom(brand, manufacturer, *ROM_ONEPLUS) -> {
                    name = ROM_ONEPLUS[0]
                    version = getRomVersion(VERSION_PROPERTY_ONEPLUS)
                }

                isRightRom(brand, manufacturer, *ROM_NUBIA) -> {
                    name = ROM_NUBIA[0]
                    version = getRomVersion(VERSION_PROPERTY_NUBIA)
                }

                else -> {
                    name = when {
                        isRightRom(brand, manufacturer, *ROM_COOLPAD) -> ROM_COOLPAD[0]
                        isRightRom(brand, manufacturer, *ROM_LG) -> ROM_LG[0]
                        isRightRom(brand, manufacturer, *ROM_GOOGLE) -> ROM_GOOGLE[0]
                        isRightRom(brand, manufacturer, *ROM_SAMSUNG) -> ROM_SAMSUNG[0]
                        isRightRom(brand, manufacturer, *ROM_MEIZU) -> ROM_MEIZU[0]
                        isRightRom(brand, manufacturer, *ROM_LENOVO) -> ROM_LENOVO[0]
                        isRightRom(brand, manufacturer, *ROM_SMARTISAN) -> ROM_SMARTISAN[0]
                        isRightRom(brand, manufacturer, *ROM_HTC) -> ROM_HTC[0]
                        isRightRom(brand, manufacturer, *ROM_SONY) -> ROM_SONY[0]
                        isRightRom(brand, manufacturer, *ROM_GIONEE) -> ROM_GIONEE[0]
                        isRightRom(brand, manufacturer, *ROM_MOTOROLA) -> ROM_MOTOROLA[0]
                        else -> manufacturer
                    }
                    version = getRomVersion("")
                }
            }
        }
    }

    private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean =
        names.any { brand.contains(it) || manufacturer.contains(it) }

    private fun getManufacturer(): String =
        runCatching {
            Build.MANUFACTURER?.takeIf { !TextUtils.isEmpty(it) }?.lowercase()
        }.getOrNull() ?: UNKNOWN

    private fun getBrand(): String =
        runCatching {
            Build.BRAND?.takeIf { !TextUtils.isEmpty(it) }?.lowercase()
        }.getOrNull() ?: UNKNOWN

    private fun getRomVersion(propertyName: String): String {
        var ret = if (propertyName.isNotEmpty()) getSystemProperty(propertyName) else ""
        if (ret.isEmpty() || ret == UNKNOWN) {
            ret = runCatching {
                Build.DISPLAY?.takeIf { !TextUtils.isEmpty(it) }?.lowercase()
            }.getOrNull() ?: ""
        }
        return ret.ifEmpty { UNKNOWN }
    }

    private fun getSystemProperty(name: String): String {
        getSystemPropertyByShell(name).takeIf { it.isNotEmpty() }?.let { return it }
        getSystemPropertyByStream(name).takeIf { it.isNotEmpty() }?.let { return it }
        return if (Build.VERSION.SDK_INT < 28) getSystemPropertyByReflect(name) else ""
    }

    private fun getSystemPropertyByShell(propName: String): String {
        var input: BufferedReader? = null
        return try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            input.readLine() ?: ""
        } catch (e: IOException) {
            ""
        } finally {
            input?.closeQuietly()
        }
    }

    private fun getSystemPropertyByStream(key: String): String {
        return try {
            val prop = Properties()
            FileInputStream(File(Environment.getRootDirectory(), "build.prop")).use {
                prop.load(it)
            }
            prop.getProperty(key, "")
        } catch (e: Exception) {
            ""
        }
    }

    @SuppressLint("PrivateApi")
    private fun getSystemPropertyByReflect(key: String): String {
        return try {
            val clz = Class.forName("android.os.SystemProperties")
            val getMethod = clz.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(clz, key, "") as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun BufferedReader.closeQuietly() {
        try {
            close()
        } catch (_: IOException) {
        }
    }

    data class RomInfo(
        var name: String = "",
        var version: String = ""
    ) {
        override fun toString(): String = "RomInfo{name=$name, version=$version}"
    }
}