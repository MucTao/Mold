@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.Manifest.permission.INTERNET
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Base64
import androidx.annotation.RequiresPermission
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.util.Enumeration
import java.util.UUID
import kotlin.concurrent.Volatile


/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/8/1
 * desc  : utils about device
 * </pre> *
 */
object DeviceUtils {
    val isDeviceRooted: Boolean
        /**
         * Return whether device is rooted.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            val su = "su"
            val locations =
                arrayOf<String?>(
                    "/system/bin/",
                    "/system/xbin/",
                    "/sbin/",
                    "/system/sd/xbin/",
                    "/system/bin/failsafe/",
                    "/data/local/xbin/",
                    "/data/local/bin/",
                    "/data/local/",
                    "/system/sbin/",
                    "/usr/bin/",
                    "/vendor/bin/",
                )
            for (location in locations) {
                if (File(location + su).exists()) {
                    return true
                }
            }
            return false
        }

    val isAdbEnabled: Boolean
        /**
         * Return whether ADB is enabled.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = Settings.Secure.getInt(Utils.app.contentResolver, Settings.Global.ADB_ENABLED, 0) > 0

    val sDKVersionName: String
        /**
         * @return the version name of device's system
         */
        get() = Build.VERSION.RELEASE

    val sDKVersionCode: Int
        /**
         * @return version code of device's system
         */
        get() = Build.VERSION.SDK_INT

    @get:SuppressLint("HardwareIds")
    val androidID: String
        /**
         * @return the android id of device
         */
        get() {
            val id: String? = Settings.Secure.getString(Utils.app.contentResolver, Settings.Secure.ANDROID_ID)
            if ("9774d56d682e549c" == id) return ""
            return id ?: ""
        }

    /**
     * Return the serial of device.
     *
     * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />` (SDK ≥ 29)
     *
     * @return the serial of device
     */
    @get:RequiresPermission(allOf = [READ_PHONE_STATE, "android.permission.READ_PRIVILEGED_PHONE_STATE"])
    @get:SuppressLint("HardwareIds")
    val serial: Result<String>
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val id: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Build.getSerial() else Build.SERIAL
                if ("unknown" == id) Result.success("")
                return Result.success(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Build.getSerial() else Build.SERIAL)
            }
            return runCatching { Build.getSerial() }
        }

    val snProvider: () -> String = { serial.getOrDefault("") }

    @get:RequiresPermission(allOf = [ACCESS_WIFI_STATE, CHANGE_WIFI_STATE])
    val macAddress: String
        /**
         * Return the MAC address.
         *
         * Must hold `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />`,
         * `<uses-permission android:name="android.permission.INTERNET" />`, `<uses-permission
         * android:name="android.permission.CHANGE_WIFI_STATE" />`
         *
         * @return the MAC address
         */
        get() {
            val macAddress = getMacAddress(null)
            if (macAddress.isNotEmpty() || wifiEnabled) return macAddress
            wifiEnabled = true
            wifiEnabled = false
            return getMacAddress(null)
        }

    @set:RequiresPermission(CHANGE_WIFI_STATE)
    private var wifiEnabled: Boolean
        get() {
            @SuppressLint("WifiManagerLeak")
            val manager: WifiManager = Utils.app.getSystemService(WIFI_SERVICE) as? WifiManager? ?: return false
            return manager.isWifiEnabled
        }
        /**
         * Enable or disable Wi-Fi.
         * Must hold `<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />`
         */
        set(enabled) {
            @SuppressLint("WifiManagerLeak")
            val manager: WifiManager = Utils.app.getSystemService(WIFI_SERVICE) as? WifiManager? ?: return
            if (enabled == manager.isWifiEnabled) return
            manager.setWifiEnabled(enabled)
        }

    /**
     * Return the MAC address.
     *
     * Must hold `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />`,
     * `<uses-permission android:name="android.permission.INTERNET" />`
     *
     * @return the MAC address
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE])
    fun getMacAddress(vararg excepts: String?): String {
        var macAddress = macAddressByNetworkInterface
        if (isAddressNotInExcepts(macAddress, *excepts)) {
            return macAddress
        }
        macAddress = macAddressByInetAddress
        if (isAddressNotInExcepts(macAddress, *excepts)) {
            return macAddress
        }
        macAddress = macAddressByWifiInfo
        if (isAddressNotInExcepts(macAddress, *excepts)) {
            return macAddress
        }
        macAddress = macAddressByFile
        if (isAddressNotInExcepts(macAddress, *excepts)) {
            return macAddress
        }
        return ""
    }

    private fun isAddressNotInExcepts(address: String?, vararg excepts: String?): Boolean {
        if (address.isNullOrEmpty()) {
            return false
        }
        if ("02:00:00:00:00:00" == address) {
            return false
        }
        if (excepts.isEmpty()) {
            return true
        }
        for (filter in excepts) {
            if (filter != null && filter == address) {
                return false
            }
        }
        return true
    }

    @get:RequiresPermission(ACCESS_WIFI_STATE)
    private val macAddressByWifiInfo: String
        get() {
            runCatching {
                val wifi: WifiManager? =
                    Utils.app.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager?
                if (wifi != null) {
                    val info: WifiInfo? = wifi.connectionInfo
                    if (info != null) {
                        @SuppressLint("HardwareIds") val macAddress: String = info.macAddress
                        if (macAddress.isNotEmpty()) {
                            return macAddress
                        }
                    }
                }
            }
                .getOrElse { e ->
                    if (e !is Exception) throw e
                    e.printStackTrace()
                }
            return "02:00:00:00:00:00"
        }

    private val macAddressByNetworkInterface: String
        get() {
            runCatching {
                val nis: Enumeration<NetworkInterface?> = NetworkInterface.getNetworkInterfaces()
                while (nis.hasMoreElements()) {
                    val ni: NetworkInterface? = nis.nextElement()
                    if (ni == null || !ni.name.equals("wlan0", true)) continue
                    val macBytes: ByteArray? = ni.getHardwareAddress()
                    if (macBytes != null && macBytes.isNotEmpty()) {
                        val sb = StringBuilder()
                        for (b in macBytes) {
                            sb.append(String.format("%02x:", b))
                        }
                        return sb.substring(0, sb.length - 1)
                    }
                }
            }
                .getOrElse { e ->
                    if (e !is Exception) throw e
                    e.printStackTrace()
                }
            return "02:00:00:00:00:00"
        }

    private val macAddressByInetAddress: String
        get() {
            runCatching {
                val inetAddress: InetAddress? = inetAddress
                if (inetAddress != null) {
                    val ni: NetworkInterface? = NetworkInterface.getByInetAddress(inetAddress)
                    if (ni != null) {
                        val macBytes: ByteArray? = ni.getHardwareAddress()
                        if (macBytes != null && macBytes.isNotEmpty()) {
                            val sb = StringBuilder()
                            for (b in macBytes) {
                                sb.append(String.format("%02x:", b))
                            }
                            return sb.substring(0, sb.length - 1)
                        }
                    }
                }
            }
                .getOrElse { e ->
                    if (e !is Exception) throw e
                    e.printStackTrace()
                }
            return "02:00:00:00:00:00"
        }

    private val inetAddress: InetAddress?
        get() {
            runCatching {
                val nis = NetworkInterface.getNetworkInterfaces()
                while (nis.hasMoreElements()) {
                    val ni: NetworkInterface = nis.nextElement()
                    // To prevent phone of Xiaomi return "10.0.2.15"
                    if (!ni.isUp()) continue
                    val addresses = ni.getInetAddresses()
                    while (addresses.hasMoreElements()) {
                        val inetAddress = addresses.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            val hostAddress = inetAddress.hostAddress ?: continue
                            if (hostAddress.indexOf(':') < 0) return inetAddress
                        }
                    }
                }
            }.getOrElse { e ->
                e.printStackTrace()
            }
            return null
        }

    private val macAddressByFile: String
        get() = runBlocking {
            ShellUtils.exec("getprop wifi.interface", false).mapCatching { result ->
                ShellUtils.exec("cat /sys/class/net/${result.stdout}/address", false)
                    .mapCatching { it.stdout }.getOrDefault("02:00:00:00:00:00")
            }.getOrDefault("02:00:00:00:00:00")
        }


    val manufacturer: String
        /**
         * e.g. Xiaomi
         * @return the manufacturer of the product/hardware
         */
        get() = Build.MANUFACTURER

    val model: String
        /**
         * e.g. MI2SC
         *
         * @return the model of device
         */
        get() {
            return Build.MODEL?.trim()?.replace("\\s*", "") ?: ""
        }

    val aBIs: Array<String>
        /**
         * Return an ordered list of ABIs supported by this device. The most preferred ABI is the
         * first element in the list.
         *
         * @return an ordered list of ABIs supported by this device
         */
        get() {
            return Build.SUPPORTED_ABIS
        }

    val isTablet: Boolean
        /**
         * Return whether device is tablet.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() =
            ((Resources.getSystem().configuration.screenLayout and
                    Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE)

    val isEmulator: Boolean
        /**
         * Return whether device is emulator.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            val checkProperty =
                Build.FINGERPRINT.startsWith("generic") ||
                        Build.FINGERPRINT.lowercase().contains("vbox") ||
                        Build.FINGERPRINT.lowercase().contains("test-keys") ||
                        Build.MODEL.contains("google_sdk") ||
                        Build.MODEL.contains("Emulator") ||
                        Build.MODEL.contains("Android SDK built for x86") ||
                        Build.MANUFACTURER.contains("Genymotion") ||
                        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                        "google_sdk" == Build.PRODUCT
            if (checkProperty) return true

            var operatorName = ""
            val tm: TelephonyManager? =
                Utils.app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
            if (tm != null) {
                val name: String? = tm.networkOperatorName
                if (name != null) {
                    operatorName = name
                }
            }
            val checkOperatorName = operatorName.lowercase() == "android"
            if (checkOperatorName) return true

            val url = "tel:" + "123456"
            val intent = Intent()
            intent.setData(url.toUri())
            intent.setAction(Intent.ACTION_DIAL)
            val checkDial = intent.resolveActivity(Utils.app.packageManager) == null
            if (checkDial) return true
            if (isEmulatorByCpu) return true
            //        boolean checkDebuggerConnected = Debug.isDebuggerConnected();
            //        if (checkDebuggerConnected) return true;
            return false
        }

    private val isEmulatorByCpu: Boolean
        /**
         * Returns whether is emulator by check cpu info. by function of [.readCpuInfo], obtain the
         * device cpu information. then compare whether it is intel or amd (because intel and amd
         * are generally not mobile phone cpu), to determine whether it is a real mobile phone
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            val cpuInfo = readCpuInfo()
            return cpuInfo.contains("intel") || cpuInfo.contains("amd")
        }

    /**
     * Return Cpu information
     *
     * @return Cpu info
     */
    private fun readCpuInfo(): String = runCatching {
        val args = arrayOf("/system/bin/cat", "/proc/cpuinfo")
        val cmd = ProcessBuilder(*args)
        val process: Process = cmd.start()
        val sb = StringBuilder()
        var readLine: String?
        BufferedReader(InputStreamReader(process.inputStream, "utf-8")).use { responseReader ->
            while ((responseReader.readLine().also { readLine = it }) != null) {
                sb.append(readLine)
            }
        }
        sb.toString().lowercase()
    }.getOrDefault("")

    val isDevelopmentSettingsEnabled: Boolean
        /**
         * Whether user has enabled development settings.
         *
         * @return whether user has enabled development settings.
         */
        get() =
            Settings.Global.getInt(
                Utils.app.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0,
            ) > 0


    @Volatile
    private lateinit var udid: String

    val uniqueDeviceId: String
        /**
         * Return the unique device id.
         * <pre>{1}{UUID(macAddress)}</pre>
         * <pre>{2}{UUID(androidId )}</pre>
         * <pre>{3}{UUID(serial )}</pre>
         * <pre>{9}{UUID(random    )}</pre>
         *
         * @return the unique device id
         */
        get() = getUniqueDeviceId("", false)


    /**
     * Return the unique device id.
     * <pre>android 10 deprecated {prefix}{1}{UUID(macAddress)}</pre>
     * <pre>{prefix}{2}{UUID(androidId )}</pre>
     * <pre>{prefix}{3}{UUID(serial )}</pre>
     * <pre>{prefix}{9}{UUID(random    )}</pre>
     *
     * @param prefix The prefix of the unique device id.
     * @param useCache True to use cache, false otherwise.
     * @return the unique device id
     */
    fun getUniqueDeviceId(
        prefix: String = "",
        useCache: Boolean = true,
    ): String {
        if (!useCache) {
            return getUniqueDeviceIdReal(prefix)
        }
        if (::udid.isInitialized.not()) {
            synchronized(DeviceUtils::class.java) {
                if (::udid.isInitialized.not()) {
                    val id: String? = Utils.sp.getString(KEY_UDID, null)
                    if (id != null) {
                        udid = id
                        return udid
                    }
                    return getUniqueDeviceIdReal(prefix)
                }
            }
        }
        return udid
    }

    private fun getUniqueDeviceIdReal(prefix: String?): String = runCatching {
        val serial = snProvider()
        if (serial.isNotEmpty()) {
            saveUdid(prefix + 3, serial)
        } else {
            val androidId = androidID
            if (androidId.isNotEmpty()) {
                saveUdid(prefix + 2, androidId)
            } else {
                val mac = macAddress
                if (mac.isNotEmpty() && mac != "02:00:00:00:00:00") {
                    saveUdid(prefix + 1, mac)
                } else
                    saveUdid(prefix + 9, "")
            }
        }
    }.getOrDefault(saveUdid(prefix + 9, ""))

    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, INTERNET, CHANGE_WIFI_STATE])
    fun isSameDevice(uniqueDeviceId: String): Boolean {
        // {prefix}{type}{22位Base64}
        if (uniqueDeviceId.length < 23) return false
        if (uniqueDeviceId == udid) return true
        val cachedId: String? = Utils.sp.getString(KEY_UDID, null)
        if (uniqueDeviceId == cachedId) return true
        val st: Int = uniqueDeviceId.length - 23
        val type = uniqueDeviceId.substring(st, st + 1)
        if (type.startsWith("1")) {
            val macAddress = macAddress
            return if (macAddress.isEmpty()) false else uniqueDeviceId.substring(st + 1) == getUdid("", macAddress)
        } else if (type.startsWith("2")) {
            val androidId = androidID
            return if (androidId.isEmpty()) false else uniqueDeviceId.substring(st + 1) == getUdid("", androidId)
        } else if (type.startsWith("3")) {
            val serial = snProvider()
            return if (serial.isEmpty()) false else uniqueDeviceId.substring(st + 1) == getUdid("", serial)
        }
        return false
    }

    private fun saveUdid(prefix: String, id: String): String {
        udid = getUdid(prefix, id)
        Utils.sp.edit {
            putString(KEY_UDID, udid)
        }
        return udid
    }

    private fun getUdid(prefix: String, id: String): String {
        val uuid = if (id.isEmpty()) {
            UUID.randomUUID()
        } else {
            UUID.nameUUIDFromBytes(id.toByteArray())
        }
        return prefix + uuid.toBase64Url()
    }

    /**
     * 把 UUID 的 128 bit 编码成 22 位 URL 安全 Base64 字符串（无填充）。
     * URL_SAFE 使用 '-' 和 '_'，避免存储/传输时的转义问题。
     */
    private fun UUID.toBase64Url(): String {
        val bytes = ByteBuffer.allocate(16)
            .putLong(mostSignificantBits)
            .putLong(leastSignificantBits)
            .array()
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }


    private const val KEY_UDID = "Udid"
}
