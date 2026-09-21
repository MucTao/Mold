@file:Suppress("unused", "DEPRECATION")

package org.muc.mold.utils.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import java.util.Locale

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2019/06/20
 * desc  : utils about language
 * </pre> *
 */
object LanguageUtils {
    private const val KEY_LOCALE = "KEY_LOCALE"
    private const val VALUE_FOLLOW_SYSTEM = "VALUE_FOLLOW_SYSTEM"

    /** Apply the system language. */
    @JvmOverloads
    fun applySystemLanguage(isRelaunchApp: Boolean = false) {
        applyLanguageReal(null, isRelaunchApp)
    }

    /**
     * Apply the language.
     *
     * @param locale The language of locale.
     * @param isRelaunchApp True to relaunch app, false to recreate all activities.
     */
    fun applyLanguage(
        locale: Locale,
        isRelaunchApp: Boolean = false,
    ) {
        applyLanguageReal(locale, isRelaunchApp)
    }

    private fun applyLanguageReal(
        locale: Locale?,
        isRelaunchApp: Boolean,
    ) {
        Utils.sp.edit(locale == null) {
            putString(KEY_LOCALE, if (locale == null) VALUE_FOLLOW_SYSTEM else locale2String(locale))
        }
        val destLocal: Locale = locale ?: getLocal(Resources.getSystem().configuration)
        updateAppContextLanguage(
            destLocal,
        ) { success ->
            if (success) {
                restart(isRelaunchApp)
            } else {
                // use relaunch app
                AppUtils.relaunchApp()
            }
        }
    }

    private fun restart(isRelaunchApp: Boolean) {
        if (isRelaunchApp) {
            AppUtils.relaunchApp()
        } else {
            for (activity in UtilsActivityLifecycleImpl.activityList) {
                activity.recreate()
            }
        }
    }

    val isAppliedLanguage: Boolean
        /**
         * Return whether applied the language by [LanguageUtils].
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = getAppliedLanguage() != null

    /**
     * Return whether applied the language by [LanguageUtils].
     *
     * @param locale The locale.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isAppliedLanguage(locale: Locale): Boolean {
        val appliedLocale: Locale = getAppliedLanguage() ?: return false
        return isSameLocale(locale, appliedLocale)
    }

    /**
     * Return the applied locale.
     *
     * @return the applied locale
     */
    fun getAppliedLanguage(): Locale? {
        val spLocaleStr: String? = Utils.sp.getString(KEY_LOCALE, null)
        if (spLocaleStr.isNullOrBlank() || VALUE_FOLLOW_SYSTEM == spLocaleStr) {
            return null
        }
        return string2Locale(spLocaleStr)
    }

    /**
     * Return the locale of context.
     *
     * @return the locale of context
     */
    fun getContextLanguage(context: Context): Locale {
        return getLocal(context.resources.configuration)
    }

    val appContextLanguage: Locale
        /**
         * Return the locale of applicationContext.
         *
         * @return the locale of applicationContext
         */
        get() = getContextLanguage(Utils.app)

    val systemLanguage: Locale
        /**
         * Return the locale of system
         *
         * @return the locale of system
         */
        get() = getLocal(Resources.getSystem().configuration)

    /**
     * Update the locale of applicationContext.
     *
     * @param destLocale The dest locale.
     * @param consumer The consumer.
     */
    fun updateAppContextLanguage(destLocale: Locale, consumer: (Boolean) -> Unit) {
        pollCheckAppContextLocal(destLocale, 0, consumer)
    }

    fun pollCheckAppContextLocal(
        destLocale: Locale,
        index: Int,
        consumer: ((Boolean) -> Unit)?
    ) {
        val appResources: Resources = Utils.app.resources
        val appConfig: Configuration = appResources.configuration
        val appLocal: Locale = getLocal(appConfig)
        appConfig.setLocale(destLocale)
        Utils.app.resources.updateConfiguration(appConfig, appResources.displayMetrics)
        if (consumer == null) return
        if (isSameLocale(appLocal, destLocale)) {
            consumer.invoke(true)
        } else {
            if (index < 20) {
                ThreadUtils.runOnUiThreadDelayed(
                    { pollCheckAppContextLocal(destLocale, index + 1, consumer) },
                    16,
                )
                return
            }
            Log.e("LanguageUtils", "appLocal didn't update.")
            consumer.invoke(false)
        }
    }

    /**
     * If applyLanguage not work, try to call it in [Activity.attachBaseContext].
     *
     * @param context The baseContext.
     * @return the context with language
     */
    fun attachBaseContext(context: Context): Context {
        val spLocaleStr: String? = Utils.sp.getString(KEY_LOCALE, null)
        if (spLocaleStr.isNullOrBlank() || VALUE_FOLLOW_SYSTEM == spLocaleStr) {
            return context
        }
        val settingsLocale: Locale = string2Locale(spLocaleStr) ?: return context
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(settingsLocale)
        return context.createConfigurationContext(config)
    }

    fun applyLanguage(activity: Activity) {
        val spLocale: String? = Utils.sp.getString(KEY_LOCALE, null)
        if (spLocale.isNullOrBlank()) {
            return
        }
        val destLocal = if (VALUE_FOLLOW_SYSTEM == spLocale) {
            getLocal(Resources.getSystem().configuration)
        } else {
            string2Locale(spLocale)
        }

        if (destLocal == null) return

        updateConfiguration(activity, destLocal)
        updateConfiguration(Utils.app, destLocal)
    }

    private fun updateConfiguration(context: Context, destLocal: Locale?) {
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(destLocal)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun locale2String(locale: Locale): String {
        val localLanguage: String? = locale.language // this may be empty
        val localCountry: String? = locale.country // this may be empty
        return "$localLanguage$$localCountry"
    }

    private fun string2Locale(str: String): Locale? {
        val locale: Locale? = string2LocaleReal(str)
        if (locale == null) {
            Log.e("LanguageUtils", "The string of $str is not in the correct format.")
            Utils.sp.edit {
                remove(KEY_LOCALE)
            }
        }
        return locale
    }

    private fun string2LocaleReal(str: String): Locale? {
        if (!isRightFormatLocalStr(str)) {
            return null
        }

        return runCatching {
            val splitIndex = str.indexOf("$")
            return@runCatching Locale(
                str.substring(0, splitIndex),
                str.substring(splitIndex + 1),
            )
        }
            .onFailure { ignore ->
                if (ignore !is Exception) throw ignore
            }
            .getOrDefault(null)
    }

    private fun isRightFormatLocalStr(localStr: String): Boolean {
        val chars = localStr.toCharArray()
        var count = 0
        for (c in chars) {
            if (c == '$') {
                if (count >= 1) {
                    return false
                }
                ++count
            }
        }
        return count == 1
    }

    private fun isSameLocale(l0: Locale, l1: Locale): Boolean {
        return (l1.language == l0.language) && (l1.country == l0.country)
    }

    private fun getLocal(configuration: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.getLocales().get(0)
        } else {
            configuration.locale
        }
    }

}
