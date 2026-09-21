@file:Suppress("unused")

package org.muc.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.muc.network.retry.Trigger

interface Incomplete

sealed class DataResult<out T>(
    val shouldLoad: Boolean,
    open val trigger: Trigger?,
    private val value: T?,
) {
    open operator fun invoke(): T? = value
    suspend fun retry() = trigger?.retry()
    fun retry(scope: CoroutineScope) = scope.launch { retry() }
}

data class DataSuccess<out T>(private val value: T, override val trigger: Trigger? = null) :
    DataResult<T>(shouldLoad = false, trigger = trigger, value = value), Incomplete {
    override operator fun invoke(): T = value
}

data class DataEmpty<out T>(override val trigger: Trigger? = null) :
    DataResult<T>(shouldLoad = false, trigger = trigger, value = null), Incomplete

data class DataFail<out T>(val error: Throwable? = null, override val trigger: Trigger? = null) :
    DataResult<T>(shouldLoad = true, trigger = trigger, value = null), Incomplete {
    constructor(msg: String, trigger: Trigger? = null) : this(IllegalStateException(msg), trigger)
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DataEmpty<T> -> DataEmpty(this.trigger)
        is DataFail<T> -> DataFail(this.error, this.trigger)
        is DataSuccess<T> -> DataSuccess(transform(this()), this.trigger)
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
    if (this is DataSuccess || this is DataEmpty) action(this())
    return this
}

inline fun <reified T> DataResult<T>.onFail(action: DataResult<T>.(cause: Throwable?) -> Unit): DataResult<T> {
    if (this is DataFail) {
        action(error)
    }
    return this
}

fun <T> T?.asDataResult(): DataResult<T> {
    return when (this) {
        null -> DataEmpty()
        is List<*> if this.isEmpty() -> DataEmpty()
        else -> DataSuccess(this)
    }
}

suspend inline fun <T> runCatchingSuspendAsDataResult(
    crossinline block: suspend () -> T,
): DataResult<T> =
    withContext(Dispatchers.IO) {
        try {
            block().asDataResult()
        } catch (e: CancellationException) {
            DataFail(e)
        } catch (e: Throwable) {//TODO 处理外层HttpCode等于200 内层code!=$code的情况
            DataFail(e)
        }
    }


suspend inline fun <T, R> runCatchingSuspendAsDataResult(
    crossinline block: suspend () -> T,
    crossinline v2r: suspend (T) -> DataResult<R>,
): DataResult<R> =
    withContext(Dispatchers.IO) {
        try {
            val res = block()
            v2r(res)
        } catch (e: CancellationException) {
            DataFail(e)
        } catch (e: Throwable) {//TODO 处理外层HttpCode等于200 内层code!=$code的情况
            DataFail(e)
        }
    }

inline fun <T> runCatchingAsDataResult(
    crossinline block: () -> T,
): DataResult<T> =
    try {
        block().asDataResult()
    } catch (e: CancellationException) {
        DataFail(e)
    } catch (e: Throwable) {//TODO 处理外层HttpCode等于200 内层code!=$code的情况
        DataFail(e)
    }

inline fun <T, R> runCatchingAsDataResult(
    crossinline block: () -> T,
    crossinline v2r: (T) -> DataResult<R>,
): DataResult<R> =
    try {
        v2r(block())
    } catch (e: CancellationException) {
        DataFail(e)
    } catch (e: Throwable) {//TODO 处理外层HttpCode等于200 内层code!=$code的情况
        DataFail(e)
    }