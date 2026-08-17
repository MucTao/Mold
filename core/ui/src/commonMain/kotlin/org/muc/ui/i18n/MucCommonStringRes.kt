package org.muc.ui.i18n

import mold.core.ui.generated.resources.common_action_approve
import mold.core.ui.generated.resources.common_action_cancel
import mold.core.ui.generated.resources.common_action_clear
import mold.core.ui.generated.resources.common_action_clear_selection
import mold.core.ui.generated.resources.common_action_close
import mold.core.ui.generated.resources.common_action_close_settings
import mold.core.ui.generated.resources.common_action_confirm
import mold.core.ui.generated.resources.common_action_copy_line
import mold.core.ui.generated.resources.common_action_copy_selected
import mold.core.ui.generated.resources.common_action_delete
import mold.core.ui.generated.resources.common_action_publist
import mold.core.ui.generated.resources.common_action_refresh
import mold.core.ui.generated.resources.common_action_reject
import mold.core.ui.generated.resources.common_action_revoke
import mold.core.ui.generated.resources.common_action_settings
import mold.core.ui.generated.resources.common_action_start
import mold.core.ui.generated.resources.common_action_stop
import mold.core.ui.generated.resources.common_delete_submit
import mold.core.ui.generated.resources.common_delete_submit_warning
import mold.core.ui.generated.resources.common_error_generic
import mold.core.ui.generated.resources.common_error_unknown
import mold.core.ui.generated.resources.common_error_with_details
import mold.core.ui.generated.resources.common_placeholder_search
import mold.core.ui.generated.resources.Res
import org.jetbrains.compose.resources.StringResource

/**
 * 应用的通用字符串资源。
 *
 * 用于统一 feature 模块之间的重复标签。
 */
object MucCommonStringRes {

    /** 启动进程的操作。 */
    val actionStart: StringResource
        get() = Res.string.common_action_start

    /** 停止进程的操作。 */
    val actionStop: StringResource
        get() = Res.string.common_action_stop

    /** 清除数据/状态的操作。 */
    val actionClear: StringResource
        get() = Res.string.common_action_clear

    /** 确认操作。 */
    val actionConfirm: StringResource
        get() = Res.string.common_action_confirm

    /** 取消操作。 */
    val actionCancel: StringResource
        get() = Res.string.common_action_cancel

    /** 刷新数据的操作。 */
    val actionRefresh: StringResource
        get() = Res.string.common_action_refresh

    val actionDelete: StringResource
        get() = Res.string.common_action_delete

    val actionPublish: StringResource
        get() = Res.string.common_action_publist

    val actionRevoke: StringResource
        get() = Res.string.common_action_revoke

    val actionApprove: StringResource
        get() = Res.string.common_action_approve

    val actionReject: StringResource
        get() = Res.string.common_action_reject

    /** 通用关闭操作。 */
    val actionClose: StringResource
        get() = Res.string.common_action_close

    /** 取消选择的操作。 */
    val actionClearSelection: StringResource
        get() = Res.string.common_action_clear_selection

    /** 复制单行/记录的操作。 */
    val actionCopyLine: StringResource
        get() = Res.string.common_action_copy_line

    /** 复制选中元素的操作。 */
    val actionCopySelected: StringResource
        get() = Res.string.common_action_copy_selected

    /** 打开设置的按钮标签。 */
    val actionSettings: StringResource
        get() = Res.string.common_action_settings

    /** 关闭设置面板的按钮标签。 */
    val actionCloseSettings: StringResource
        get() = Res.string.common_action_close_settings

    /** 搜索框的通用占位符。 */
    val placeholderSearch: StringResource
        get() = Res.string.common_placeholder_search

    /** 无详细信息的通用错误标签。 */
    val errorGeneric: StringResource
        get() = Res.string.common_error_generic

    /** 通用未知错误文本。 */
    val errorUnknown: StringResource
        get() = Res.string.common_error_unknown

    /** 带详细信息的错误模板。 */
    val errorWithDetails: StringResource
        get() = Res.string.common_error_with_details

    val deleteSubmit: StringResource
        get() = Res.string.common_delete_submit

    val deleteSubmitWarning: StringResource
        get() = Res.string.common_delete_submit_warning
}
