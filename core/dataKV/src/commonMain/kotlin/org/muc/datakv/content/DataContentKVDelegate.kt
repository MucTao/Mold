@file:Suppress("unused")

package org.muc.datakv.content

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import org.muc.datakv.DataKVDelegate
import org.muc.datakv.ExpirableData
import org.muc.datakv.asDuration
import org.muc.datakv.di.NO_EXPIRATION
import org.muc.datakv.di.ioScope
import org.muc.datakv.di.logger
import kotlin.time.Duration


class DataContentKVDelegate<V>(
    private val serializer: KSerializer<V>,
    private val engine: DataContentEngine?,
    private val key: String,
    override val defaultValue: V,
) : DataKVDelegate<V> {
    override val flow: StateFlow<V> field = MutableStateFlow(defaultValue)

    override suspend fun getValue(): V {
        val expirableData: ExpirableData<V> = engine?.get(key, serializer, defaultValue) ?: return defaultValue
        val newExpireTime = expirableData.expireTime
        if (newExpireTime != expireTimeFlow.value) {
            expireTimeFlow.value = expirableData.expireTime
        }
        val newValue = expirableData.data ?: defaultValue
        if (newValue != flow.value) {
            flow.value = newValue
        }
        return newValue
    }

    override val expireTimeFlow: StateFlow<Long> field = MutableStateFlow(NO_EXPIRATION)

    override val expireTimeDurationFlow: Flow<Duration> = expireTimeFlow.asDuration()

    override suspend fun setValue(expireTime: Long, block: (V) -> V): V = withContext(Dispatchers.IO) {
        engine?.put(key, block(flow.value), serializer, expireTime) ?: defaultValue
    }

    override fun clear() {
        expireTimeFlow.value = NO_EXPIRATION
        flow.value = defaultValue
        engine?.delete(key)
        logger.debug { "Cleared value for $key" }
    }

    init {
        if (engine == null) error("要使用跨应用数据请用 : IDataKVOwner by EngineProvider")
        loadByEngine()
        observeValueChanges()
    }

    private fun observeValueChanges() {
        engine?.valueChangeKeyFlow?.onEach { str ->
            if (str == key) {
                logger.info { "dataContent：${str}的值发生变化" }
                loadByEngine()
                logger.info { "dataContent：${str}变化后的值为：${flow.value}" }
            }
        }?.launchIn(ioScope)
    }

    private fun loadByEngine() {
        val engine = engine ?: return
        ioScope.launch {
            val expirableData: ExpirableData<V> = engine.get(key, serializer, defaultValue)
            val newExpireTime = expirableData.expireTime
            if (newExpireTime != expireTimeFlow.value) {
                expireTimeFlow.value = expirableData.expireTime
            }
            val newValue = expirableData.data ?: defaultValue
            if (newValue != flow.value) {
                flow.value = newValue
            }
        }
    }
}