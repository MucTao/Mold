@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.content.res.Resources
import android.util.DisplayMetrics
import java.lang.reflect.Field

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2018/11/15
 * desc  : utils about adapt screen
 * </pre> *
 */
object AdaptScreenUtils {
    private var sMetricsFields: MutableList<Field?>? = null

    /** Adapt for the horizontal screen, and call it in [android.app.Activity.getResources]. */
    fun adaptWidth(resources: Resources, designWidth: Int): Resources {
        val newXdpi: Float = (resources.displayMetrics.widthPixels * 72f) / designWidth
        applyDisplayMetrics(resources, newXdpi)
        return resources
    }

    /** Adapt for the vertical screen, and call it in [android.app.Activity.getResources]. */
    fun adaptHeight(
        resources: Resources,
        designHeight: Int,
        includeNavBar: Boolean = false,
    ): Resources {
        val screenHeight: Float =
            (resources.displayMetrics.heightPixels +
                    (if (includeNavBar) BarUtils.getStatusBarHeight(resources) else 0)) * 72f
        val newXdpi = screenHeight / designHeight
        applyDisplayMetrics(resources, newXdpi)
        return resources
    }


    /**
     * @param resources The resources.
     * @return the resource
     */
    fun closeAdapt(resources: Resources): Resources {
        val newXdpi: Float = Resources.getSystem().displayMetrics.density * 72f
        applyDisplayMetrics(resources, newXdpi)
        return resources
    }

    /**
     * Value of pt to value of px.
     *
     * @param ptValue The value of pt.
     * @return value of px
     */
    fun pt2Px(ptValue: Float): Int {
        val metrics: DisplayMetrics = Utils.app.resources.displayMetrics
        return (ptValue * metrics.xdpi / 72f + 0.5).toInt()
    }

    /**
     * Value of px to value of pt.
     *
     * @param pxValue The value of px.
     * @return value of pt
     */
    fun px2Pt(pxValue: Float): Int {
        val metrics: DisplayMetrics = Utils.app.resources.displayMetrics
        return (pxValue * 72 / metrics.xdpi + 0.5).toInt()
    }

    private fun applyDisplayMetrics(resources: Resources, newXdpi: Float) {
        resources.displayMetrics.xdpi = newXdpi
        Utils.app.resources.displayMetrics.xdpi = newXdpi
        applyOtherDisplayMetrics(resources, newXdpi)
    }

    val preLoadRunnable: Runnable
        get() = Runnable { preLoad() }

    private fun preLoad() {
        applyDisplayMetrics(Resources.getSystem(), Resources.getSystem().displayMetrics.xdpi)
    }

    private fun applyOtherDisplayMetrics(resources: Resources, newXdpi: Float) {
        if (sMetricsFields == null) {
            sMetricsFields = ArrayList()
            val resCls = resources.javaClass
            var declaredFields: Array<Field>? = resCls.declaredFields
            while (!declaredFields.isNullOrEmpty()) {
                for (field in declaredFields) {
                    if (field.type.isAssignableFrom(DisplayMetrics::class.java)) {
                        field.isAccessible = true
                        val tmpDm: DisplayMetrics? = getMetricsFromField(resources, field)
                        if (tmpDm != null) {
                            sMetricsFields?.add(field)
                            tmpDm.xdpi = newXdpi
                        }
                    }
                }
                val superResCls = resCls.getSuperclass()
                if (superResCls != null) {
                    declaredFields = superResCls.declaredFields
                } else {
                    break
                }
            }
        } else {
            applyMetricsFields(resources, newXdpi)
        }
    }

    private fun applyMetricsFields(resources: Resources?, newXdpi: Float) {
        if (sMetricsFields == null) return
        for (metricsField in sMetricsFields) {
            runCatching {
                val dm: DisplayMetrics? = metricsField?.get(resources) as DisplayMetrics?
                if (dm != null) dm.xdpi = newXdpi
            }
                .getOrElse { e ->
                    if (e !is Exception) throw e
                    e.printStackTrace()
                }
        }
    }

    private fun getMetricsFromField(resources: Resources?, field: Field): DisplayMetrics? =
        runCatching {
            field.get(resources) as DisplayMetrics?
        }
            .getOrNull()
}
