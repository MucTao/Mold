@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.os.Build
import android.text.Html
import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/07
 * desc  : utils about encode
 * </pre> *
 */
object EncodeUtils {

    /**
     * Return the urlencoded string.
     *
     * @param input The input.
     * @param charsetName The name of charset.
     * @return the urlencoded string
     */
    fun urlEncode(
        input: String?,
        charsetName: String? = "UTF-8",
    ) = runCatching {
        if (input.isNullOrEmpty()) ""
        else URLEncoder.encode(input, charsetName)
    }

    /**
     * Return the string of decode urlencoded string.
     *
     * @param input The input.
     * @param charsetName The name of charset.
     * @return the string of decode urlencoded string
     */
    fun urlDecode(
        input: String?,
        charsetName: String? = "UTF-8",
    ) = runCatching {
        if (input.isNullOrEmpty()) return@runCatching ""
        val safeInput: String =
            input.replace("%(?![0-9a-fA-F]{2})", "%25").replace("\\+", "%2B")
        URLDecoder.decode(safeInput, charsetName)
    }

    /**
     * Return Base64-encode bytes.
     *
     * @param input The input.
     * @return Base64-encode bytes
     */
    fun base64Encode(input: String): ByteArray {
        return base64Encode(input.toByteArray())
    }

    /**
     * Return Base64-encode bytes.
     *
     * @param input The input.
     * @return Base64-encode bytes
     */
    fun base64Encode(input: ByteArray?): ByteArray {
        if (input == null || input.isEmpty()) return ByteArray(0)
        return Base64.encode(input, Base64.NO_WRAP)
    }

    /**
     * Return Base64-encode string.
     *
     * @param input The input.
     * @return Base64-encode string
     */
    fun base64Encode2String(input: ByteArray?): String {
        if (input == null || input.isEmpty()) return ""
        return Base64.encodeToString(input, Base64.NO_WRAP)
    }

    /**
     * Return the bytes of decode Base64-encode string.
     *
     * @param input The input.
     * @return the string of decode Base64-encode string
     */
    fun base64Decode(input: String?): ByteArray {
        if (input.isNullOrEmpty()) return ByteArray(0)
        return Base64.decode(input, Base64.NO_WRAP)
    }

    /**
     * Return the bytes of decode Base64-encode bytes.
     *
     * @param input The input.
     * @return the bytes of decode Base64-encode bytes
     */
    fun base64Decode(input: ByteArray?): ByteArray {
        if (input == null || input.isEmpty()) return ByteArray(0)
        return Base64.decode(input, Base64.NO_WRAP)
    }

    /**
     * Return html-encode string.
     *
     * @param input The input.
     * @return html-encode string
     */
    fun htmlEncode(input: CharSequence?): String {
        if (input.isNullOrEmpty()) return ""
        val sb = StringBuilder()
        input.forEach { c ->
            when (c) {
                '<' -> sb.append("&lt;") // $NON-NLS-1$
                '>' -> sb.append("&gt;") // $NON-NLS-1$
                '&' -> sb.append("&amp;") // $NON-NLS-1$
                '\'' -> // http://www.w3.org/TR/xhtml1
                    // The named character reference &apps; (the apostrophe, U+0027) was
                    // introduced in XML 1.0 but does not appear in HTML. Authors should
                    // therefore use &#39; instead of &apos; to work as expected in HTML 4
                    // user agents.
                    sb.append("&#39;") // $NON-NLS-1$
                '"' -> sb.append("&quot;") // $NON-NLS-1$
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * Return the string of decode html-encode string.
     *
     * @param input The input.
     * @return the string of decode html-encode string
     */
    fun htmlDecode(input: String?): CharSequence {
        if (input.isNullOrEmpty()) return ""
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(input)
        }
    }

    /**
     * Return the binary encoded string padded with one space
     *
     * @param input The input.
     * @return binary string
     */
    fun binaryEncode(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        return input.map { Integer.toBinaryString(it.code) }.joinToString(" ")
    }

    /**
     * Return UTF-8 String from binary
     *
     * @param input binary string
     * @return UTF-8 String
     */
    fun binaryDecode(input: String?): String {
        if (input.isNullOrEmpty()) return ""
        val splits = input.split(" ")
        val sb = StringBuilder()
        for (split in splits) {
            sb.append((Integer.parseInt(split, 2).toChar()))
        }
        return sb.toString()
    }
}
