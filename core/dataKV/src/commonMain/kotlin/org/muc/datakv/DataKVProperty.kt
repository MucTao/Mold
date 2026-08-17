package org.muc.datakv

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class DataKVProperty<V>(private val default: V) : ReadOnlyProperty<IDataKVOwner, DataKVDelegate<V>> {
    private var cache: DataKVDelegate<V>? = null
    override fun getValue(thisRef: IDataKVOwner, property: KProperty<*>): DataKVDelegate<V> =
        cache ?: createDelegate(thisRef, property, default).also {
            cache = it
        }

    abstract fun createDelegate(thisRef: IDataKVOwner, property: KProperty<*>, default: V): DataKVDelegate<V>
}