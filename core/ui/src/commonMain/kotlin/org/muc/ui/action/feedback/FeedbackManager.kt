package org.muc.ui.action.feedback

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class FeedBackType { PRIMARY, INFO, SUCCESS, ERROR, WARNING }

// ========== Toast（轻量通知）==========
data class ToastData(
    val message: String,
    val type: FeedBackType = FeedBackType.INFO,
    val duration: Duration = 2.seconds
)

// ========== Snackbar（可交互反馈）==========
data class SnackbarData(
    val message: String,
    val type: FeedBackType = FeedBackType.INFO,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val duration: Duration? = null // null = 不自动消失，等用户操作
)

// ========== 统一状态持有者 ==========
object FeedbackManager {
    // Toast 队列
    val toasts: List<ToastData>
        field = mutableStateListOf<ToastData>()

    // Snackbar 队列（通常只显示一个，新的替换旧的）
    private val _snackbar = mutableStateOf<SnackbarData?>(null)
    val snackbar: SnackbarData? get() = _snackbar.value

    fun showToast(message: String, type: FeedBackType = FeedBackType.INFO, duration: Duration = 2.seconds) {
        if (toasts.size >= 3) toasts.removeFirst()
        toasts.add(ToastData(message, type, duration))
    }

    internal suspend fun removeToastAfterDelay(toast: ToastData) {
        delay(toast.duration)
        toasts.remove(toast)
    }

    fun showSnackbar(
        message: String,
        type: FeedBackType = FeedBackType.INFO,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        duration: Duration? = null
    ) {
        _snackbar.value = SnackbarData(message, type, actionLabel, onAction, duration)
    }

    internal suspend fun removeSnackBarAfterDelay(snackBar: SnackbarData) {
        if (snackBar.duration == null) return
        delay(snackBar.duration)
        dismissSnackbar()
    }

    fun dismissSnackbar() {
        _snackbar.value = null
    }
}