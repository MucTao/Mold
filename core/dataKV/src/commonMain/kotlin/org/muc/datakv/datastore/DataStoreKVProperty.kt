@file:Suppress("unused")

package org.muc.datakv.datastore

import androidx.datastore.core.DataStore
import org.muc.datakv.DataKVProperty
import org.muc.datakv.ExpirableData
import org.muc.datakv.IDataKVOwner
import kotlin.reflect.KProperty

class DataStoreKVProperty<V>(
    private val getDataStore: (String) -> DataStore<ExpirableData<V>>,
    default: V,
) : DataKVProperty<V>(default) {
    override fun createDelegate(thisRef: IDataKVOwner, property: KProperty<*>, default: V): DataStoreKVDelegate<V> =
        DataStoreKVDelegate(dataStore = getDataStore(property.name), defaultValue = default)

}
