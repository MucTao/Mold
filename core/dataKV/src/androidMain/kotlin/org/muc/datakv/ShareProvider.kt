package org.muc.datakv

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.ContentObserver
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.Keep
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.muc.datakv.di.DataKV.valueChangeKeyFlow

@Keep
class ShareProvider : ContentProvider() {

    private val Context.dataStore by preferencesDataStore(TABLE)

    companion object {
        @JvmStatic
        var AUTHORITY = "org.muc.content.provider.ShareProvider"

        @JvmStatic
        var TABLE = "MucCP"

        val providerUri: Uri get() = Uri.parse("content://$AUTHORITY/$TABLE")

        private const val CODE_SINGLE_ITEM = 1
        private const val COLUMN_ID = "_id" // ContentProvider强制要求的主键列
        const val COLUMN_VALUE = "value" // DataStore的值
        private val URI_MATCHER by lazy {
            UriMatcher(UriMatcher.NO_MATCH).apply {
                // Uri匹配码：匹配单个键（如content://AUTHORITY/MucCP/Key）
                addURI(AUTHORITY, "$TABLE/*", CODE_SINGLE_ITEM)
            }
        }
        const val TAG = "MucCP"

        val observer by lazy {
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                // 数据变化时回调（主线程执行）
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    // uri 是发生变化的数据源 URI，可据此判断是哪个数据变了 example: content://com.jf.smartscreen.jfshareprovider/jfkv/orgCode
                    uri?.let {
                        // 核心操作：重新查询最新数据
                        val matchCode = URI_MATCHER.match(uri)
                        if (matchCode != CODE_SINGLE_ITEM) return
                        val key = uri.lastPathSegment ?: return
                        Log.i(TAG, "监听到 : uri = $uri, key = $key 变化")
                        valueChangeKeyFlow.tryEmit(key)
                    }
                }
            }
        }
    }

    override fun onCreate(): Boolean = true
    override fun getType(uri: Uri): String? {
        val matchCode = URI_MATCHER.match(uri)
        return if (matchCode == CODE_SINGLE_ITEM) "vnd.android.cursor.item/$AUTHORITY.$TABLE" else null
    }

    //adb shell content query  --uri  content://com.jf.base.provider.JfShareProvider/JfKV/httpLogEnabled --where java.lang.Boolean
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        where: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        Log.i(TAG, "query : uri = $uri")
        val cursor = MatrixCursor(arrayOf(COLUMN_ID, COLUMN_VALUE))
        if (!isValidUri(uri)) return cursor
        val key = uri.lastPathSegment ?: return cursor
        // 使用 runBlocking 是为了兼容 ContentProvider 的同步接口
        // 但由于 DataStore 操作很快，这里可以接受
        runCatching {
            val value = runBlocking {
                context?.dataStore?.data
                    ?.map { it[stringPreferencesKey(key)] }
                    ?.first()
            }
            Log.i(TAG, "query : $key = $value")
            cursor.addRow(arrayOf(key, value))
        }.onFailure {
            Log.e(TAG, "Query failed for key: $key", it)
        }
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        Log.i(TAG, "insert : uri = $uri, values = $values")
        update(uri, values, null, null)
        return uri
    }

    //adb shell content update --uri content://com.jf.base.provider.JfShareProvider/JfKV/httpLogEnabled --bind httpLogEnabled:i:1 --where java.lang.Boolean
    override fun update(uri: Uri, values: ContentValues?, where: String?, selectionArgs: Array<String>?): Int {
        Log.i(TAG, "update : uri = $uri  values = $values")
        if (!isValidUri(uri)) return 0
        val key = uri.lastPathSegment ?: return 0
        // 如果 values 为 null 或没有对应 key 的值，执行删除操作
        if (values == null || !values.containsKey(key)) {
            return delete(uri, where, selectionArgs)
        }
        val value = values.getAsString(key) ?: return 0

        return runCatching {
            runBlocking {
                context?.dataStore?.edit { preferences ->
                    preferences[stringPreferencesKey(key)] = value
                }
            } ?: return 0

            Log.i(TAG, "update : $key = $value")
            notifyDataChanged(uri)
            1
        }.getOrElse {
            Log.e(TAG, "Update failed for key: $key", it)
            0
        }
    }

    override fun delete(uri: Uri, where: String?, selectionArgs: Array<String>?): Int {
        Log.i(TAG, "delete : uri = $uri")
        if (!isValidUri(uri)) return 0
        val key = uri.lastPathSegment ?: return 0
        return runCatching {
            val preferencesKey = stringPreferencesKey(key)
            var deleted = false
            runBlocking {
                context?.dataStore?.edit { preferences ->
                    if (preferences.contains(preferencesKey)) {
                        preferences.remove(preferencesKey)
                        deleted = true
                    }
                }
            }
            if (deleted) {
                Log.i(TAG, "delete : $key deleted")
                notifyDataChanged(uri)
                1
            } else {
                Log.i(TAG, "delete : $key not found")
                0
            }
        }.getOrElse {
            Log.e(TAG, "Delete failed for key: $key", it)
            0
        }
    }

    private fun isValidUri(uri: Uri): Boolean {
        return URI_MATCHER.match(uri) == CODE_SINGLE_ITEM.also {
            Log.i(TAG, "isValidUri : uri = $uri, matcherValue = $it")
        }
    }

    private fun notifyDataChanged(uri: Uri) {
        try {
            context?.contentResolver?.notifyChange(uri, observer)
            Log.i(TAG, "notifyChange : uri = $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify change", e)
        }
    }

}