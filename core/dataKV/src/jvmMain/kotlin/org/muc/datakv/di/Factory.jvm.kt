package org.muc.datakv.di

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.KSerializer
import org.muc.datakv.content.DataContentEngine
import java.io.File

actual fun getDataKVStorePath(name: String): String = File(System.getProperty("user.home"), name).absolutePath
actual fun createDataEngine(): DataContentEngine = object : DataContentEngine {
    override fun <T> put(key: String, value: T, serializer: KSerializer<T>, expireTime: Long) {
        TODO("Not yet implemented")
    }

    override fun delete(key: String) {
        TODO("Not yet implemented")
    }

    override suspend fun <T> get(key: String, serializer: KSerializer<T>, default: T): T {
        TODO("Not yet implemented")
    }

    override val valueChangeKeyFlow: MutableSharedFlow<String>
        get() = TODO("Not yet implemented")
}