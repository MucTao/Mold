@file:Suppress("unused")

package org.muc.mold.utils.util

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/09/25
 * desc  : utils about clipboard
 * </pre> *
 */
object ClipboardUtils {
    /**
     * Copy the text to clipboard.
     *
     * The label equals name of package.
     *
     * @param text The text.
     */
    fun copyText(text: CharSequence?) {
        val cm: ClipboardManager = Utils.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(Utils.app.packageName, text))
    }

    /**
     * Copy the text to clipboard.
     *
     * @param label The label.
     * @param text The text.
     */
    fun copyText(label: CharSequence?, text: CharSequence?) {
        val cm: ClipboardManager =
            Utils.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /** Clear the clipboard. */
    fun clear() {
        val cm: ClipboardManager =
            Utils.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(null, ""))
    }

    val label: CharSequence
        /**
         * @return the label for clipboard
         */
        get() {
            val cm: ClipboardManager =
                Utils.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val des: ClipDescription = cm.primaryClipDescription ?: return ""
            return des.label ?: ""
        }

    val text: CharSequence
        /**
         * @return the text for clipboard
         */
        get() {
            val cm: ClipboardManager =
                Utils.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData? = cm.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text: CharSequence? = clip.getItemAt(0).coerceToText(Utils.app)
                if (text != null) {
                    return text
                }
            }
            return ""
        }

    /** Add the clipboard changed listener. */
    fun addChangedListener(listener: ClipboardManager.OnPrimaryClipChangedListener?) {
        val cm: ClipboardManager =
            Utils.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.addPrimaryClipChangedListener(listener)
    }

    /** Remove the clipboard changed listener. */
    fun removeChangedListener(listener: ClipboardManager.OnPrimaryClipChangedListener?) {
        val cm: ClipboardManager = Utils.app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.removePrimaryClipChangedListener(listener)
    }
}
