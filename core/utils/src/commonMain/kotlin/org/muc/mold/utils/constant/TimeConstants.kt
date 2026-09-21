@file:Suppress("unused")

package org.muc.mold.utils.constant

import androidx.annotation.IntDef

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2017/03/13
 * desc  : constants of time
</pre> * 
 */
object TimeConstants {
    const val MSEC: Int = 1
    const val SEC: Int = 1000
    const val MIN: Int = 60000
    const val HOUR: Int = 3600000
    const val DAY: Int = 86400000

    @IntDef(MSEC, SEC, MIN, HOUR, DAY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Unit
}
