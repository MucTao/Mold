@file:Suppress("unused")

package org.muc.datakv.content

import org.muc.datakv.IDataKVOwner
import org.muc.datakv.di.createDataEngine

object EngineProvider : IDataKVOwner {
    override val engine: DataContentEngine = createDataEngine()
}