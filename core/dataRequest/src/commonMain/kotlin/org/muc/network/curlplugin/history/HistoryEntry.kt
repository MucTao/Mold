package org.muc.network.curlplugin.history

/**
 * 历史记录条目。
 * 仅存储请求元数据和响应摘要，完整响应体故意不保存以避免内存膨胀。
 */
interface HistoryEntry{
    val id: Long
    val isSuccess: Boolean
}
