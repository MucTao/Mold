package org.muc.network.retry

import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import org.muc.network.status.DataFlowEmpty
import org.muc.network.status.DataFlowFail
import org.muc.network.status.DataFlowLoading
import org.muc.network.status.DataFlowResult
import org.muc.network.status.DataFlowSuccess
import kotlin.time.Duration.Companion.milliseconds


interface RetryRequest {
    fun <T> refreshableRequest(
        trigger: Trigger = Trigger(), // 外部传入的触发器
        request: suspend () -> Flow<T?>
    ): Flow<DataFlowResult<T>>
}

object RetryRequestImpl : RetryRequest {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <T> refreshableRequest(
        trigger: Trigger,
        request: suspend () -> Flow<T?>
    ): Flow<DataFlowResult<T>> = trigger.getRetryFlow()
        .flatMapLatest {
            val res: Flow<DataFlowResult<T>> = request()
                .map { value ->
                    when (value) {
                        null -> DataFlowEmpty(trigger)
                        is List<*> -> if (value.isEmpty()) DataFlowEmpty(trigger) else DataFlowSuccess(value, trigger)
                        else -> DataFlowSuccess(value, trigger)
                    }
                }
                .onStart { emit(DataFlowLoading(trigger)) }
                .catch {
                    if (it is NullPointerException) {
                        emit(DataFlowEmpty(trigger))
                    } else
                        emit(DataFlowFail(it, trigger))
                }
            res
        } //.distinctUntilChanged() 避免重复结果
}

interface ApiRequest : RetryRequest {
    fun <T> request(request: suspend () -> T): Flow<T?>
    fun <T> reRequest(request: suspend () -> T?): Flow<DataFlowResult<T>>
}

object ApiRequestImpl : ApiRequest, RetryRequest by RetryRequestImpl {
    override fun <T> request(request: suspend () -> T): Flow<T?> = flow { emit(request()) }
        .retry()
        .flowOn(Dispatchers.IO)

    override fun <T> reRequest(request: suspend () -> T?): Flow<DataFlowResult<T>> = refreshableRequest {
        request {
            request()
        }
    }
}


fun <T> Flow<T>.retry() = this.retryWhen { cause, attempt ->
    println(cause.toString())
    if (attempt <= 10 && cause !is JsonConvertException) {
        delay((1000L..5000).random().milliseconds) // 重试随机间隔
        true // 允许重试
    } else {
        false // 超过次数/其他异常，不重试
    }
}