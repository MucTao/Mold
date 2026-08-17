package org.muc.ui.action

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import org.muc.ui.alertdialogs.MucAlertDialog
import org.muc.ui.alertdialogs.MucAlertDialogAction
import org.muc.ui.banner.MucBanner
import org.muc.ui.banner.MucBannerType
import org.muc.ui.buttons.MucButtonSize
import org.muc.ui.buttons.MucButtonType
import org.muc.ui.buttons.MucFilledButton
import org.muc.ui.buttons.MucOutlinedButton
import org.muc.ui.design.Dimensions
import org.muc.ui.i18n.MucCommonStringRes

sealed class Action(open val msg: @Composable () -> String) {
    data class ActionRequest<T>(
        override val msg: @Composable () -> String,
        val type: MucButtonType,
        val onConfirmAction: ActionManager.(T) -> Unit,
        val default: T? = null,
        val errorMsg: MutableState<String?> = mutableStateOf(null),
        val content: (@Composable (ActionContentScope<T>) -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MucButtonType,
            onConfirmAction: ActionManager.(T) -> Unit,
            default: T? = null,
            errorMsg: MutableState<String?> = mutableStateOf(null),
            content: (@Composable (ActionContentScope<T>) -> Unit)? = null
        ) : this({ msg }, type, onConfirmAction, default, errorMsg, content)
    }

    data class ActionRequestDialog<T>(
        override val msg: @Composable () -> String,
        val type: MucButtonType,
        val onConfirmAction: Pair<String?, ActionManager.(T) -> Unit>,
        val modifier: Modifier = Modifier,
        val default: T? = null,
        val errorMsg: MutableState<String?> = mutableStateOf(null),
        val content: (@Composable (ActionContentScope<T>) -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MucButtonType,
            onConfirmAction: Pair<String?, ActionManager.(T) -> Unit>,
            modifier: Modifier = Modifier,
            default: T? = null,
            errorMsg: MutableState<String?> = mutableStateOf(null),
            content: (@Composable (ActionContentScope<T>) -> Unit)? = null
        ) : this({ msg }, type, onConfirmAction, modifier, default, errorMsg, content)

        constructor(
            msg: String,
            type: MucButtonType,
            onConfirmAction: ActionManager.(T) -> Unit,
            modifier: Modifier = Modifier,
            default: T? = null,
            errorMsg: MutableState<String?> = mutableStateOf(null),
            content: (@Composable (ActionContentScope<T>) -> Unit)? = null
        ) : this({ msg }, type, null to onConfirmAction, modifier, default, errorMsg, content)
    }

    data class ActionRequestTips(
        override val msg: @Composable () -> String,
        val type: MucButtonType,
        val onConfirmAction: (() -> Unit)? = null,
        val content: (@Composable () -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MucButtonType,
            onConfirmAction: (() -> Unit)? = null,
            content: (@Composable () -> Unit)? = null
        ) : this({ msg }, type, onConfirmAction, content)
    }

    data class ActionRequestDialogTips(
        override val msg: @Composable () -> String,
        val type: MucButtonType,
        val modifier: Modifier = Modifier,
        val onConfirmAction: Pair<String?, (() -> Unit)>? = null,
        val content: (@Composable () -> Unit)? = null
    ) : Action(msg) {
        constructor(
            msg: String,
            type: MucButtonType,
            modifier: Modifier = Modifier,
            onConfirmAction: Pair<String?, (() -> Unit)>? = null,
            content: (@Composable () -> Unit)? = null
        ) : this({ msg }, type, modifier, onConfirmAction, content)

        constructor(
            msg: String,
            type: MucButtonType,
            modifier: Modifier = Modifier,
            onConfirmAction: (() -> Unit),
            content: (@Composable () -> Unit)? = null
        ) : this({ msg }, type, modifier, null to onConfirmAction, content)
    }
}

sealed class ActionFeedback(open val msg: @Composable () -> String?, open val type: MucBannerType) {
    data class ActionSuccess(override val msg: @Composable () -> String) : ActionFeedback(msg, MucBannerType.SUCCESS) {
        constructor(msg: String) : this({ msg })
    }

    data class ActionError(override val msg: @Composable () -> String?) : ActionFeedback(msg, MucBannerType.ERROR) {
        constructor(msg: String?) : this({ msg })
    }
}

interface ActionContentScope<T> {
    var data: T?
    var errorMsg: String?
}

data class BaseActionState(
    val isActionRunning: Boolean = false,
    val action: Action? = null,
    val feedback: ActionFeedback? = null
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
            MucAlertDialog(
                onDismissRequest = actionManager::onCancelAction,
                title = action.msg(),
                confirmAction = if (onConfirmAction != null) MucAlertDialogAction(
                    text = stringResource(MucCommonStringRes.actionConfirm),
                    onClick = {
                        actionManager.onConfirmAction()
                        onConfirmAction()
                    },
                    type = action.type,
                    size = MucButtonSize.MEDIUM,
                    loading = state.isActionRunning,
                ) else MucAlertDialogAction(
                    text = stringResource(MucCommonStringRes.actionCancel),
                    onClick = actionManager::onCancelAction,
                    type = action.type,
                    size = MucButtonSize.MEDIUM,
                ),
                dismissAction = if (onConfirmAction != null) MucAlertDialogAction(
                    text = stringResource(MucCommonStringRes.actionCancel),
                    onClick = actionManager::onCancelAction,
                    type = MucButtonType.NEUTRAL,
                    size = MucButtonSize.MEDIUM,
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

    state.feedback?.let { feedback ->
        MucBanner(
            message = when (feedback) {
                is ActionFeedback.ActionError -> {
                    val details = feedback.msg().orEmpty().trim()
                    if (details.isNotEmpty()) {
                        stringResource(MucCommonStringRes.errorWithDetails, details)    // 带详情的错误提示
                    } else {
                        stringResource(MucCommonStringRes.errorUnknown)                 // 未知错误提示
                    }
                }

                is ActionFeedback.ActionSuccess -> feedback.msg()
            },
            type = feedback.type,
            onDismiss = actionManager::onCancelAction,
            alignment = Alignment.BottomEnd,
            modifier = Modifier.fillMaxWidth(0.5f)
                .padding(Dimensions.paddingDefault),
        )
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

    MucAlertDialog(
        onDismissRequest = actionManager::onCancelAction,
        title = action.msg(),
        confirmAction = MucAlertDialogAction(
            text = stringResource(MucCommonStringRes.actionConfirm),
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
            size = MucButtonSize.MEDIUM,
            loading = isActionRunning,
            enabled = contentScope.errorMsg == null
        ),
        dismissAction = MucAlertDialogAction(
            text = stringResource(MucCommonStringRes.actionCancel),
            onClick = actionManager::onCancelAction,
            type = MucButtonType.NEUTRAL,
            size = MucButtonSize.MEDIUM,
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
                        contentDescription = stringResource(MucCommonStringRes.actionClose),
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
                MucOutlinedButton(
                    onClick = actionManager::onCancelAction,
                    text = stringResource(MucCommonStringRes.actionCancel),
                    type = MucButtonType.NEUTRAL,
                    size = MucButtonSize.MEDIUM,

                    )
                val onConfirmAction = action.onConfirmAction
                MucFilledButton(
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
                    text = onConfirmAction.first ?: stringResource(MucCommonStringRes.actionConfirm),
                    type = action.type,
                    size = MucButtonSize.MEDIUM,
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
                        contentDescription = stringResource(MucCommonStringRes.actionClose),
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
                MucOutlinedButton(
                    onClick = actionManager::onCancelAction,
                    text = stringResource(MucCommonStringRes.actionCancel),
                    type = if (onConfirmAction != null) MucButtonType.NEUTRAL else action.type,
                    size = MucButtonSize.MEDIUM,
                )
                if (onConfirmAction != null)
                    MucFilledButton(
                        onClick = onConfirmAction.second,
                        text = onConfirmAction.first ?: stringResource(MucCommonStringRes.actionConfirm),
                        type = action.type,
                        size = MucButtonSize.MEDIUM,
                        loading = isActionRunning,
                    )
            }
        }
    }
}