package org.muc.datakv.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import okio.Path.Companion.toPath
import org.muc.datakv.ExpirableData
import org.muc.datakv.content.DataContentEngine
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.time.Clock

@OptIn(ExperimentalForeignApi::class)
private fun getDocumentDirectory(): NSURL {
    return requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
    )
}

actual fun getDataKVStorePath(name: String): String = "${getDocumentDirectory().path}/${name}.json"

/**
 * App Groups 功能。允许同一个开发者账号下的多个应用（或应用与扩展）访问一个共享的文件容器。
 * 1.配置 App Groups 权限：在 Xcode 中为所有需要共享数据的应用 Target 开启 App Groups 能力，并加入同一个 App Group
 */
@OptIn(ExperimentalForeignApi::class)
object DataKV : DataContentEngine {
    private lateinit var _group: String
    val group get() = _group

    fun init(appGroupIdentifier: String) {
        this._group = appGroupIdentifier
    }

    private val sharedContainerPath: String? by lazy { NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(group)?.path }

    private val dataStore: DataStore<Preferences>? by lazy {
        sharedContainerPath?.let { path ->
            PreferenceDataStoreFactory.createWithPath(scope = ioScope) { "$path/data_kv.preferences_pb".toPath() }
        }
    }

    override fun <T> put(key: String, value: T, serializer: KSerializer<T>, expireTime: Long) {
        if (value == null) {
            delete(key)
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val expired = expireTime in (NO_EXPIRATION + 1)..<now
            if (expired) {
                delete(key)
            } else {
                val data = ExpirableData<T>(value, expireTime)
                ioScope.launch {
                    dataStore?.edit { prefs ->
                        prefs[stringPreferencesKey(key)] = json.encodeToString(ExpirableData.serializer(serializer), data)
                    }
                }
            }
        }
    }

    override fun delete(key: String) {
        val preferencesKey = stringPreferencesKey(key)
        ioScope.launch {
            dataStore?.edit { prefs ->
                val contains = prefs.contains(preferencesKey)
                if (contains) {
                    prefs.remove(preferencesKey)
                }
            }
        }
    }

    override suspend fun <T> get(key: String, serializer: KSerializer<T>, default: T): ExpirableData<T> {
        val resStr = dataStore?.data?.map { prefs -> prefs[stringPreferencesKey(key)] }?.first() ?: return ExpirableData(default)
        val value = json.decodeFromString(ExpirableData.serializer(serializer), resStr)
        val now = Clock.System.now().toEpochMilliseconds()
        val expired = value.expireTime in (NO_EXPIRATION + 1)..<now

        return if (expired || value.data == null) {
            delete(key)
            ExpirableData(default)
        } else {
            value
        }
    }

    override val valueChangeKeyFlow: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1024,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

}

actual fun createDataEngine(): DataContentEngine = DataKV