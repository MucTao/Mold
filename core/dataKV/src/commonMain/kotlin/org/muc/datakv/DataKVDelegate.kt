@file:Suppress("unused")

package org.muc.datakv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.muc.datakv.di.NO_EXPIRATION
import org.muc.datakv.di.nowMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 暴漏给用户的核心属性和方法
 */
interface DataKVDelegate<V> {
    val defaultValue: V

    val flow: Flow<V>

    suspend fun getValue() : V

    val expireTimeFlow: StateFlow<Long>
    val expireTime: Long get() = expireTimeFlow.value

    val expireTimeDurationFlow: Flow<Duration>

    suspend fun setValue(expireTime: Long = NO_EXPIRATION, block: (V) -> V): V

    suspend fun setValue(duration: Duration, block: (V) -> V): V =
        setValue(
            if (duration.isPositive()) nowMillis() + duration.inWholeMilliseconds else NO_EXPIRATION,
            block
        )


    suspend fun setValue(dateTime: LocalDateTime, block: (V) -> V): V =
        setValue(
            dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            block
        )


    /**
     * 清除后值会变成默认值
     */
    fun clear()
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun StateFlow<Long>.asDuration() = this
    .flatMapLatest { targetExpireTime ->
        flow {
            if (targetExpireTime == NO_EXPIRATION) {
                emit(Duration.ZERO)
                return@flow
            }

            while (currentCoroutineContext().isActive) {
                val remaining = (targetExpireTime - nowMillis()).milliseconds

                if (!remaining.isPositive()) {
                    emit(Duration.ZERO)
                    break
                }
                emit(remaining)
                val delayTime = when {
                    remaining > 1.minutes -> 1.minutes
                    remaining > 10.seconds -> 10.seconds
                    else -> 1.seconds
                }
                delay(delayTime)
            }
        }
    }
    .flowOn(Dispatchers.IO)