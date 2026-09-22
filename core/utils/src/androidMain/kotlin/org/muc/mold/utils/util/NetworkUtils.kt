@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.CHANGE_WIFI_STATE
import android.Manifest.permission.INTERNET
import android.Manifest.permission.MODIFY_PHONE_STATE
import android.Manifest.permission.READ_BASIC_PHONE_STATE
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.text.format.Formatter
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.muc.mold.utils.util.NetworkUtils.getDomainAddress
import org.muc.mold.utils.util.NetworkUtils.getIPAddress
import org.muc.mold.utils.util.NetworkUtils.isAvailable
import org.muc.mold.utils.util.NetworkUtils.isAvailableByDns
import org.muc.mold.utils.util.NetworkUtils.isAvailableByPing
import org.muc.mold.utils.util.NetworkUtils.isWifiAvailable
import java.lang.reflect.Method
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.util.Enumeration
import java.util.LinkedList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/02
 * desc  : utils about network (coroutines edition)
 * </pre>
 */
object NetworkUtils {

    enum class NetworkType {
        NETWORK_ETHERNET,
        NETWORK_WIFI,
        NETWORK_5G,
        NETWORK_4G,
        NETWORK_3G,
        NETWORK_2G,
        NETWORK_UNKNOWN,
        NETWORK_NO,
    }

    // ====================================================================================
    //  网络状态监听（Flow 替代 BroadcastReceiver + Listener）
    // ====================================================================================

    private const val DEBOUNCE_MILLIS = 1_000L


    /**
     * 监听网络状态变化（
     * - 首次订阅立即发射当前网络类型
     * - 之后收到 `CONNECTIVITY_ACTION` 广播后 1s 防抖，类型变化才发射
     * - 协程取消时自动 unregisterReceiver，不再需要手动反注册
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    val networkTypeFlow: Flow<NetworkType> = callbackFlow {
        @SuppressLint("MissingPermission")
        fun getNetworkTypeSafe(): NetworkType =
            runCatching { networkType }.getOrDefault(NetworkType.NETWORK_NO)

        var lastType: NetworkType? = null
        var debounceJob: Job? = null
        val receiver = object : BroadcastReceiver() {
            @RequiresPermission(ACCESS_NETWORK_STATE)
            override fun onReceive(context: Context?, intent: Intent) {
                if (intent.action != ConnectivityManager.CONNECTIVITY_ACTION) return
                debounceJob?.cancel()
                debounceJob = launch {
                    delay(DEBOUNCE_MILLIS.milliseconds)
                    val newType = networkType
                    if (newType == lastType) return@launch
                    lastType = newType
                    trySend(newType)
                }
            }
        }
        val initialType = getNetworkTypeSafe()
        lastType = initialType
        trySend(initialType)
        Utils.app.registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        awaitClose {
            debounceJob?.cancel()
            runCatching { Utils.app.unregisterReceiver(receiver) }
        }
    }.distinctUntilChanged()

    // ====================================================================================
    //  无线设置
    // ====================================================================================

    /** Open the settings of wireless. */
    fun openWirelessSettings() {
        Utils.app.startActivity(
            Intent(Settings.ACTION_WIRELESS_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    // ====================================================================================
    //  连通性判断
    // ====================================================================================

    @get:RequiresPermission(ACCESS_NETWORK_STATE)
    val isConnected: Boolean
        get() {
            val info = activeNetworkInfo ?: return false
            return info.isConnected
        }

    @get:RequiresPermission(INTERNET)
    val isAvailable: Boolean get() = isAvailableByDns || isAvailableByPing

    /** 协程版 [isAvailable]，在 IO 调度器执行。 */
    @RequiresPermission(INTERNET)
    suspend fun isAvailableAsync(): Boolean = withContext(Dispatchers.IO) { isAvailable }

    // ------------------------ Ping ------------------------

    @get:RequiresPermission(INTERNET)
    val isAvailableByPing: Boolean
        get() = isAvailableByPing()

    /**
     * Return whether network is available using ping.
     *
     * Must hold `<uses-permission android:name="android.permission.INTERNET" />`
     *
     * @param ip The ip address. Default: 223.5.5.5
     */
    @RequiresPermission(INTERNET)
    fun isAvailableByPing(ip: String? = null): Boolean {
        val realIp = if (TextUtils.isEmpty(ip)) "223.5.5.5" else ip
        return runBlocking { ShellUtils.exec("ping -c 1 $realIp", false).isSuccess }
    }

    /** 协程版 [isAvailableByPing]。 */
    @RequiresPermission(INTERNET)
    suspend fun isAvailableByPingAsync(ip: String? = null): Boolean =
        withContext(Dispatchers.IO) { isAvailableByPing(ip) }

    // ------------------------ DNS ------------------------

    @get:RequiresPermission(INTERNET)
    val isAvailableByDns: Boolean
        get() = isAvailableByDns()

    /**
     * Return whether network is available using domain.
     *
     * Must hold `<uses-permission android:name="android.permission.INTERNET" />`
     *
     * @param domain The name of domain. Default: www.baidu.com
     */
    @RequiresPermission(INTERNET)
    fun isAvailableByDns(domain: String? = null): Boolean {
        val realDomain = if (TextUtils.isEmpty(domain)) "www.baidu.com" else domain
        return runCatching {
            InetAddress.getByName(realDomain) != null
        }.onFailure { e ->
            if (e !is UnknownHostException) throw e
            e.printStackTrace()
        }.getOrDefault(false)
    }

    /** 协程版 [isAvailableByDns]。 */
    @RequiresPermission(INTERNET)
    suspend fun isAvailableByDnsAsync(domain: String? = null): Boolean =
        withContext(Dispatchers.IO) { isAvailableByDns(domain) }

    // ====================================================================================
    //  移动数据 / VPN / 代理
    // ====================================================================================

    val mobileDataEnabled: Boolean
        @RequiresPermission(anyOf = [ACCESS_NETWORK_STATE, MODIFY_PHONE_STATE, READ_PHONE_STATE, READ_BASIC_PHONE_STATE])
        get() = runCatching {
            val tm = Utils.app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return tm.isDataEnabled
            }
            @SuppressLint("PrivateApi")
            val method: Method? = tm.javaClass.getDeclaredMethod("getDataEnabled")
            method?.invoke(tm) as? Boolean ?: false
        }.getOrElse { e ->
            if (e !is Exception) throw e
            e.printStackTrace()
            false
        }

    val isBehindProxy: Boolean
        get() = !(System.getProperty("http.proxyHost") == null ||
                System.getProperty("http.proxyPort") == null)

    val isUsingVPN: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() {
            val cm = Utils.app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cm.getNetworkInfo(ConnectivityManager.TYPE_VPN)?.isConnectedOrConnecting == true
            } else {
                cm.getNetworkInfo(NetworkCapabilities.TRANSPORT_VPN)?.isConnectedOrConnecting == true
            }
        }

    @get:RequiresPermission(ACCESS_NETWORK_STATE)
    val isMobileData: Boolean
        get() {
            val info = activeNetworkInfo ?: return false
            return info.isAvailable && info.type == ConnectivityManager.TYPE_MOBILE
        }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun is4G(): Boolean {
        val info = activeNetworkInfo ?: return false
        return info.isAvailable && info.subtype == TelephonyManager.NETWORK_TYPE_LTE
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun is5G(): Boolean {
        val info = activeNetworkInfo ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            info.isAvailable && info.subtype == TelephonyManager.NETWORK_TYPE_NR
        } else {
            false
        }
    }

    // ====================================================================================
    //  WiFi
    // ====================================================================================

    @get:RequiresPermission(ACCESS_WIFI_STATE)
    @set:RequiresPermission(CHANGE_WIFI_STATE)
    var wifiEnabled: Boolean
        get() {
            @SuppressLint("WifiManagerLeak")
            val manager = Utils.app.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return false
            return manager.isWifiEnabled
        }
        set(enabled) {
            @SuppressLint("WifiManagerLeak")
            val manager = Utils.app.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return
            if (enabled == manager.isWifiEnabled) return
            manager.isWifiEnabled = enabled
        }

    @get:RequiresPermission(ACCESS_NETWORK_STATE)
    val isWifiConnected: Boolean
        get() {
            val cm = Utils.app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val ni = cm.activeNetworkInfo ?: return false
            return ni.type == ConnectivityManager.TYPE_WIFI
        }

    @get:RequiresPermission(allOf = [ACCESS_WIFI_STATE, INTERNET])
    val isWifiAvailable: Boolean
        get() = wifiEnabled && isAvailable

    /** 协程版 [isWifiAvailable]。 */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, INTERNET])
    suspend fun isWifiAvailableAsync(): Boolean = withContext(Dispatchers.IO) { isWifiAvailable }

    // ====================================================================================
    //  运营商 & 网络类型
    // ====================================================================================

    val networkOperatorName: String?
        get() {
            val tm = Utils.app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            return tm?.networkOperatorName
        }

    @get:RequiresPermission(ACCESS_NETWORK_STATE)
    val networkType: NetworkType
        get() {
            if (isEthernet) return NetworkType.NETWORK_ETHERNET

            val info = activeNetworkInfo
            if (info == null || !info.isAvailable) return NetworkType.NETWORK_NO

            if (info.type == ConnectivityManager.TYPE_WIFI) return NetworkType.NETWORK_WIFI
            if (info.type != ConnectivityManager.TYPE_MOBILE) return NetworkType.NETWORK_UNKNOWN

            return when (info.subtype) {
                TelephonyManager.NETWORK_TYPE_GSM,
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN -> NetworkType.NETWORK_2G

                TelephonyManager.NETWORK_TYPE_TD_SCDMA,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.NETWORK_3G

                TelephonyManager.NETWORK_TYPE_IWLAN,
                TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.NETWORK_4G

                TelephonyManager.NETWORK_TYPE_NR -> NetworkType.NETWORK_5G

                else -> {
                    val subtypeName = info.subtypeName
                    if (subtypeName.equals("TD-SCDMA", true)
                        || subtypeName.equals("WCDMA", true)
                        || subtypeName.equals("CDMA2000", true)
                    ) NetworkType.NETWORK_3G
                    else NetworkType.NETWORK_UNKNOWN
                }
            }
        }


    @get:RequiresPermission(ACCESS_NETWORK_STATE)
    private val isEthernet: Boolean
        get() {
            val cm = Utils.app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET) ?: return false
            val state = info.state ?: return false
            return state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING
        }

    @get:RequiresPermission(ACCESS_NETWORK_STATE)
    private val activeNetworkInfo: NetworkInfo?
        get() {
            val cm = Utils.app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return null
            return cm.activeNetworkInfo
        }

    // ====================================================================================
    //  IP / 域名
    // ====================================================================================

    /** 协程版 [getIPAddress]。 */
    @RequiresPermission(INTERNET)
    suspend fun getIPAddressAsync(useIPv4: Boolean): String? =
        withContext(Dispatchers.IO) { getIPAddress(useIPv4) }

    /**
     * Return the ip address.
     *
     * Must hold `<uses-permission android:name="android.permission.INTERNET" />`
     *
     * @param useIPv4 True to use ipv4, false otherwise.
     */
    @RequiresPermission(INTERNET)
    fun getIPAddress(useIPv4: Boolean): String? {
        runCatching {
            val nis: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            val adds = LinkedList<InetAddress>()
            while (nis.hasMoreElements()) {
                val ni = nis.nextElement()
                if (!ni.isUp || ni.isLoopback) continue
                val addresses = ni.inetAddresses
                while (addresses.hasMoreElements()) {
                    adds.addFirst(addresses.nextElement())
                }
            }
            for (add in adds) {
                if (add.isLoopbackAddress) continue
                val hostAddress = add.hostAddress ?: continue
                val isIPv4 = hostAddress.indexOf(':') < 0
                if (useIPv4) {
                    if (isIPv4) return hostAddress
                } else if (!isIPv4) {
                    val index = hostAddress.indexOf('%')
                    return if (index < 0) hostAddress.uppercase()
                    else hostAddress.substring(0, index).uppercase()
                }
            }
        }.onFailure { e ->
            if (e !is SocketException) throw e
            e.printStackTrace()
        }
        return ""
    }

    val broadcastIpAddress: String?
        get() {
            runCatching {
                val nis = NetworkInterface.getNetworkInterfaces()
                while (nis.hasMoreElements()) {
                    val ni = nis.nextElement()
                    if (!ni.isUp || ni.isLoopback) continue
                    for (ia in ni.interfaceAddresses) {
                        ia.broadcast?.let { return it.hostAddress }
                    }
                }
            }.onFailure { e ->
                if (e !is SocketException) throw e
                e.printStackTrace()
            }
            return ""
        }

    /** 协程版 [getDomainAddress]。 */
    @RequiresPermission(INTERNET)
    suspend fun getDomainAddressAsync(domain: String?): String? =
        withContext(Dispatchers.IO) { getDomainAddress(domain) }

    /**
     * Return the domain address.
     *
     * Must hold `<uses-permission android:name="android.permission.INTERNET" />`
     */
    @RequiresPermission(INTERNET)
    fun getDomainAddress(domain: String?): String? {
        return runCatching {
            InetAddress.getByName(domain).hostAddress
        }.onFailure { e ->
            if (e !is UnknownHostException) throw e
            e.printStackTrace()
        }.getOrDefault("")
    }

    // ====================================================================================
    //  WiFi 相关 IP / SSID
    // ====================================================================================

    @get:RequiresPermission(ACCESS_WIFI_STATE)
    val ipAddressByWifi: String?
        get() = wifiDhcpField { Formatter.formatIpAddress(it.ipAddress) }

    @get:RequiresPermission(ACCESS_WIFI_STATE)
    val gatewayByWifi: String?
        get() = wifiDhcpField { Formatter.formatIpAddress(it.gateway) }

    @get:RequiresPermission(ACCESS_WIFI_STATE)
    val netMaskByWifi: String?
        get() = wifiDhcpField { Formatter.formatIpAddress(it.netmask) }

    @get:RequiresPermission(ACCESS_WIFI_STATE)
    val serverAddressByWifi: String?
        get() = wifiDhcpField { Formatter.formatIpAddress(it.serverAddress) }

    private inline fun wifiDhcpField(block: (android.net.DhcpInfo) -> String?): String? {
        @SuppressLint("WifiManagerLeak")
        val wm = Utils.app.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return ""
        return block(wm.dhcpInfo)
    }

    @get:RequiresPermission(ACCESS_WIFI_STATE)
    val sSID: String?
        get() {
            val wm = Utils.app.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return ""
            val wi = wm.connectionInfo ?: return ""
            val ssid = wi.ssid
            if (ssid.isNullOrBlank()) return ""
            return if (ssid.length > 2 && ssid.first() == '"' && ssid.last() == '"') {
                ssid.substring(1, ssid.length - 1)
            } else ssid
        }

    // ====================================================================================
    //  WiFi 扫描结果（Flow 替代 Timer + Consumer）
    // ====================================================================================

    private const val SCAN_PERIOD_MILLIS: Long = 3_000L

    @get:RequiresPermission(allOf = [ACCESS_WIFI_STATE, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    val wifiScanResult: WifiScanResults
        get() {
            val result = WifiScanResults()
            if (!wifiEnabled) return result
            @SuppressLint("WifiManagerLeak")
            val wm = Utils.app.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wm.scanResults?.let { result.setAllResults(it) }
            return result
        }

    /**
     * 周期性 WiFi 扫描结果流
     * - 首次立即发射一次（即使为空）
     * - 之后每 [intervalDuration] 触发一次扫描，结果变化才发射
     * - 收集器取消时自动停止扫描（无需 Timer.cancel）
     */
    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun wifiScanResultsFlow(
        intervalDuration: Duration = SCAN_PERIOD_MILLIS.milliseconds,
    ): Flow<WifiScanResults> = flow {
        var previous: WifiScanResults? = null
        while (currentCoroutineContext().isActive) {
            val wm = Utils.app.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wm == null || !wm.isWifiEnabled) {
                delay(intervalDuration)
                continue
            }
            runCatching { wm.startScan() }
            val current = WifiScanResults().apply {
                setAllResults(wm.scanResults ?: emptyList())
            }
            if (previous == null || !isSameScanResults(previous.allResults, current.allResults)) {
                previous = current
                emit(current)
            }
            delay(intervalDuration)
        }
    }.flowOn(Dispatchers.IO)

    private fun isSameScanResults(
        l1: List<ScanResult?>?,
        l2: List<ScanResult?>?,
    ): Boolean {
        if (l1 == null && l2 == null) return true
        if (l1 == null || l2 == null) return false
        if (l1.size != l2.size) return false
        for (i in l1.indices) {
            if (!isSameScanResultContent(l1[i], l2[i])) return false
        }
        return true
    }

    private fun isSameScanResultContent(r1: ScanResult?, r2: ScanResult?): Boolean =
        r1 != null && r2 != null &&
                r1.BSSID == r2.BSSID &&
                r1.SSID == r2.SSID &&
                r1.capabilities == r2.capabilities &&
                r1.level == r2.level

    // ====================================================================================
    //  WiFi 扫描结果封装
    // ====================================================================================

    class WifiScanResults {
        val allResults = ArrayList<ScanResult>()
        private val filterResults = ArrayList<ScanResult>()

        fun getAllResults(): List<ScanResult> = allResults
        fun getFilterResults(): List<ScanResult> = filterResults

        fun setAllResults(allResults: List<ScanResult>) {
            this.allResults.clear()
            this.allResults.addAll(allResults)
            filterResults.clear()
            filterResults.addAll(filterScanResult(allResults))
        }

        companion object {
            private fun filterScanResult(results: List<ScanResult>): List<ScanResult> {
                if (results.isEmpty()) return emptyList()
                val map = LinkedHashMap<String, ScanResult>(results.size)
                for (result in results) {
                    if (result.SSID.isNullOrBlank()) continue
                    val old = map[result.SSID]
                    if (old != null && old.level >= result.level) continue
                    map[result.SSID] = result
                }
                return map.values.toList()
            }
        }
    }
}