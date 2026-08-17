package org.muc.datakv.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
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
            // ✅ 使用 reified 类型
            return json.decodeFromString<ExpirableData<T>>(str)
        }

        override suspend fun writeTo(t: ExpirableData<T>, sink: BufferedSink) {
            sink.use {
                it.writeUtf8(json.encodeToString(t))
            }
        }
    }
}

inline fun <reified T> createExpirableDataStore(filePath: String): DataStore<ExpirableData<T>> {
    val storage = OkioStorage<ExpirableData<T>>(
        producePath = { getDataKVStorePath("$filePath.json").toPath() },
        fileSystem = FileSystem.SYSTEM,
        serializer = createTypedSerializer()
    )
    return DataStoreFactory.create(storage = storage)
}