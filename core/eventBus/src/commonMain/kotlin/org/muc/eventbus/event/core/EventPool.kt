@file:Suppress("unused")

package org.muc.eventbus.event.core

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.muc.eventbus.event.AppEvent

class EventPool<T : AppEvent>(private val maxSize: Int = 100) {
    private val pool = mutableListOf<T>()
    private val mutex = Mutex()
    private var hitCount = 0
    private var missCount = 0

    suspend fun acquire(block: () -> T): T {
        mutex.withLock {
            if (pool.isNotEmpty()) {
                hitCount++
                return pool.removeAt(pool.lastIndex)
            }
        }
        missCount++
        return block()
    }

    suspend fun release(event: T) {
        if (event is Resettable) {
            event.reset()
        }
        mutex.withLock {
            if (pool.size < maxSize) {
                pool.add(event)
            }
        }
    }

    suspend fun getStats(): PoolStats {
        mutex.withLock {
            return PoolStats(
                poolSize = pool.size,
                hitCount = hitCount,
                missCount = missCount,
                hitRate = if (hitCount + missCount > 0)
                    hitCount.toDouble() / (hitCount + missCount) else 0.0
            )
        }
    }

    data class PoolStats(
        val poolSize: Int,
        val hitCount: Int,
        val missCount: Int,
        val hitRate: Double
    )
}

interface Resettable {
    fun reset()
}