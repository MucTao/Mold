@file:Suppress("unused")

package org.muc.datakv.content

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    override val valueFlow: StateFlow<V> field = MutableStateFlow(defaultValue)

    override val expireTimeFlow: StateFlow<Long> field = MutableStateFlow(NO_EXPIRATION)

    override val expireTimeDurationFlow: Flow<Duration> = expireTimeFlow.asDuration()

    override fun setValue(expireTime: Long, block: (V) -> V) {
        engine?.put(key, block(value), serializer, expireTime)
    }

    override fun clear() {
        expireTimeFlow.value = NO_EXPIRATION
        valueFlow.value = defaultValue
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
                logger.info { "dataContent：${str}变化后的值为：${valueFlow.value}" }
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
            if (newValue != valueFlow.value) {
                valueFlow.value = newValue
            }
        }
    }
}