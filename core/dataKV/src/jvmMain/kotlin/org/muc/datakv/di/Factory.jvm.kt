package org.muc.datakv.di

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.KSerializer
import org.muc.datakv.ExpirableData
import org.muc.datakv.content.DataContentEngine
import java.io.File

actual fun getDataKVStorePath(name: String): String = File(System.getProperty("user.home"), name).absolutePath
actual fun createDataEngine(): DataContentEngine = object : DataContentEngine {
    override fun <T> put(key: String, value: T, serializer: KSerializer<T>, expireTime: Long) {

    }

    override fun delete(key: String) {

    }

    override suspend fun <T> get(key: String, serializer: KSerializer<T>, default: T): ExpirableData<T> {
        return ExpirableData(null, 0)
    }

    override val valueChangeKeyFlow: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1024,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
}