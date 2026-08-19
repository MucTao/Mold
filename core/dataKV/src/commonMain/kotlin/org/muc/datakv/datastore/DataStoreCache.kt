package org.muc.datakv.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.use
import org.muc.datakv.ExpirableData
import org.muc.datakv.di.getDataKVStorePath
import org.muc.datakv.di.json

inline fun <reified T> createTypedSerializer(): OkioSerializer<ExpirableData<T>> {
    return object : OkioSerializer<ExpirableData<T>> {
        override val defaultValue: ExpirableData<T> = ExpirableData()

        override suspend fun readFrom(source: BufferedSource): ExpirableData<T> {
            val str = source.readUtf8()
            return json.decodeFromString<ExpirableData<T>>(str)
        }

        override suspend fun writeTo(t: ExpirableData<T>, sink: BufferedSink) {
            sink.use {
                it.writeUtf8(json.encodeToString(t))
            }
        }
    }
}

object DataStoreCache {
    val lock = SynchronizedObject()
    val map = HashMap<String, DataStore<*>>()
}

inline fun <reified T> DataStoreCache.createExpirableDataStore(filePath: String): DataStore<ExpirableData<T>> {
    val cached = map[filePath]
    if (cached is DataStore<*>) {
        @Suppress("UNCHECKED_CAST")
        return cached as DataStore<ExpirableData<T>>
    }

    return synchronized(lock) {
        val doubleCheck = map[filePath]
        if (doubleCheck is DataStore<*>) {
            @Suppress("UNCHECKED_CAST")
            doubleCheck as DataStore<ExpirableData<T>>
        } else {
            val dataStore = DataStoreFactory.create(
                storage = OkioStorage<ExpirableData<T>>(
                    producePath = { getDataKVStorePath("$filePath.json").toPath() },
                    fileSystem = FileSystem.SYSTEM,
                    serializer = createTypedSerializer()
                )
            )
            map[filePath] = dataStore as DataStore<*>
            dataStore
        }
    }
}