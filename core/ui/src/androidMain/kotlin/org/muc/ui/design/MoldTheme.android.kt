package org.muc.ui.design

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun dynamicColor(darkTheme: Boolean): ColorScheme? {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    return when {
        !dynamicColor -> null
        darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        else -> dynamicLightColorScheme(LocalContext.current)
    }
}