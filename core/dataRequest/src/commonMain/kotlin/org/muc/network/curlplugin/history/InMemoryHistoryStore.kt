@file:Suppress("unused")

package org.muc.network.curlplugin.history

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 带历史记录大小限制的 [HistoryStore] 内存实现。
 *
 * 线程安全通过私有锁对象上的同步来保证。
 * 内部使用 [ArrayDeque] 实现 O(1) 的头部删除操作，避免 [List.drop] 的 O(n) 开销。
 */
class InMemoryHistoryStore(
    private val capacity: Int = DEFAULT_CAPACITY,
) : HistoryStore {

    private val lock = SynchronizedObject()
    private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    override val entries: StateFlow<List<HistoryEntry>> = _entries.asStateFlow()

    override fun append(entry: HistoryEntry) {
        synchronized(lock) {
            _entries.value = buildUpdatedList(_entries.value, entry, capacity)
        }
    }

    override fun update(id: Long, transform: (HistoryEntry) -> HistoryEntry) {
        synchronized(lock) {
            val list: MutableList<HistoryEntry> = _entries.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index != -1) {
                list[index] = transform(list[index])
                _entries.value = list
            }
        }
    }

    override fun clear() {
        synchronized(lock) {
            _entries.value = emptyList()
        }
    }

    /**
     * 将条目追加到容量受限的列表中。
     * 若超出容量限制，移除最旧条目（FIFO 策略）。
     * 使用 [ArrayDeque] 确保 O(1) 头部删除。
     */
    private fun <T> buildUpdatedList(current: List<T>, entry: T, maxCapacity: Int): List<T> {
        if (maxCapacity <= 0) return emptyList()
        if (current.size < maxCapacity) return current + entry
        val deque = ArrayDeque(current)
        deque.removeFirst()
        deque.addLast(entry)
        return deque.toList()
    }

    private companion object {
        const val DEFAULT_CAPACITY: Int = 99
    }
}
