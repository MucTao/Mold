package org.muc.datakv.content

import kotlinx.serialization.KSerializer
import org.muc.datakv.DataKVProperty
import org.muc.datakv.IDataKVOwner
import kotlin.reflect.KProperty


class DataContentKVProperty<V>(
    private val serializer: KSerializer<V>,
    private val engine: DataContentEngine,
    private val storeName: String?,
    default: V,
) : DataKVProperty<V>(default) {

    override fun createDelegate(thisRef: IDataKVOwner, property: KProperty<*>, default: V): DataContentKVDelegate<V> =
        DataContentKVDelegate(serializer, engine, if (storeName == null) property.name else "${storeName}_${property.name}", default)
}



