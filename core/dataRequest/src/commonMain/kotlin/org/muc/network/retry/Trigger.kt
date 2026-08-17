package org.muc.network.retry

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart

class Trigger {
    // 核心触发流：replay=0 不缓存，extraBufferCapacity=1 防止发送丢失
    private val tryFlow = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    suspend fun retry() = tryFlow.emit(Unit)
    internal fun getRetryFlow() = tryFlow.onStart { emit(Unit) }//初始化时自动触发第一次请求
}