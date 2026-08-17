@file:Suppress("unused")

package org.muc.network.curlplugin.history

import kotlinx.coroutines.flow.StateFlow

/**
 * 线程安全的命令历史存储。
 *
 * 历史记录仅存在于进程的 RAM 中，不会在应用重启之间持久化。
 */
interface HistoryStore {

    /** 当前命令历史记录，按添加顺序排列（旧在前，新在后）。 */
    val entries: StateFlow<List<HistoryEntry>>

    /** 添加一条记录到历史。 */
    fun append(entry: HistoryEntry)
    fun update(id: Long, transform: (HistoryEntry) -> HistoryEntry)

    /** 清除历史记录. */
    fun clear()
}
