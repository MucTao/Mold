package org.muc.mold

import android.app.Application
import android.os.Build

object AppProvider {
    private lateinit var _app: Application
    val app get() = _app
    fun init(app: Application) {
        this._app = app
        org.muc.datakv.di.DataKV.init(app)
    }
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()