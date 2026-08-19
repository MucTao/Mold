package org.muc.datakv.content

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.KSerializer
import org.muc.datakv.ExpirableData
import org.muc.datakv.di.NO_EXPIRATION


interface DataContentEngine {

    suspend fun <T> put(key: String, value: T, serializer: KSerializer<T>, expireTime: Long = NO_EXPIRATION): T

    fun delete(key: String)

    suspend fun <T> get(key: String, serializer: KSerializer<T>, default: T): ExpirableData<T>

    val valueChangeKeyFlow: MutableSharedFlow<String>
}
