package org.muc.ui.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy

val defaultDialogMetadata = DialogSceneStrategy.dialog(
    DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = true,
    )
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberListDetailStrategy(): ListDetailSceneStrategy<NavKey> {
    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive: PaneScaffoldDirective = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo).copy(horizontalPartitionSpacerSize = 0.dp)
    }
    return rememberListDetailSceneStrategy(directive = directive)
}