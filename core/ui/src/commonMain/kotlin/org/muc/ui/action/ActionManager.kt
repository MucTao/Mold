package org.muc.ui.action

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

interface ActionManager {
    val actionState: StateFlow<BaseActionState>
    val isActionRunning: Boolean
    fun onRequestAction(action: Action)
    fun onRequestFeedBack(scope: CoroutineScope, feedback: ActionFeedback, closeAction: Boolean = true)
    fun onConfirmAction()
    fun onCancelAction(closeAction: Boolean = true)
}


/**
 * 封装通用的 Action 逻辑处理
 * @param stateFlow 传入 ViewModel 的 MutableStateFlow，用于更新状态
 */
class ActionManagerImpl : ActionManager {

    private val _actionState: MutableStateFlow<BaseActionState> = MutableStateFlow(BaseActionState())
    override val actionState: StateFlow<BaseActionState> = _actionState.asStateFlow()
    override val isActionRunning: Boolean
        get() = actionState.value.isActionRunning

    override fun onRequestAction(action: Action) {
        feedBackJob?.cancel()
        _actionState.update {
            BaseActionState(action = action)
        }
    }

    private var feedBackJob: Job? = null
    override fun onRequestFeedBack(scope: CoroutineScope, feedback: ActionFeedback, closeAction: Boolean) {
        feedBackJob?.cancel()
        _actionState.update {
            if (closeAction) BaseActionState(feedback = feedback)
            else it.copy(feedback = feedback, isActionRunning = false)
        }
        feedBackJob = scope.launch {
            delay(3.seconds)
            _actionState.update {
                if (closeAction) BaseActionState()
                else it.copy(feedback = null, isActionRunning = false)
            }
        }
    }

    override fun onConfirmAction() {
        feedBackJob?.cancel()
        _actionState.update {
            it.copy(isActionRunning = true)
        }
    }

    override fun onCancelAction(closeAction: Boolean) {
        feedBackJob?.cancel()
        _actionState.update { if (closeAction) BaseActionState() else it.copy(isActionRunning = false) }
    }
}