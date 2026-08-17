package org.muc.datakv.content

import kotlinx.serialization.KSerializer
import org.muc.datakv.DataKVProperty
import org.muc.datakv.IDataKVOwner
import kotlin.reflect.KProperty


class DataContentKVProperty<V>(
    private val serializer: KSerializer<V>,
    default: V,
) : DataKVProperty<V>(default) {

    override fun createDelegate(thisRef: IDataKVOwner, property: KProperty<*>, default: V): DataContentKVDelegate<V> =
        DataContentKVDelegate(serializer, thisRef.engine, property.name, default)
}



