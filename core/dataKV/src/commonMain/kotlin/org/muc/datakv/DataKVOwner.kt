package org.muc.datakv

import kotlinx.serialization.serializer
import org.muc.datakv.content.DataContentKVProperty
import org.muc.datakv.datastore.DataStoreCache
import org.muc.datakv.datastore.DataStoreKVProperty
import org.muc.datakv.datastore.createExpirableDataStore
import org.muc.datakv.di.createDataEngine

//使用说明
//基础用法
//class 目标类 : IDataKVOwner

//数据需要多次区分存储 可以重写 storeName
//class 目标类 : IDataKVOwner{
//  val storeName = "newName"
// }

// 如果数据要跨应用
// class 目标类 : IDataKVOwner {
//  val key by dataCross(default)
// }

interface IDataKVOwner {
    val storeName: String? get() = null
}

inline fun <reified T> IDataKVOwner.dataCross(default: T): DataKVProperty<T> =
    DataContentKVProperty(serializer<T>(), createDataEngine(), storeName,default)

inline fun <reified T> IDataKVOwner.datakv(default: T, cross: Boolean = false): DataKVProperty<T> =
    if (cross) dataCross(default)
    else {
        DataStoreKVProperty({
            if (storeName == null)
                DataStoreCache.createExpirableDataStore(it)
            else
                DataStoreCache.createExpirableDataStore("$storeName/$it")
        }, default)
    }