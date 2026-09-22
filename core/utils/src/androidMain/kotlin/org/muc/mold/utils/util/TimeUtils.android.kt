package org.muc.mold.utils.util

import android.provider.Settings
import androidx.annotation.RequiresPermission
import org.muc.mold.utils.util.Utils.app


internal actual fun getUsingNetworkTime(): Boolean = Settings.Global.getInt(app.contentResolver, Settings.Global.AUTO_TIME, 0) == 1

@RequiresPermission(android.Manifest.permission.WRITE_SETTINGS)
internal actual fun setUsingNetworkTime(use: Boolean) {
    Settings.Global.putInt(app.contentResolver, Settings.Global.AUTO_TIME, if (use) 1 else 0)
}