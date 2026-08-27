package org.muc.ui.action

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.stringResource
import org.muc.ui.alertdialogs.MoldAlertDialog
import org.muc.ui.alertdialogs.MoldAlertDialogAction
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldButtonType
import org.muc.ui.buttons.MoldFilledButton
import org.muc.ui.buttons.MoldOutlinedButton
import org.muc.ui.design.Dimensions
import org.muc.ui.i18n.MoldCommonStringRes

sealed class Action(open val msg: @Composable () -> String) {
    data class ActionRequest<T>(
        override val msg: @Composable () -> String,
        val type: MoldButtonType,
        val onConfirmAction: ActionManager.(T) -> Unit,
        val modifier: Modifier = Modifier,
        val default: T? = null,
        val errorMsg: MutableState<String?> = mutableStateOf(null),
        val content: (@Composable (ActionContentScope<T>) -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MoldButtonType,
            onConfirmAction: ActionManager.(T) -> Unit,
            modifier: Modifier = Modifier,
            default: T? = null,
            errorMsg: MutableState<String?> = mutableStateOf(null),
            content: (@Composable (ActionContentScope<T>) -> Unit)? = null
        ) : this({ msg }, type, onConfirmAction, modifier, default, errorMsg, content)
    }

    data class ActionRequestDialog<T>(
        override val msg: @Composable () -> String,
        val type: MoldButtonType,
        val onConfirmAction: Pair<String?, ActionManager.(T) -> Unit>,
        val modifier: Modifier = Modifier,
        val default: T? = null,
        val errorMsg: MutableState<String?> = mutableStateOf(null),
        val content: (@Composable (ActionContentScope<T>) -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MoldButtonType,
            onConfirmAction: Pair<String?, ActionManager.(T) -> Unit>,
            modifier: Modifier = Modifier,
            default: T? = null,
            errorMsg: MutableState<String?> = mutableStateOf(null),
            content: (@Composable (ActionContentScope<T>) -> Unit)? = null
        ) : this({ msg }, type, onConfirmAction, modifier, default, errorMsg, content)

        constructor(
            msg: String,
            type: MoldButtonType,
            onConfirmAction: ActionManager.(T) -> Unit,
            modifier: Modifier = Modifier,
            default: T? = null,
            errorMsg: MutableState<String?> = mutableStateOf(null),
            content: (@Composable (ActionContentScope<T>) -> Unit)? = null
        ) : this({ msg }, type, null to onConfirmAction, modifier, default, errorMsg, content)
    }

    data class ActionRequestTips(
        override val msg: @Composable () -> String,
        val type: MoldButtonType,
        val onConfirmAction: (() -> Unit)? = null,
        val content: (@Composable () -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MoldButtonType,
            onConfirmAction: (() -> Unit)? = null,
            content: (@Composable () -> Unit)? = null
        ) : this({ msg }, type, onConfirmAction, content)
    }

    data class ActionRequestDialogTips(
        override val msg: @Composable () -> String,
        val type: MoldButtonType,
        val modifier: Modifier = Modifier,
        val onConfirmAction: Pair<String?, (() -> Unit)>? = null,
        val content: (@Composable () -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MoldButtonType,
            modifier: Modifier = Modifier,
            onConfirmAction: Pair<String?, (() -> Unit)>? = null,
            content: (@Composable () -> Unit)? = null
        ) : this({ msg }, type, modifier, onConfirmAction, content)

        constructor(
            msg: String,
            type: MoldButtonType,
            modifier: Modifier = Modifier,
            onConfirmAction: (() -> Unit),
            content: (@Composable () -> Unit)? = null
        ) : this({ msg }, type, modifier, null to onConfirmAction, content)
    }
}

interface ActionContentScope<T> {
    var data: T?
    var errorMsg: String?
}

data class BaseActionState(
    val isActionRunning: Boolean = false,
    val action: Action? = null,
)

@Composable
fun ActionView(actionManager: ActionManager) {
    val state by actionManager.actionState.collectAsState()
    when (val action = state.action) {
        is Action.ActionRequest<*> ->
            ActionRequestAlertDialog(
                action = action,
                isActionRunning = state.isActionRunning,
                actionManager = actionManager
            )

        is Action.ActionRequestDialog<*> ->
            ActionRequestDialog(
                action = action,
                isActionRunning = state.isActionRunning,
                actionManager = actionManager
            )


        is Action.ActionRequestTips -> {
            val onConfirmAction = action.onConfirmAction
            MoldAlertDialog(
                onDismissRequest = actionManager::onCancelAction,
                title = action.msg(),
                confirmAction = if (onConfirmAction != null) MoldAlertDialogAction(
                    text = stringResource(MoldCommonStringRes.actionConfirm),
                    onClick = {
                        actionManager.onConfirmAction()
                        onConfirmAction()
                    },
                    type = action.type,
                    size = MoldButtonSize.MEDIUM,
                    loading = state.isActionRunning,
                ) else MoldAlertDialogAction(
                    text = stringResource(MoldCommonStringRes.actionCancel),
                    onClick = actionManager::onCancelAction,
                    type = action.type,
                    size = MoldButtonSize.MEDIUM,
                ),
                dismissAction = if (onConfirmAction != null) MoldAlertDialogAction(
                    text = stringResource(MoldCommonStringRes.actionCancel),
                    onClick = actionManager::onCancelAction,
                    type = MoldButtonType.PRIMARY,
                    size = MoldButtonSize.MEDIUM,
                ) else null,
            ) {
                action.content?.invoke()
            }
        }

        is Action.ActionRequestDialogTips ->
            ActionRequestDialogTips(
                action,
                isActionRunning = state.isActionRunning,
                actionManager = actionManager
            )

        null -> {}
    }
}

@Composable
private fun <E> ActionRequestAlertDialog(
    action: Action.ActionRequest<E>,
    isActionRunning: Boolean,
    actionManager: ActionManager
) {
    // 1. 创建一个随弹窗生命周期生灭的局部状态宿主
    val contentScope = remember(action) {
        object : ActionContentScope<E> {
            override var data: E? by mutableStateOf(action.default)
            override var errorMsg: String? by mutableStateOf(null)
        }
    }

    MoldAlertDialog(
        modifier = action.modifier,
        onDismissRequest = actionManager::onCancelAction,
        title = action.msg(),
        confirmAction = MoldAlertDialogAction(
            text = stringResource(MoldCommonStringRes.actionConfirm),
            onClick = {
                if (contentScope.errorMsg == null) {
                    val finalData = contentScope.data
                    if (finalData != null) {
                        with(action) {
                            actionManager.onConfirmAction(finalData)
                        }
                    } else {
                        contentScope.errorMsg = "输入内容不能为空" // 兜底校验
                    }
                }
            },
            type = action.type,
            size = MoldButtonSize.MEDIUM,
            loading = isActionRunning,
            enabled = contentScope.errorMsg == null
        ),
        dismissAction = MoldAlertDialogAction(
            text = stringResource(MoldCommonStringRes.actionCancel),
            onClick = actionManager::onCancelAction,
            size = MoldButtonSize.MEDIUM,
        ),
    ) {
        contentScope.errorMsg?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = Dimensions.paddingSmall)
            )
        }
        action.content?.invoke(contentScope)
    }
}

@Composable
private fun <E> ActionRequestDialog(
    action: Action.ActionRequestDialog<E>,
    isActionRunning: Boolean,
    actionManager: ActionManager
) {
    // 1. 创建一个随弹窗生命周期生灭的局部状态宿主
    val contentScope = remember(action) {
        object : ActionContentScope<E> {
            override var data: E? by mutableStateOf(action.default)
            override var errorMsg: String? by mutableStateOf(null)
        }
    }
    Dialog(
        onDismissRequest = actionManager::onCancelAction,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Column(
            action.modifier.background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXSmall)
        ) {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = Dimensions.paddingDefault, vertical = Dimensions.paddingXSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = action.msg(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimensions.paddingXSmall),
                )
                IconButton(
                    onClick = actionManager::onCancelAction,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(MoldCommonStringRes.actionClose),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                action.content?.invoke(contentScope)
            }
            Row(
                Modifier.padding(Dimensions.paddingMedium).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                contentScope.errorMsg?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            start = Dimensions.paddingDefault,
                            bottom = Dimensions.paddingMedium
                        )
                    )
                }
                MoldOutlinedButton(
                    onClick = actionManager::onCancelAction,
                    text = stringResource(MoldCommonStringRes.actionCancel),
                    size = MoldButtonSize.MEDIUM,
                )
                val onConfirmAction = action.onConfirmAction
                MoldFilledButton(
                    onClick = {
                        if (contentScope.errorMsg == null) {
                            val finalData = contentScope.data
                            if (finalData != null) {
                                onConfirmAction.second(actionManager, finalData)
                            } else {
                                contentScope.errorMsg = "输入内容不能为空"
                            }
                        }
                    },
                    text = onConfirmAction.first ?: stringResource(MoldCommonStringRes.actionConfirm),
                    type = action.type,
                    size = MoldButtonSize.MEDIUM,
                    loading = isActionRunning,
                    enabled = contentScope.errorMsg == null
                )
            }
        }
    }
}


@Composable
private fun ActionRequestDialogTips(
    action: Action.ActionRequestDialogTips,
    isActionRunning: Boolean,
    actionManager: ActionManager
) {
    Dialog(
        onDismissRequest = actionManager::onCancelAction,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Column(
            action.modifier.background(MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXSmall)
        ) {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = Dimensions.paddingDefault, vertical = Dimensions.paddingXSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = action.msg(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimensions.paddingXSmall),
                )
                IconButton(
                    onClick = actionManager::onCancelAction,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(MoldCommonStringRes.actionClose),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                action.content?.invoke()
            }
            Row(
                Modifier.padding(Dimensions.paddingMedium).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val onConfirmAction = action.onConfirmAction
                MoldOutlinedButton(
                    onClick = actionManager::onCancelAction,
                    text = stringResource(MoldCommonStringRes.actionCancel),
                    type = if (onConfirmAction != null) MoldButtonType.PRIMARY else action.type,
                    size = MoldButtonSize.MEDIUM,
                )
                if (onConfirmAction != null)
                    MoldFilledButton(
                        onClick = onConfirmAction.second,
                        text = onConfirmAction.first ?: stringResource(MoldCommonStringRes.actionConfirm),
                        type = action.type,
                        size = MoldButtonSize.MEDIUM,
                        loading = isActionRunning,
                    )
            }
        }
    }
}