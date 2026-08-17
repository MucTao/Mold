@file:Suppress("unused")

package org.muc.eventbus.event.core

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds


class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val timeout: Long = 5000L,
    private val halfOpenMaxAttempts: Int = 3
) {
    enum class State { CLOSED, OPEN, HALF_OPEN }

    private val _state = atomic(State.CLOSED.ordinal)
    private val failureCount = atomic(0)
    private val lastFailureTime = atomic(0L)
    private val halfOpenAttempts = atomic(0)

    val state: State
        get() = State.entries[_state.value]

    fun isOpen(): Boolean = _state.value == State.OPEN.ordinal

    fun recordFailure() {
        failureCount.incrementAndGet()
        lastFailureTime.value = Clock.System.now().toEpochMilliseconds()

        if (failureCount.value >= failureThreshold) {
            _state.value = State.OPEN.ordinal
        }
    }

    fun recordSuccess() {
        when (state) {
            State.HALF_OPEN -> {
                halfOpenAttempts.incrementAndGet()
                if (halfOpenAttempts.value >= halfOpenMaxAttempts) {
                    reset()
                }
            }

            State.CLOSED -> {
                failureCount.value = 0
            }

            else -> {}
        }
    }

    fun tryReset(): Boolean {
        return when (state) {
            State.OPEN -> {
                if (Clock.System.now().toEpochMilliseconds() - lastFailureTime.value > timeout) {
                    _state.value = State.HALF_OPEN.ordinal
                    halfOpenAttempts.value = 0
                    true
                } else {
                    false
                }
            }

            else -> false
        }
    }

    fun reset() {
        _state.value = State.CLOSED.ordinal
        failureCount.value = 0
        halfOpenAttempts.value = 0
    }

    // 自动恢复检查
    fun startAutoRecovery(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (true) {
                delay(timeout.milliseconds)
                if (state == State.OPEN) {
                    tryReset()
                }
            }
        }
    }
}