package org.muc.datakv

import kotlinx.serialization.serializer
import org.muc.datakv.content.DataContentEngine
import org.muc.datakv.content.DataContentKVProperty
import org.muc.datakv.datastore.DataStoreCache
import org.muc.datakv.datastore.DataStoreKVProperty
import org.muc.datakv.datastore.createExpirableDataStore

//使用说明
//如果数据不需要跨应用
//class 目标类 : IDataKVOwner

// 如果数据要跨应用
// class 目标类 : IDataKVOwner by EngineProvider

interface IDataKVOwner {
    val engine: DataContentEngine? get() = null
}

inline fun <reified T> dataCross(default: T): DataKVProperty<T> =
    DataContentKVProperty(serializer<T>(), default)

inline fun <reified T> datakv(default: T, cross: Boolean = false): DataKVProperty<T> =
    if (cross) dataCross(default)
    else DataStoreKVProperty(DataStoreCache::createExpirableDataStore, default)