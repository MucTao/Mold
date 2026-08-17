@file:Suppress("unused")

package org.muc.eventbus.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import org.muc.eventbus.event.AppEvent
import org.muc.eventbus.event.Priority
import org.muc.eventbus.event.core.EventBus
import org.muc.eventbus.event.core.logger
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds


// 批量发送扩展
suspend fun EventBus.sendAll(
    events: List<AppEvent>,
    priority: Priority = Priority.NORMAL
) {
    events.forEach { event ->
        send(event, priority)
    }
}

suspend fun EventBus.sendStackEvent(
    event: AppEvent,
    priority: Priority = Priority.NORMAL,
    usePool: Boolean = false
) {
    send(event, priority, usePool = usePool, cacheable = true)
}

// 响应式操作符
@OptIn(FlowPreview::class)
fun EventBus.debounceEvents(
    timeout: kotlin.time.Duration = 300.milliseconds
): Flow<AppEvent> {
    return events.debounce(timeout)
}

@OptIn(FlowPreview::class)
fun EventBus.throttleEvents(
    timeout: kotlin.time.Duration = 1000.milliseconds
): Flow<AppEvent> {
    return events.sample(timeout)
}

// 监控工具
fun EventBus.monitor(
    scope: CoroutineScope,
    interval: kotlin.time.Duration = 30000.milliseconds
) {
    scope.launch {
        while (true) {
            kotlinx.coroutines.delay(interval)
            val stats = getStats()
            logger.debug { "Stats: ${stats.totalEvents} events, ${stats.droppedEvents} dropped" }
        }
    }
}

// 事件追踪
inline fun <reified T : AppEvent> EventBus.trackEvents(
    scope: CoroutineScope,
    crossinline onEvent: suspend (T) -> Unit
) {
    subscribe<T>(scope) { event ->
        val start = Clock.System.now().toEpochMilliseconds()
        try {
            onEvent(event)
            val duration = Clock.System.now().toEpochMilliseconds() - start
            if (duration > 100) {
                logger.warn { "Slow event: ${event::class.simpleName} took ${duration}ms" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Event failed: ${event::class.simpleName}" }
        }
    }
}

@Composable
inline fun <reified T : AppEvent> EventBus.EventBusCollector(
    priority: Priority = Priority.NORMAL,
    crossinline onEvent: suspend (T) -> Unit
) {
    // LaunchedEffect 会在组件进入组合时启动，离开时自动取消，防止内存泄漏
    LaunchedEffect(Unit) {
        // 调用你提供的 EventBus 的 subscribe 方法
        subscribe<T>(
            scope = this, // 将 LaunchedEffect 的协程作用域传入
            priority = priority,
            onEvent = { event -> onEvent(event) }
        )
    }
}

@Composable
inline fun <reified T : AppEvent> EventBus.EventBusStickyCollector(
    crossinline onEvent: suspend (T) -> Unit
) {
    LaunchedEffect(Unit) {
        subscribeSticky<T>(
            scope = this,
            onEvent = { event -> onEvent(event) }
        )
    }
}
