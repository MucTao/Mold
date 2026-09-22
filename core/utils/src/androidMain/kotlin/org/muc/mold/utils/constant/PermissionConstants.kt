@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.constant

import android.Manifest.permission
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.StringDef

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2017/12/29
 * desc  : constants of permission
 * </pre> *
 */
@SuppressLint("InlinedApi")
object PermissionConstants {
    const val CALENDAR: String = "CALENDAR"
    const val CAMERA: String = "CAMERA"
    const val CONTACTS: String = "CONTACTS"
    const val LOCATION: String = "LOCATION"
    const val MICROPHONE: String = "MICROPHONE"
    const val PHONE: String = "PHONE"
    const val SENSORS: String = "SENSORS"
    const val SMS: String = "SMS"
    const val STORAGE: String = "STORAGE"
    const val ACTIVITY_RECOGNITION: String = "ACTIVITY_RECOGNITION"

    private val GROUP_CALENDAR =
        arrayOf(
            permission.READ_CALENDAR,
            permission.WRITE_CALENDAR,
        )
    private val GROUP_CAMERA = arrayOf(permission.CAMERA)
    private val GROUP_CONTACTS =
        arrayOf(
            permission.READ_CONTACTS,
            permission.WRITE_CONTACTS,
            permission.GET_ACCOUNTS,
        )
    private val GROUP_LOCATION =
        arrayOf(
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_BACKGROUND_LOCATION,
        )
    private val GROUP_MICROPHONE = arrayOf(permission.RECORD_AUDIO)
    private val GROUP_PHONE =
        arrayOf(
            permission.READ_PHONE_STATE,
            permission.READ_PHONE_NUMBERS,
            permission.CALL_PHONE,
            permission.READ_CALL_LOG,
            permission.WRITE_CALL_LOG,
            permission.ADD_VOICEMAIL,
            permission.USE_SIP,
            permission.PROCESS_OUTGOING_CALLS,
            permission.ANSWER_PHONE_CALLS,
        )
    private val GROUP_PHONE_BELOW_O =
        arrayOf(
            permission.READ_PHONE_STATE,
            permission.READ_PHONE_NUMBERS,
            permission.CALL_PHONE,
            permission.READ_CALL_LOG,
            permission.WRITE_CALL_LOG,
            permission.ADD_VOICEMAIL,
            permission.USE_SIP,
            permission.PROCESS_OUTGOING_CALLS,
        )
    private val GROUP_SENSORS = arrayOf(permission.BODY_SENSORS)
    private val GROUP_SMS =
        arrayOf(
            permission.SEND_SMS,
            permission.RECEIVE_SMS,
            permission.READ_SMS,
            permission.RECEIVE_WAP_PUSH,
            permission.RECEIVE_MMS,
        )
    private val GROUP_STORAGE =
        arrayOf(
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
        )
    private val GROUP_ACTIVITY_RECOGNITION = arrayOf(permission.ACTIVITY_RECOGNITION)

    fun getPermissions(@PermissionGroup permission: String?): Array<String> {
        if (permission == null) return emptyArray()
        return when (permission) {
            CALENDAR -> GROUP_CALENDAR
            CAMERA -> GROUP_CAMERA
            CONTACTS -> GROUP_CONTACTS
            LOCATION -> GROUP_LOCATION
            MICROPHONE -> GROUP_MICROPHONE
            PHONE ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    GROUP_PHONE_BELOW_O
                } else {
                    GROUP_PHONE
                }

            SENSORS -> GROUP_SENSORS
            SMS -> GROUP_SMS
            STORAGE -> GROUP_STORAGE
            ACTIVITY_RECOGNITION -> GROUP_ACTIVITY_RECOGNITION
            else -> arrayOf(permission)
        }
    }

    @StringDef(CALENDAR, CAMERA, CONTACTS, LOCATION, MICROPHONE, PHONE, SENSORS, SMS, STORAGE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class PermissionGroup
}
