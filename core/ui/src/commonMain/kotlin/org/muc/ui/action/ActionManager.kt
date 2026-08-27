package org.muc.ui.action

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface ActionManager {
    val actionState: StateFlow<BaseActionState>
    val isActionRunning: Boolean
    fun onRequestAction(action: Action)
    fun onConfirmAction()
    fun onCancelAction(closeAction: Boolean = true)
}


/**
 * 封装通用的 Action 逻辑处理
 * actionState 传入 ViewModel 的 StateFlow，用于更新状态
 */
class ActionManagerImpl : ActionManager {
    override val actionState: StateFlow<BaseActionState> field = MutableStateFlow(BaseActionState())
    override val isActionRunning: Boolean
        get() = actionState.value.isActionRunning

    override fun onRequestAction(action: Action) {
        actionState.update {
            BaseActionState(action = action)
        }
    }

    override fun onConfirmAction() {
        actionState.update {
            it.copy(isActionRunning = true)
        }
    }

    override fun onCancelAction(closeAction: Boolean) {
        actionState.update { if (closeAction) BaseActionState() else it.copy(isActionRunning = false) }
    }
}