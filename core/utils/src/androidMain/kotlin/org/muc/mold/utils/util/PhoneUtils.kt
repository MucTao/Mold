@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.Manifest.permission.CALL_PHONE
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/02
 * desc  : utils about phone
 * </pre>
 */
object PhoneUtils {

    // ====================================================================================
    //  设备类型
    // ====================================================================================

    /**
     * Return whether the device is phone.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isPhone: Boolean
        get() = telephonyManager.phoneType != TelephonyManager.PHONE_TYPE_NONE

    // ====================================================================================
    //  硬件标识：deviceId / serial / IMEI / MEID / IMSI
    // ====================================================================================

    /**
     * Return the unique device id.
     *
     * If the version of SDK is greater than 28, it will return an empty string.
     *
     * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
     *
     * @return the unique device id
     */
    @get:RequiresPermission(allOf = [READ_PHONE_STATE, "android.permission.READ_PRIVILEGED_PHONE_STATE"])
    @get:SuppressLint("HardwareIds")
    val deviceId: String
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return ""
            val tm = telephonyManager
            tm.deviceId?.takeIf { it.isNotEmpty() }?.let { return it }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tm.imei?.takeIf { it.isNotEmpty() }?.let { return it }
                return tm.meid.orEmpty()
            }
            return ""
        }


    /**
     * Return the IMEI.
     *
     * If the version of SDK is greater than 28, it will return an empty string.
     *
     * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
     *
     * @return the IMEI
     */
    @get:RequiresPermission(allOf = [READ_PHONE_STATE,"android.permission.READ_PRIVILEGED_PHONE_STATE"])
    val iMEI: String?
        get() = getImeiOrMeid()

    /**
     * Return the MEID.
     *
     * If the version of SDK is greater than 28, it will return an empty string.
     *
     * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
     *
     * @return the MEID
     */
    @get:RequiresPermission(allOf = [READ_PHONE_STATE,"android.permission.READ_PRIVILEGED_PHONE_STATE"])
    val mEID: String?
        get() = getImeiOrMeid(isImei = false)

    /**
     * Return the IMEI or MEID.
     *
     * If the version of SDK is greater than 28, it will return an empty string.
     *
     * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
     *
     * @param isImei True to return the IMEI, false to return the MEID. Default: true
     * @return the IMEI or MEID
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(allOf = [READ_PHONE_STATE,"android.permission.READ_PRIVILEGED_PHONE_STATE"])
    fun getImeiOrMeid(isImei: Boolean = true): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return ""

        val tm = telephonyManager

        // API 26+：双卡各取一次，返回较小的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return if (isImei) getMinOne(tm.getImei(0), tm.getImei(1))
            else getMinOne(tm.getMeid(0), tm.getMeid(1))
        }

        // API 21–25：先读系统属性，失败再反射 getDeviceId(int)
        val prop = getSystemPropertyByReflect(
            if (isImei) "ril.gsm.imei" else "ril.cdma.meid"
        )
        if (prop.isNotEmpty()) {
            val arr = prop.split(",")
            return if (arr.size == 2) getMinOne(arr[0], arr[1]) else arr[0]
        }

        var id0: String? = tm.deviceId
        var id1: String? = ""
        runCatching {
            val method: Method = tm.javaClass.getMethod(
                "getDeviceId",
                Int::class.javaPrimitiveType
            )
            id1 = method.invoke(
                tm,
                if (isImei) TelephonyManager.PHONE_TYPE_GSM
                else TelephonyManager.PHONE_TYPE_CDMA
            ) as? String
        }.onFailure { e ->
            when (e) {
                is NoSuchMethodException,
                is IllegalAccessException,
                is InvocationTargetException -> e.printStackTrace()

                else -> throw e
            }
        }

        // 长度校验
        if (isImei) {
            if (id0 != null && id0.length < 15) id0 = ""
            if (id1 != null && id1.length < 15) id1 = ""
        } else {
            if (id0 != null && id0.length == 14) id0 = ""
            if (id1 != null && id1.length == 14) id1 = ""
        }
        return getMinOne(id0, id1)
    }

    /**
     * Return the IMSI.
     *
     * Must hold `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
     *
     * @return the IMSI
     */
    @get:RequiresPermission(allOf = [READ_PHONE_STATE,"android.permission.READ_PRIVILEGED_PHONE_STATE"])
    @get:SuppressLint("HardwareIds")
    val iMSI: String?
        get() = runCatching { telephonyManager.subscriberId }
            .onFailure { e ->
                if (e !is SecurityException) throw e
                e.printStackTrace()
            }
            .getOrDefault("")

    // ====================================================================================
    //  SIM 卡 / 网络运营商
    // ====================================================================================

    /**
     * Returns the current phone type.
     *
     * @return the current phone type
     *
     *  - [TelephonyManager.PHONE_TYPE_NONE]
     *  - [TelephonyManager.PHONE_TYPE_GSM]
     *  - [TelephonyManager.PHONE_TYPE_CDMA]
     *  - [TelephonyManager.PHONE_TYPE_SIP]
     */
    val phoneType: Int
        get() = telephonyManager.phoneType

    /**
     * Return whether sim card state is ready.
     *
     * @return `true`: yes<br></br>`false`: no
     */
    val isSimCardReady: Boolean
        get() = telephonyManager.simState == TelephonyManager.SIM_STATE_READY

    /**
     * Return the sim operator name.
     *
     * @return the sim operator name
     */
    val simOperatorName: String
        get() = telephonyManager.simOperatorName

    /**
     * Return the sim operator using mnc.
     *
     * @return the sim operator
     */
    val simOperatorByMnc: String?
        get() {
            val operator = telephonyManager.simOperator ?: return ""
            return when (operator) {
                "46000", "46002", "46007", "46020" -> "中国移动"
                "46001", "46006", "46009" -> "中国联通"
                "46003", "46005", "46011" -> "中国电信"
                else -> operator
            }
        }

    // ====================================================================================
    //  拨号 / 短信
    // ====================================================================================

    /**
     * Skip to dial.
     *
     * @param phoneNumber The phone number.
     */
    fun dial(phoneNumber: String) {
        Utils.app.startActivity(IntentUtils.getDialIntent(phoneNumber))
    }

    /**
     * Make a phone call.
     *
     * Must hold `<uses-permission android:name="android.permission.CALL_PHONE" />`
     *
     * @param phoneNumber The phone number.
     */
    @RequiresPermission(CALL_PHONE)
    fun call(phoneNumber: String) {
        Utils.app.startActivity(IntentUtils.getCallIntent(phoneNumber))
    }

    /**
     * Send sms.
     *
     * @param phoneNumber The phone number.
     * @param content The content.
     */
    fun sendSms(phoneNumber: String, content: String?) {
        Utils.app.startActivity(IntentUtils.getSendSmsIntent(phoneNumber, content))
    }

    // ====================================================================================
    //  内部工具
    // ====================================================================================

    /**
     * 从两个字符串里取字典序较小且非空的那个；都为空返回 `""`。
     */
    private fun getMinOne(s0: String?, s1: String?): String {
        val a = s0.orEmpty()
        val b = s1.orEmpty()
        return when {
            a.isEmpty() && b.isEmpty() -> ""
            a.isEmpty() -> b
            b.isEmpty() -> a
            a <= b -> a
            else -> b
        }
    }

    /**
     * 通过反射读 `android.os.SystemProperties.get(key, "")`。
     * 读取失败（类不存在、反射异常等）时静默返回 `""`。
     */
    private fun getSystemPropertyByReflect(key: String): String =
        runCatching {
            @SuppressLint("PrivateApi")
            val clz = Class.forName("android.os.SystemProperties")
            val getMethod: Method = clz.getMethod(
                "get",
                String::class.java,
                String::class.java
            )
            getMethod.invoke(clz, key, "") as? String ?: ""
        }.getOrDefault("")

    /** 缓存的 TelephonyManager 单例访问器。 */
    private val telephonyManager: TelephonyManager
        get() = Utils.app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
}