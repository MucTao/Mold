package org.muc.ui.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.scene.DialogSceneStrategy

val defaultDialogMetadata = DialogSceneStrategy.dialog(
    DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = true,
    )
)