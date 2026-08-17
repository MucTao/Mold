package org.muc.ui.alertdialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import org.muc.ui.buttons.MucFilledButton
import org.muc.ui.buttons.MucOutlinedButton

/**
 * 用于确认与表单操作的通用警告对话框。
 *
 * 该组件封装了统一的标题/按钮样式，并支持通过插槽 [content] 传入自定义内容。
 *
 * @param onDismissRequest 关闭对话框的回调请求
 * @param title 对话框标题
 * @param modifier 对话框的修饰符
 * @param titleIcon 标题旁的可选图标
 * @param titleIconTint 标题图标的颜色
 * @param confirmAction 确认按钮配置（填充样式）
 * @param dismissAction 取消按钮配置（描边样式），为 `null` 时不显示取消按钮
 * @param hideDismissWhenBusy 业务执行中隐藏取消按钮
 * @param allowDismissWhenBusy 业务执行中允许通过非按钮区域关闭对话框
 * @param contentSpacing 内容元素之间的垂直间距
 * @param content 对话框内容（文本/输入框/其他自定义元素）
 */
@Composable
fun MucAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    titleIcon: ImageVector? = null,
    titleIconTint: Color = MaterialTheme.colorScheme.primary,
    confirmAction: MucAlertDialogAction,
    dismissAction: MucAlertDialogAction? = null,
    hideDismissWhenBusy: Boolean = true,
    allowDismissWhenBusy: Boolean = false,
    contentSpacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isBusy = confirmAction.loading || (dismissAction?.loading == true)
    val canDismiss = allowDismissWhenBusy || !isBusy

    AlertDialog(
        onDismissRequest = {
            if (canDismiss) {
                onDismissRequest()
            }
        },
        modifier = modifier,
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss,
        ),
        title = {
            if (titleIcon == null) {
                Text(text = title)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        tint = titleIconTint,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = title)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
                content = content,
            )
        },
        confirmButton = {
            MucFilledButton(
                onClick = confirmAction.onClick,
                text = confirmAction.text,
                enabled = confirmAction.enabled,
                loading = confirmAction.loading,
                type = confirmAction.type,
                size = confirmAction.size,
                cornerRadius = confirmAction.cornerRadius,
            )
        },
        dismissButton = {
            val shouldShowDismiss = dismissAction != null && (!hideDismissWhenBusy || !isBusy)
            if (shouldShowDismiss) {
                dismissAction.let { action ->
                    MucOutlinedButton(
                        onClick = action.onClick,
                        text = action.text,
                        enabled = action.enabled,
                        loading = action.loading,
                        type = action.type,
                        size = action.size,
                        cornerRadius = action.cornerRadius,
                    )
                }
            }
        },
    )
}