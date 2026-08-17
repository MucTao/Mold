@file:Suppress("unused")
package org.muc.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.muc.network.retry.Trigger

interface Incomplete

sealed class DataResult<out T>(
    val complete: Boolean,
    val shouldLoad: Boolean,
    open val trigger: Trigger?,
    private val value: T?,
) {
    open operator fun invoke(): T? = value
    suspend fun retry() = trigger?.retry()
    fun retry(scope: CoroutineScope) = scope.launch { retry() }
}

object Uninitialized : DataResult<Nothing>(complete = false, shouldLoad = true, trigger = null, value = null),
    Incomplete

data class DataLoading<out T>(override val trigger: Trigger? = null) :
    DataResult<T>(complete = false, shouldLoad = false, trigger = trigger, value = null), Incomplete

data class DataSuccess<out T>(private val value: T, override val trigger: Trigger? = null) :
    DataResult<T>(complete = true, shouldLoad = false, trigger = trigger, value = value), Incomplete {
    override operator fun invoke(): T = value
}

data class DataEmpty<out T>(override val trigger: Trigger? = null) :
    DataResult<T>(complete = true, shouldLoad = false, trigger = trigger, value = null), Incomplete

data class DataFail<out T>(val error: Throwable? = null, override val trigger: Trigger? = null) :
    DataResult<T>(complete = true, shouldLoad = true, trigger = trigger, value = null), Incomplete {
    constructor(msg: String, trigger: Trigger? = null) : this(IllegalStateException(msg), trigger)
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DataEmpty<T> -> DataEmpty(this.trigger)
        is DataFail<T> -> DataFail(this.error, this.trigger)
        is DataLoading<T> -> DataLoading(this.trigger)
        is DataSuccess<T> -> DataSuccess(transform(this()), this.trigger)
        Uninitialized -> Uninitialized
    }
}

inline fun <reified T> DataResult<T>.onSuccess(
    elseBlock: () -> Unit = {},
    action: (T) -> Unit
): DataResult<T> {
    val data = this()
    if (this is DataSuccess || data != null) action(data!!) else elseBlock()
    return this
}

/**
 * 用于展示空布局
 */
inline fun <reified T> DataResult<T>.onEmpty(action: () -> Unit): DataResult<T> {
    if (this is DataEmpty) action()
    return this
}

inline fun <reified T> DataResult<T>.onSuccessOrEmpty(action: (T?) -> Unit): DataResult<T> {
    if (this is DataSuccess || this is DataEmpty) action(this.data)
    return this
}

inline fun <reified T> DataResult<T>.onFail(action: DataResult<T>.(cause: Throwable?) -> Unit): DataResult<T> {
    if (this is DataFail) {
        action(error)
    }
    return this
}


inline fun <reified T> DataResult<T>.onLoading(action: () -> Unit): DataResult<T> {
    if (this is DataLoading && data == null) action()
    return this
}

inline fun <reified T> DataResult<T>.onComplete(action: () -> Unit): DataResult<T> {
    if (this.complete) action()
    return this
}

val <T> DataResult<T>.data get() = this.invoke()

fun <T> Flow<T?>.asDataResult(value: T? = null): Flow<DataResult<T>> {
    return this
        .map {
            when (it) {
                null -> DataEmpty()
                is List<*> if it.isEmpty() -> DataEmpty()
                else -> DataSuccess(it)
            }
        }
        .onStart { emit(DataLoading()) }
        .catch {
            if (it is NullPointerException) {
                emit(DataEmpty())
            } else
                emit(DataFail(it))
        }
}

suspend fun <T> Flow<T?>.asDataResult(getValue: suspend () -> T?): Flow<DataResult<T>> = asDataResult(getValue())

fun <T> Flow<Pair<Trigger, T?>>.asDataResult(tigger: Trigger? = null): Flow<DataResult<T>> {
    return this
        .map { (tiggerDef, valueDef) ->
            when (valueDef) {
                null -> DataEmpty(tigger ?: tiggerDef)
                is List<*> if valueDef.isEmpty() -> DataEmpty(tigger ?: tiggerDef)
                else -> DataSuccess(valueDef, tigger ?: tiggerDef)
            }
        }
        .onStart { emit(DataLoading(tigger)) }
        .catch {
            if (it is NullPointerException) {
                emit(DataEmpty())
            } else
                emit(DataFail(it))
        }
}


