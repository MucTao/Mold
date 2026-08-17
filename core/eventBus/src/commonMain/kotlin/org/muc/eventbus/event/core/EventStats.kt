@file:Suppress("unused")

package org.muc.eventbus.event.core

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock


class EventStats {
    private val totalEvents = atomic(0L)
    private val droppedEvents = atomic(0L)
    private val processingTime = atomic(0L)
    private val eventCounts = mutableMapOf<String, AtomicLong>()
    private val mutex = Mutex()
    private val startTime = Clock.System.now().toEpochMilliseconds()

    suspend fun recordEvent(event: String, success: Boolean, duration: Long) {
        totalEvents.incrementAndGet()
        if (!success) {
            droppedEvents.incrementAndGet()
        }
        mutex.withLock {
            eventCounts.getOrPut(event) { atomic(0L) }.incrementAndGet()
        }
        processingTime.addAndGet(duration)
    }

    suspend fun getStats(): Stats {
        val total = totalEvents.value
        val counts: Map<String, Long> = mutex.withLock {
            eventCounts.mapValues { it.value.value }
        }
        return Stats(
            totalEvents = total,
            droppedEvents = droppedEvents.value,
            processingTime = processingTime.value,
            averageTime = if (total > 0) processingTime.value / total else 0,
            eventCounts = counts,
            uptime =  Clock.System.now().toEpochMilliseconds() - startTime
        )
    }

    data class Stats(
        val totalEvents: Long,
        val droppedEvents: Long,
        val processingTime: Long,
        val averageTime: Long,
        val eventCounts: Map<String, Long>,
        val uptime: Long
    )
}