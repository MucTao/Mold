package org.muc.ui.action

import androidx.compose.runtime.Composable
import org.muc.ui.i18n.MoldCommonStringRes
import org.muc.ui.alertdialogs.MoldAlertDialog
import org.muc.ui.alertdialogs.MoldAlertDialogAction
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldButtonType
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConfirmDialog(
    title: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    type: MoldButtonType = MoldButtonType.PRIMARY,
    content: @Composable () -> Unit,
) {
    MoldAlertDialog(
        onDismissRequest = onDismiss,
        title = title ?: "",
        confirmAction = MoldAlertDialogAction(
            text = stringResource(MoldCommonStringRes.actionConfirm),
            onClick = onConfirm,
            size = MoldButtonSize.MEDIUM,
        ),
        dismissAction = MoldAlertDialogAction(
            text = stringResource(MoldCommonStringRes.actionCancel),
            onClick = onDismiss,
            type = type,
            size = MoldButtonSize.MEDIUM,
        )
    ) {
        content()
    }
}
