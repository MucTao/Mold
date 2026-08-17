package org.muc.mold

import android.app.Application

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        AppProvider.init(this)
    }
}