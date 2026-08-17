@file:Suppress("unused")

package org.muc.eventbus.event.core

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.muc.eventbus.event.AppEvent
import org.muc.eventbus.event.Priority
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

val logger = KotlinLogging.logger {}   // 如果使用 Napier 可替换

object EventBus {
    // ==================== 常量 ====================
    private const val BUFFER_SIZE = 1024
    private const val MAX_CACHE_PER_TYPE = 5

    // 优先级调度器（提供默认值，可通过 init() 覆盖）
    val dispatchers by lazy {
        mapOf(
            Priority.CRITICAL to Dispatchers.Main.limitedParallelism(1),
            Priority.HIGH to Dispatchers.IO.limitedParallelism(4),
            Priority.NORMAL to Dispatchers.IO.limitedParallelism(8),
            Priority.LOW to Dispatchers.IO.limitedParallelism(2),
            Priority.BACKGROUND to Dispatchers.IO.limitedParallelism(1)
        )
    }

    // ==================== 内部状态 ====================
    private val eventCounter = atomic(0L)
    private val droppedCounter = atomic(0L)

    // 线程安全的缓存（使用 Mutex + 普通 Map）
    val cacheMutex = Mutex()
    val l1Cache = mutableMapOf<KClass<out AppEvent>, MutableList<AppEvent>>()

    private val circuitBreaker = CircuitBreaker()
    private val eventPool by lazy { EventPool<AppEvent>() }
    private val stats = EventStats()

    private val _events by lazy {
        MutableSharedFlow<AppEvent>(
            replay = 0,
            extraBufferCapacity = BUFFER_SIZE,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()


    // ==================== 初始化（惰性，可外部触发） ====================
    private var isInitialized = false

    /**
     * 启动批处理和可选监控。建议在 Application 或根 Composable 中调用。
     */
    fun initialize(enableMonitoring: Boolean = false) {
        if (isInitialized) return
        isInitialized = true
        circuitBreaker.startAutoRecovery(CoroutineScope(Dispatchers.IO))
        if (enableMonitoring) {
            startMonitoring()
        }
    }

    // ==================== 发送方法 ====================

    suspend fun send(
        event: AppEvent,
        priority: Priority = Priority.NORMAL,
        usePool: Boolean = false,//高频事件流场景下使用false显著降低 GC 压力
        cacheable: Boolean = false
    ): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val eventName = event::class.simpleName ?: "Unknown"
        logger.debug { "ultimateSend event:${event} startTime: $startTime  eventName: $eventName" }

        if (circuitBreaker.isOpen()) {
            droppedCounter.incrementAndGet()
            stats.recordEvent(eventName, false, 0)
            logger.warn { "Circuit breaker open, dropping: $eventName" }
            return false
        }

        if (cacheable) {
            cacheEvent(event)
        }

        return try {
            val dispatcher = dispatchers[priority] ?: dispatchers[Priority.NORMAL]!!
            withContext(dispatcher) {
                val finalEvent = if (usePool) eventPool.acquire { event } else event
                try {
                    val result = _events.tryEmit(finalEvent)
                    val duration = Clock.System.now().toEpochMilliseconds() - startTime
                    eventCounter.incrementAndGet()
                    stats.recordEvent(eventName, result, duration)
                    if (result) {
                        circuitBreaker.recordSuccess()
                    } else {
                        droppedCounter.incrementAndGet()
                        circuitBreaker.recordFailure()
                    }
                    if (duration > 10_000_000) {
                        logger.warn { "Slow event: $event took ${duration / 1_000_000}ms" }
                    }
                    result
                } finally {
                    if (usePool && finalEvent is Resettable) {
                        eventPool.release(finalEvent)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Send failed: $eventName" }
            circuitBreaker.recordFailure()
            droppedCounter.incrementAndGet()
            stats.recordEvent(eventName, false, Clock.System.now().toEpochMilliseconds() - startTime)
            false
        }
    }

    // ==================== 订阅方法 ====================
    inline fun <reified T : AppEvent> subscribe(
        scope: CoroutineScope,
        priority: Priority = Priority.NORMAL,
        crossinline onEvent: suspend (T) -> Unit
    ): Job {
        return scope.launch(dispatchers[priority] ?: Dispatchers.IO) {
            events
                .filterIsInstance<T>()
                .catch { e -> logger.error(e) { "Subscribe error" } }
                .collect { onEvent(it) }
        }
    }

    inline fun <reified T : AppEvent> subscribeSticky(
        scope: CoroutineScope,
        crossinline onEvent: suspend (T) -> Unit
    ) {
        scope.launch {
            val cachedEvent = getCachedEvent<T>()
            logger.info { "cachedEvent: $cachedEvent" }
            if (cachedEvent != null) {
                onEvent(cachedEvent)
            }
        }
        subscribe<T>(scope) { event ->
            onEvent(event)
        }
    }

    // ==================== 缓存 ====================
    suspend inline fun <reified T : AppEvent> getCachedEvent(): T? {
        return cacheMutex.withLock {
            l1Cache[T::class]
                ?.lastOrNull() as? T
        }
    }

    suspend fun clearCache() {
        cacheMutex.withLock {
            l1Cache.clear()
        }
    }

    private suspend fun cacheEvent(event: AppEvent) {
        val eventClass = event::class
        cacheMutex.withLock {
            val list = l1Cache.getOrPut(eventClass) { mutableListOf() }
            list.add(event)
            if (list.size > MAX_CACHE_PER_TYPE) list.removeFirst()
        }
    }

    // ==================== 统计 ====================
    suspend fun getStats(): UltimateStats {
        return UltimateStats(
            totalEvents = eventCounter.value,
            droppedEvents = droppedCounter.value,
            bufferSize = _events.replayCache.size,
            cacheSize = l1Cache.size,
            circuitBreakerState = circuitBreaker.state.name,
            poolStats = eventPool.getStats(),
            eventStats = stats.getStats()
        )
    }


    private fun startMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(30.seconds)
                val stats = getStats()
                logger.debug {
                    """
                    📊 EventBus Stats:
                    Total: ${stats.totalEvents}
                    Dropped: ${stats.droppedEvents}
                    Buffer: ${stats.bufferSize}
                    Cache: ${stats.cacheSize}
                    Circuit: ${stats.circuitBreakerState}
                    Pool Hit Rate: ${(stats.poolStats.hitRate * 10000).toLong() / 100.0}%
                    Uptime: ${stats.eventStats.uptime / 1000}s
                """.trimIndent()
                }
            }
        }
    }
}

// ==================== 数据类（保持不变） ====================
data class UltimateStats(
    val totalEvents: Long,
    val droppedEvents: Long,
    val bufferSize: Int,
    val cacheSize: Int,
    val circuitBreakerState: String,
    val poolStats: EventPool.PoolStats,
    val eventStats: EventStats.Stats
)