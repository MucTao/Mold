package org.muc.datakv.di

import org.muc.datakv.content.DataContentEngine

expect fun getDataKVStorePath(name: String): String

expect fun createDataEngine(): DataContentEngine
