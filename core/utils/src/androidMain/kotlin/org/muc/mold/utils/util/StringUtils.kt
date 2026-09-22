@file:Suppress("unused")

package org.muc.mold.utils.util

import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import org.muc.mold.utils.util.ConvertUtils.toBitmap
import java.util.Locale

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/16
 * desc  : utils about string
 * </pre> *
 */
object StringUtils {

    /**
     * Hex string to bytes.
     *
     * e.g. hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
     *
     * @param hexString The hex string.
     * @return the bytes
     */
    fun hexString2Bytes(hexString: String?): ByteArray {
        var hexString = hexString
        if (hexString.isNullOrBlank()) return ByteArray(0)
        var len = hexString.length
        if (len % 2 != 0) {
            hexString = "0$hexString"
            len += 1
        }
        val hexBytes = hexString.uppercase(Locale.getDefault()).toCharArray()
        val ret = ByteArray(len shr 1)
        var i = 0
        while (i < len) {
            ret[i shr 1] = (hex2Dec(hexBytes[i]) shl 4 or hex2Dec(hexBytes[i + 1])).toByte()
            i += 2
        }
        return ret
    }

    private fun hex2Dec(hexChar: Char): Int {
        return when (hexChar) {
            in '0'..'9' -> {
                hexChar.code - '0'.code
            }

            in 'A'..'F' -> {
                hexChar.code - 'A'.code + 10
            }

            else -> {
                throw IllegalArgumentException()
            }
        }
    }

    /**
     * Base64 → Bitmap。
     *
     * @param flags Base64 标志，默认 [Base64.DEFAULT]
     */
    fun base642Bitmap(
        base64: String,
        flags: Int = Base64.DEFAULT
    ): Result<Bitmap> = runCatching { Base64.decode(base64, flags).toBitmap().getOrThrow() }

    /**
     * Base64 → ByteArray。
     */
    fun base642Bytes(
        base64: String,
        flags: Int = Base64.DEFAULT
    ): Result<ByteArray> = runCatching { Base64.decode(base64, flags) }

    /**
     * 字符串 → Html Spanned（API 24+）。
     */
    fun string2Html(
        text: String
    ): Result<android.text.Spanned> = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(text)
        }
    }

    /**
     * Convert string to DBC.
     *
     * @param s The string.
     * @return the DBC string
     */
    fun toDBC(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        val chars = s.toCharArray()
        var i = 0
        val len = chars.size
        while (i < len) {
            when (chars[i].code) {
                12288 -> {
                    chars[i] = ' '
                }

                in 65281..65374 -> {
                    chars[i] = (chars[i].code - 65248).toChar()
                }

                else -> {
                    chars[i] = chars[i]
                }
            }
            i++
        }
        return String(chars)
    }

    /**
     * Convert string to SBC.
     *
     * @param s The string.
     * @return the SBC string
     */
    fun toSBC(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        val chars = s.toCharArray()
        var i = 0
        val len = chars.size
        while (i < len) {
            when {
                chars[i] == ' ' -> {
                    chars[i] = 12288.toChar()
                }

                chars[i].code in 33..126 -> {
                    chars[i] = (chars[i].code + 65248).toChar()
                }

                else -> {
                    chars[i] = chars[i]
                }
            }
            i++
        }
        return String(chars)
    }

    /**
     * Return the string value associated with a particular resource ID.
     *
     * @param id The desired resource identifier.
     * @return the string value associated with a particular resource ID.
     */
    fun getString(@StringRes id: Int): String = getString(id, null)

    /**
     * Return the string value associated with a particular resource ID.
     *
     * @param id The desired resource identifier.
     * @param formatArgs The format arguments that will be used for substitution.
     * @return the string value associated with a particular resource ID.
     */
    fun getString(@StringRes id: Int, vararg formatArgs: Any?): String = runCatching {
        format(Utils.app.getString(id), formatArgs)
    }.getOrNull() ?: id.toString()

    /**
     * Return the string array associated with a particular resource ID.
     *
     * @param id The desired resource identifier.
     * @return The string array associated with the resource.
     */
    fun getStringArray(@ArrayRes id: Int): Array<String> = runCatching {
        return Utils.app.resources.getStringArray(id)
    }
        .getOrElse { arrayOf(id.toString()) }

    /**
     * Format the string.
     *
     * @param str The string.
     * @param args The args.
     * @return a formatted string.
     */
    fun format(str: String?, vararg args: Any?): String? {
        var text = str
        if (text != null) {
            if (args.isNotEmpty()) {
                runCatching {
                    text = String.format(str, args)
                }
            }
        }
        return text
    }
}
