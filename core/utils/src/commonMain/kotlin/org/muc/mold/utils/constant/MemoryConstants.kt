@file:Suppress("unused")
package org.muc.mold.utils.constant

import androidx.annotation.IntDef

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2017/03/13
 * desc  : constants of memory
</pre> * 
 */
object MemoryConstants {
    const val BYTE: Int = 1
    const val KB: Int = 1024
    const val MB: Int = 1048576
    const val GB: Int = 1073741824

    @IntDef(BYTE, KB, MB, GB)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Unit
}
