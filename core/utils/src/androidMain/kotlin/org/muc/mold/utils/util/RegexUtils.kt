@file:Suppress("unused")

package org.muc.mold.utils.util

import androidx.collection.SimpleArrayMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.muc.mold.utils.constant.RegexConstants
import java.util.regex.Pattern

/**
 * <pre>
 * author: Muc
 * blog  : http://Muc.com
 * time  : 2016/08/02
 * desc  : utils about regex
 * </pre>
 *
 * 所有匹配方法均为**同步**实现；对可能耗时的操作（复杂正则、长输入、身份证精确校验）
 * 提供 `xxxAsync` 挂起版本，跑在 [Dispatchers.Default] 上。
 */
object RegexUtils {

    // ====================================================================================
    //  预编译 Pattern：避免每次调用 Pattern.matches 时重复编译
    // ====================================================================================

    private val P_MOBILE_SIMPLE = Pattern.compile(RegexConstants.REGEX_MOBILE_SIMPLE)
    private val P_MOBILE_EXACT = Pattern.compile(RegexConstants.REGEX_MOBILE_EXACT)
    private val P_TEL = Pattern.compile(RegexConstants.REGEX_TEL)
    private val P_ID_CARD15 = Pattern.compile(RegexConstants.REGEX_ID_CARD15)
    private val P_ID_CARD18 = Pattern.compile(RegexConstants.REGEX_ID_CARD18)
    private val P_EMAIL = Pattern.compile(RegexConstants.REGEX_EMAIL)
    private val P_URL = Pattern.compile(RegexConstants.REGEX_URL)
    private val P_ZH = Pattern.compile(RegexConstants.REGEX_ZH)
    private val P_USERNAME = Pattern.compile(RegexConstants.REGEX_USERNAME)
    private val P_DATE = Pattern.compile(RegexConstants.REGEX_DATE)
    private val P_IP = Pattern.compile(RegexConstants.REGEX_IP)

    // ====================================================================================
    //  身份证精确校验：权重、校验位、省级行政区代码
    // ====================================================================================

    private val ID_WEIGHT = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
    private val ID_SUFFIX = charArrayOf('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2')

    /** 省级行政区代码 → 名称。首访时构建一次。 */
    private val CITY_MAP: SimpleArrayMap<String, String> by lazy {
        SimpleArrayMap<String, String>(34).apply {
            put("11", "北京"); put("12", "天津"); put("13", "河北")
            put("14", "山西"); put("15", "内蒙古")
            put("21", "辽宁"); put("22", "吉林"); put("23", "黑龙江")
            put("31", "上海"); put("32", "江苏"); put("33", "浙江")
            put("34", "安徽"); put("35", "福建"); put("36", "江西"); put("37", "山东")
            put("41", "河南"); put("42", "湖北"); put("43", "湖南")
            put("44", "广东"); put("45", "广西"); put("46", "海南")
            put("50", "重庆"); put("51", "四川"); put("52", "贵州")
            put("53", "云南"); put("54", "西藏")
            put("61", "陕西"); put("62", "甘肃"); put("63", "青海")
            put("64", "宁夏"); put("65", "新疆")
            put("71", "台湾老"); put("81", "香港"); put("82", "澳门")
            put("83", "台湾新"); put("91", "国外")
        }
    }

    // ====================================================================================
    //  手机 / 电话
    // ====================================================================================

    /**
     * Return whether input matches regex of simple mobile.
     *
     * @param input The input.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isMobileSimple(input: CharSequence?): Boolean = isMatch(P_MOBILE_SIMPLE, input)

    /**
     * Return whether input matches regex of exact mobile.
     *
     * 先走正则；若不匹配且提供了 [newSegments]，再校验 11 位纯数字 + 前缀命中。
     *
     * @param input The input.
     * @param newSegments The new segments of mobile number. Optional.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isMobileExact(input: CharSequence?, newSegments: List<String>? = null): Boolean {
        if (isMatch(P_MOBILE_EXACT, input)) return true
        if (newSegments.isNullOrEmpty()) return false
        if (input == null || input.length != 11) return false
        val content = input.toString()
        if (content.any { !it.isDigit() }) return false
        return newSegments.any { content.startsWith(it) }
    }

    /**
     * Return whether input matches regex of telephone number.
     *
     * @param input The input.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isTel(input: CharSequence?): Boolean = isMatch(P_TEL, input)

    // ====================================================================================
    //  身份证
    // ====================================================================================

    /**
     * Return whether input matches regex of id card number which length is 15.
     *
     * @param input The input.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isIDCard15(input: CharSequence?): Boolean = isMatch(P_ID_CARD15, input)

    /**
     * Return whether input matches regex of id card number which length is 18.
     *
     * @param input The input.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isIDCard18(input: CharSequence?): Boolean = isMatch(P_ID_CARD18, input)

    /**
     * Return whether input matches regex of exact id card number which length is 18.
     *
     * 在 [isIDCard18] 基础上进一步校验：省级行政区代码 ∈ [CITY_MAP] 且校验位正确。
     *
     * @param input The input.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isIDCard18Exact(input: CharSequence?): Boolean {
        if (!isMatch(P_ID_CARD18, input)) return false
        val s = input!!.toString()
        if (CITY_MAP[s.substring(0, 2)] == null) return false
        var sum = 0
        for (i in 0..16) sum += (s[i] - '0') * ID_WEIGHT[i]
        return s[17] == ID_SUFFIX[sum % 11]
    }

    // ====================================================================================
    //  邮箱 / URL / 中文 / 用户名 / 日期 / IP
    // ====================================================================================

    /**
     * Returns the domain part of a given Email address.
     *
     * E.g. Returns "protonmail.com" from "johnsmith@protonmail.com".
     * 若不含 `@` 返回 `""`。
     */
    fun extractEmailProvider(email: String): String {
        val idx = email.lastIndexOf('@')
        return if (idx < 0) "" else email.substring(idx + 1)
    }

    /**
     * Returns the username part of a given Email address.
     *
     * E.g. Returns "john smith" from "johnsmith@protonmail.com".
     * 若不含 `@` 返回整个字符串。
     */
    fun extractEmailUsername(email: String): String {
        val idx = email.lastIndexOf('@')
        return if (idx < 0) email else email.substring(0, idx)
    }

    /**
     * Return whether a given Email address is on a specified Email provider.
     *
     * E.g. "johnsmith@protonmail.com" 与 "gmail.com" → `false`。
     *
     * @param email The Email address.
     * @param emailProvider The Email provider to testify against.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFromEmailProvider(email: String, emailProvider: String?): Boolean =
        extractEmailProvider(email).equals(emailProvider, ignoreCase = true)

    /**
     * Return whether a given Email address is on any of the specified Email providers.
     *
     * @param email The Email address.
     * @param emailProviders The list of Email providers to testify against.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFromAnyOfEmailProviders(email: String, emailProviders: Array<String?>?): Boolean {
        if (emailProviders.isNullOrEmpty()) return false
        val provider = extractEmailProvider(email)
        return emailProviders.any { it != null && it.equals(provider, ignoreCase = true) }
    }

    /**
     * Return whether input matches regex of email.
     */
    fun isEmail(input: CharSequence?): Boolean = isMatch(P_EMAIL, input)

    /**
     * Return whether input matches regex of url.
     */
    fun isURL(input: CharSequence?): Boolean = isMatch(P_URL, input)

    /**
     * Return whether input matches regex of Chinese character.
     */
    fun isZh(input: CharSequence?): Boolean = isMatch(P_ZH, input)

    /**
     * Return whether input matches regex of username.
     *
     * 允许：`a-z`、`A-Z`、`0-9`、`_`、中文字符；
     * 不能以 `_` 结尾；长度 6–20。
     */
    fun isUsername(input: CharSequence?): Boolean = isMatch(P_USERNAME, input)

    /**
     * Return whether input matches regex of date which pattern is "yyyy-MM-dd".
     */
    fun isDate(input: CharSequence?): Boolean = isMatch(P_DATE, input)

    /**
     * Return whether input matches regex of ip address.
     */
    fun isIP(input: CharSequence?): Boolean = isMatch(P_IP, input)

    // ====================================================================================
    //  通用匹配 / 提取 / 替换
    // ====================================================================================

    /**
     * Return whether input matches the regex.
     *
     * @param regex The regex.
     * @param input The input.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isMatch(regex: String, input: CharSequence?): Boolean =
        !input.isNullOrEmpty() && Pattern.matches(regex, input)

    /**
     * Return the list of input matches the regex.
     *
     * @param regex The regex.
     * @param input The input.
     * @return the list of input matches；输入为空时返回空列表
     */
    fun getMatches(regex: String, input: CharSequence?): List<String> {
        if (input.isNullOrEmpty()) return emptyList()
        val matches = ArrayList<String>()
        val matcher = Pattern.compile(regex).matcher(input)
        while (matcher.find()) matches.add(matcher.group())
        return matches
    }

    /**
     * Splits input around matches of the regex.
     *
     * @param input The input.
     * @param regex The regex.
     * @return the list of strings computed by splitting input around matches of regex；
     *         输入为空时返回空列表
     */
    fun getSplits(input: String?, regex: String): List<String> {
        if (input.isNullOrEmpty()) return emptyList()
        return input.split(regex)
    }

    /**
     * Replace the first subsequence of the input sequence that matches the regex with the given
     * replacement string.
     *
     * @param input The input.
     * @param regex The regex.
     * @param replacement The replacement string.
     * @return the constructed string；输入为空时返回 `""`
     */
    fun getReplaceFirst(input: String?, regex: String, replacement: String?): String {
        if (input.isNullOrEmpty()) return ""
        return Pattern.compile(regex).matcher(input).replaceFirst(replacement.orEmpty())
    }

    /**
     * Replace every subsequence of the input sequence that matches the pattern with the given
     * replacement string.
     *
     * @param input The input.
     * @param regex The regex.
     * @param replacement The replacement string.
     * @return the constructed string；输入为空时返回 `""`
     */
    fun getReplaceAll(input: String?, regex: String, replacement: String?): String {
        if (input.isNullOrEmpty()) return ""
        return Pattern.compile(regex).matcher(input).replaceAll(replacement.orEmpty())
    }

    // ====================================================================================
    //  协程版
    //
    //  正则匹配在长输入 / 复杂正则下可能达到毫秒甚至更高量级，UI 线程调用会掉帧。
    //  对关键耗时入口提供 suspend 版本，跑在 Dispatchers.Default（CPU 密集）上。
    // ====================================================================================

    /** 协程版 [isMatch]。 */
    suspend fun isMatchAsync(regex: String, input: CharSequence?): Boolean =
        withContext(Dispatchers.Default) { isMatch(regex, input) }

    /** 协程版 [getMatches]。 */
    suspend fun getMatchesAsync(regex: String, input: CharSequence?): List<String> =
        withContext(Dispatchers.Default) { getMatches(regex, input) }

    /** 协程版 [getReplaceFirst]。 */
    suspend fun getReplaceFirstAsync(
        input: String?,
        regex: String,
        replacement: String?,
    ): String = withContext(Dispatchers.Default) { getReplaceFirst(input, regex, replacement) }

    /** 协程版 [getReplaceAll]。 */
    suspend fun getReplaceAllAsync(
        input: String?,
        regex: String,
        replacement: String?,
    ): String = withContext(Dispatchers.Default) { getReplaceAll(input, regex, replacement) }

    /** 协程版 [isIDCard18Exact]（含省级代码查表 + 校验位计算）。 */
    suspend fun isIDCard18ExactAsync(input: CharSequence?): Boolean =
        withContext(Dispatchers.Default) { isIDCard18Exact(input) }

    // ====================================================================================
    //  内部实现
    // ====================================================================================

    /** 基于预编译 Pattern 的匹配；空输入快速返回。 */
    private fun isMatch(pattern: Pattern, input: CharSequence?): Boolean =
        !input.isNullOrEmpty() && pattern.matcher(input).matches()
}