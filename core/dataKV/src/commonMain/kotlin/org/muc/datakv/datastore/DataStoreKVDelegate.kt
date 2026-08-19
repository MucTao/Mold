@file:Suppress("unused")

package org.muc.datakv.datastore

import androidx.datastore.core.DataStore
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.muc.datakv.DataKVDelegate
import org.muc.datakv.ExpirableData
import org.muc.datakv.asDuration
import org.muc.datakv.di.NO_EXPIRATION
import org.muc.datakv.di.ioScope
import org.muc.datakv.di.nowMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DataStoreKVDelegate<V>(
    private val dataStore: DataStore<ExpirableData<V>>,
    override val defaultValue: V,
) : DataKVDelegate<V> {
    private val cleaned = atomic(false)

    override val flow: StateFlow<V> = dataStore.data.map { data ->
        expireTimeFlow.value = data.expireTime
        if (isExpired(data.expireTime)) {
            clearExpiredData()
            defaultValue
        } else {
            data.data ?: defaultValue
        }
    }.distinctUntilChanged().stateIn(
        scope = ioScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = defaultValue
    )

    override val expireTimeFlow: StateFlow<Long> field = MutableStateFlow(NO_EXPIRATION)

    override val expireTimeDurationFlow: Flow<Duration> = expireTimeFlow.asDuration()

    override suspend fun setValue(expireTime: Long, block: (V) -> V): V = withContext(Dispatchers.IO) {
        dataStore.updateData { pre ->
            val now = nowMillis()
            val expired = isExpired(expireTime, now)
            if (!expired) {
                if (expireTime != 0L)
                    clearExpiredData(expireTime.minus(now).milliseconds)
                val value: V = block(if (expired) defaultValue else pre.data ?: defaultValue)
                cleaned.value = false
                expireTimeFlow.value = expireTime
                pre.copy(data = value, expireTime = expireTime)
            } else {
                cleaned.value = true
                expireTimeFlow.value = NO_EXPIRATION
                pre.copy(data = null, expireTime = expireTime)
            }
        }.data ?: defaultValue
    }

    override fun clear() {
        clearExpiredData()
    }

    private fun isExpired(expireTime: Long, now: Long = nowMillis()): Boolean =
        expireTime in (NO_EXPIRATION + 1)..<now

    //==============================清理逻辑==============================================
    private var clearJob: Job? = null
    private val clearLock = SynchronizedObject()
    private fun clearExpiredData(duration: Duration? = null) {
        if (!cleaned.compareAndSet(expect = false, update = true)) {
            return // 已经在清理中
        }
        expireTimeFlow.value = NO_EXPIRATION
        clearJob?.cancel()
        clearJob = ioScope.launch {
            duration?.let { delay(it) }
            dataStore.updateData { ExpirableData(null, NO_EXPIRATION) }
            cleaned.value = false
        }
    }
}

