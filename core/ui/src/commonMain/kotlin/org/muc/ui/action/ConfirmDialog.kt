package org.muc.ui.action

import androidx.compose.runtime.Composable
import org.muc.ui.i18n.MucCommonStringRes
import org.muc.ui.alertdialogs.MucAlertDialog
import org.muc.ui.alertdialogs.MucAlertDialogAction
import org.muc.ui.buttons.MucButtonSize
import org.muc.ui.buttons.MucButtonType
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConfirmDialog(
    title: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    type: MucButtonType = MucButtonType.NEUTRAL,
    content: @Composable () -> Unit,
) {
    MucAlertDialog(
        onDismissRequest = onDismiss,
        title = title ?: "",
        confirmAction = MucAlertDialogAction(
            text = stringResource(MucCommonStringRes.actionConfirm),
            onClick = onConfirm,
            size = MucButtonSize.MEDIUM,
        ),
        dismissAction = MucAlertDialogAction(
            text = stringResource(MucCommonStringRes.actionCancel),
            onClick = onDismiss,
            type = type,
            size = MucButtonSize.MEDIUM,
        )
    ) {
        content()
    }
}