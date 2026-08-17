package org.muc.datakv.di

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import org.muc.datakv.ShareProvider
import org.muc.datakv.content.DataContentEngine
import org.muc.datakv.ExpirableData
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object DataKV : DataContentEngine {
    private lateinit var _app: Application
    val app get() = _app

    /**
     * @param authority :必须和AndroidManifest.xml 中注册的 authorities一致
     */
    fun init(app: Application, authority: String = ShareProvider.AUTHORITY, table: String = ShareProvider.TABLE) {
        this._app = app
        ShareProvider.AUTHORITY = authority
        ShareProvider.TABLE = table
        registerContentObserver()
    }


    private val cr: ContentResolver by lazy { _app.contentResolver }


    override fun <T> put(key: String, value: T, serializer: KSerializer<T>, expireTime: Long) {
        Log.i(ShareProvider.TAG, "DataContentEngine put : $key = $value expireTime=$expireTime")
        if (value == null) {
            Log.i(ShareProvider.TAG, "DataContentEngine put : 传入的值为空 执行delete($key)")
            delete(key)
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val expired = expireTime in (NO_EXPIRATION + 1)..<now
            if (expired) {
                Log.i(ShareProvider.TAG, "DataContentEngine put : 传入的过期时间 执行delete($key)")
                delete(key)
            } else {
                clearExpiredData(expireTime.minus(now).milliseconds, key)
                val data = ExpirableData<T>(value, expireTime)
                val values = ContentValues()
                values.put(key, json.encodeToString(ExpirableData.serializer(serializer), data))
                cr.update(
                    Uri.withAppendedPath(
                        ShareProvider.providerUri, key
                    ), values, null, null
                )
            }
        }
    }

    override fun delete(key: String) {
        val deletedRows = cr.delete(Uri.withAppendedPath(ShareProvider.providerUri, key), null, null)
        Log.i(ShareProvider.TAG, "Deleted $deletedRows rows for key: $key ")
    }

    override suspend fun <T> get(key: String, serializer: KSerializer<T>, default: T): ExpirableData<T> = runCatching {
        cr.query(
            Uri.withAppendedPath(
                ShareProvider.providerUri, key
            ), null, null, null, null
        )?.use { cursor ->
            if (cursor.moveToNext()) {
                val index = cursor.getColumnIndex(ShareProvider.COLUMN_VALUE)
                val resString = cursor.getString(index) ?: return@use ExpirableData(default)
                val value = json.decodeFromString(ExpirableData.serializer(serializer), resString)
                val now = Clock.System.now().toEpochMilliseconds()
                val expired = value.expireTime in (NO_EXPIRATION + 1)..<now
                if (expired || value.data == null) {
                    delete(key)
                    ExpirableData(default)
                } else {
                    value
                }
            } else ExpirableData(default)
        } ?: ExpirableData(default)
    }.getOrElse {
        Log.e(ShareProvider.TAG, "get: $key -> ${it.message}", it)
        ExpirableData(default)
    }

    //======================监听逻辑===============================================================================
    override val valueChangeKeyFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1024,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    private fun registerContentObserver() = runCatching {
        cr.registerContentObserver(
            ShareProvider.providerUri, // 监听所有 JfKV 数据变化
            true, // 包括子目录
            ShareProvider.observer // 自定义观察者
        )
    }

    //==============================清理逻辑==============================================
    private var clearJob: Job? = null
    private fun clearExpiredData(duration: Duration? = null, key: String) {
        clearJob?.cancel()
        clearJob = ioScope.launch {
            duration?.let { delay(it) }
            delete(key)
        }
    }
}

actual fun getDataKVStorePath(name: String): String = DataKV.app.filesDir.resolve(name).absolutePath

actual fun createDataEngine(): DataContentEngine = DataKV