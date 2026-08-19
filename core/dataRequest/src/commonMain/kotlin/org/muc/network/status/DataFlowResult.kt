@file:Suppress("unused")

package org.muc.network.status

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.muc.network.DataEmpty
import org.muc.network.DataFail
import org.muc.network.DataResult
import org.muc.network.DataSuccess
import org.muc.network.Incomplete
import org.muc.network.retry.Trigger


sealed class DataFlowResult<out T>(
    val complete: Boolean,
    val shouldLoad: Boolean,
    open val trigger: Trigger?,
    private val value: T?,
) {
    open operator fun invoke(): T? = value
    suspend fun retry() = trigger?.retry()
    fun retry(scope: CoroutineScope) = scope.launch { retry() }
}

object Uninitialized : DataFlowResult<Nothing>(complete = false, shouldLoad = true, trigger = null, value = null),
    Incomplete

data class DataFlowLoading<out T>(override val trigger: Trigger? = null) :
    DataFlowResult<T>(complete = false, shouldLoad = false, trigger = trigger, value = null), Incomplete

data class DataFlowSuccess<out T>(private val value: T, override val trigger: Trigger? = null) :
    DataFlowResult<T>(complete = true, shouldLoad = false, trigger = trigger, value = value), Incomplete {
    override operator fun invoke(): T = value
}

data class DataFlowEmpty<out T>(override val trigger: Trigger? = null) :
    DataFlowResult<T>(complete = true, shouldLoad = false, trigger = trigger, value = null), Incomplete

data class DataFlowFail<out T>(val error: Throwable? = null, override val trigger: Trigger? = null) :
    DataFlowResult<T>(complete = true, shouldLoad = true, trigger = trigger, value = null), Incomplete {
    constructor(msg: String, trigger: Trigger? = null) : this(IllegalStateException(msg), trigger)
}

inline fun <T, R> DataFlowResult<T>.map(transform: (T) -> R): DataFlowResult<R> {
    return when (this) {
        is DataFlowEmpty<T> -> DataFlowEmpty(this.trigger)
        is DataFlowFail<T> -> DataFlowFail(this.error, this.trigger)
        is DataFlowLoading<T> -> DataFlowLoading(this.trigger)
        is DataFlowSuccess<T> -> DataFlowSuccess(transform(this()), this.trigger)
        Uninitialized -> Uninitialized
    }
}

inline fun <T, R> DataFlowResult<T>.asResult(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DataFlowFail<T> -> DataFail(this.error, this.trigger)
        is DataFlowSuccess<T> -> DataSuccess(transform(this()), this.trigger)
        else -> DataEmpty(this.trigger)
    }
}

inline fun <reified T> DataFlowResult<T>.onSuccess(
    elseBlock: () -> Unit = {},
    action: (T) -> Unit
): DataFlowResult<T> {
    val data = this()
    if (this is DataFlowSuccess || data != null) action(data!!) else elseBlock()
    return this
}

/**
 * 用于展示空布局
 */
inline fun <reified T> DataFlowResult<T>.onEmpty(action: () -> Unit): DataFlowResult<T> {
    if (this is DataFlowEmpty) action()
    return this
}

inline fun <reified T> DataFlowResult<T>.onSuccessOrEmpty(action: (T?) -> Unit): DataFlowResult<T> {
    if (this is DataFlowSuccess || this is DataFlowEmpty) action(this.data)
    return this
}

inline fun <reified T> DataFlowResult<T>.onFail(action: DataFlowResult<T>.(cause: Throwable?) -> Unit): DataFlowResult<T> {
    if (this is DataFlowFail) {
        action(error)
    }
    return this
}


inline fun <reified T> DataFlowResult<T>.onLoading(action: () -> Unit): DataFlowResult<T> {
    if (this is DataFlowLoading && data == null) action()
    return this
}

inline fun <reified T> DataFlowResult<T>.onComplete(action: () -> Unit): DataFlowResult<T> {
    if (this.complete) action()
    return this
}

val <T> DataFlowResult<T>.data get() = this.invoke()

fun <T> Flow<T?>.asDataStatusResult(value: T? = null): Flow<DataFlowResult<T>> {
    return this
        .map {
            when (it) {
                null -> DataFlowEmpty()
                is List<*> if it.isEmpty() -> DataFlowEmpty()
                else -> DataFlowSuccess(it)
            }
        }
        .onStart { emit(DataFlowLoading()) }
        .catch {
            if (it is NullPointerException) {
                emit(DataFlowEmpty())
            } else
                emit(DataFlowFail(it))
        }
}

suspend fun <T> Flow<T?>.asDataStatusResult(getValue: suspend () -> T?): Flow<DataFlowResult<T>> = asDataStatusResult(getValue())

fun <T> Flow<Pair<Trigger, T?>>.asDataStatusResult(tigger: Trigger? = null): Flow<DataFlowResult<T>> {
    return this
        .map { (tiggerDef, valueDef) ->
            when (valueDef) {
                null -> DataFlowEmpty(tigger ?: tiggerDef)
                is List<*> if valueDef.isEmpty() -> DataFlowEmpty(tigger ?: tiggerDef)
                else -> DataFlowSuccess(valueDef, tigger ?: tiggerDef)
            }
        }
        .onStart { emit(DataFlowLoading(tigger)) }
        .catch {
            if (it is NullPointerException) {
                emit(DataFlowEmpty())
            } else
                emit(DataFlowFail(it))
        }
}


