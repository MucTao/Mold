@file:Suppress("unused")

package org.muc.network.curlplugin.history

/**
 * HTTP 网络请求的历史记录条目。
 *
 * 仅存储请求元数据和响应摘要，完整响应体故意不保存以避免内存膨胀。
 *
 * @param id 唯一标识符
 * @param timestampEpochMs 请求发起时间戳（毫秒）
 * @param method HTTP 请求方法（GET/POST 等）
 * @param url 请求 URL
 * @param curl 等效的 cURL 命令字符串
 * @param responseBodyString 响应体摘要
 * @param durationMs 请求耗时（毫秒）
 * @param httpCode HTTP 响应状态码
 * @param shortError 简短错误信息，成功时为 null
 */
data class HttpHistoryEntry(
    override val id: Long,
    val timestampEpochMs: Long,
    val method: String,
    val url: String,
    val curl: String,
    val responseBodyString: String,
    val durationMs: Long,
    val httpCode: Int?,
    val shortError: String?,
) : HistoryEntry {
    /** HTTP 请求成功完成（状态码 2xx 且无错误）。 */
    override val isSuccess: Boolean
        get() = httpCode != null && httpCode in 200..299 && shortError.isNullOrBlank()
}
