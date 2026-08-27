package org.muc.ui.selection

import androidx.compose.runtime.Immutable

/**
 * 通用的列表项多选状态。
 *
 * 用于采用以下交互模式的桌面端界面：
 * - click: single select;
 * - Shift+click: range select;
 * - Ctrl/Cmd+click: toggle item.
 */
@Immutable
data class MoldMultiSelectionState<T>(
    val selectedIds: Set<T> = emptySet(),
    val anchorId: T? = null,
)

/**
 * 仅保留 [visibleIds] 中存在的选中项。
 *
 * 在筛选或更新列表后调用，避免保留指向已不可见行的无效引用。
 */
fun <T> MoldMultiSelectionState<T>.retainVisible(
    visibleIds: Collection<T>,
): MoldMultiSelectionState<T> {
    val visibleSet = visibleIds.toHashSet()
    val normalizedSelection = selectedIds.filterTo(linkedSetOf()) { id -> id in visibleSet }
    val normalizedAnchor = anchorId?.takeIf { id -> id in visibleSet }
    if (normalizedSelection == selectedIds && normalizedAnchor == anchorId) return this
    return copy(selectedIds = normalizedSelection, anchorId = normalizedAnchor)
}

/** 清除选中项和锚点行。 */
fun <T> MoldMultiSelectionState<T>.clearSelection(): MoldMultiSelectionState<T> =
    if (selectedIds.isEmpty() && anchorId == null) this else MoldMultiSelectionState()

/**
 * 选中 [visibleIds] 中的所有行。
 */
fun <T> MoldMultiSelectionState<T>.selectAll(
    visibleIds: List<T>,
): MoldMultiSelectionState<T> {
    if (visibleIds.isEmpty()) return clearSelection()
    return MoldMultiSelectionState(
        selectedIds = visibleIds.toSet(),
        anchorId = visibleIds.first(),
    )
}

/**
 * 应用用户的行选择操作。
 *
 * @param itemId 被点击行的 ID。
 * @param visibleIds 按当前显示顺序排列的行 ID。
 * @param additiveSelection 使用 Ctrl/Cmd+Click 时为 true。
 * @param rangeSelection 使用 Shift+Click 时为 true。
 */
fun <T> MoldMultiSelectionState<T>.onItemSelectionRequested(
    itemId: T,
    visibleIds: List<T>,
    additiveSelection: Boolean,
    rangeSelection: Boolean,
): MoldMultiSelectionState<T> {
    if (visibleIds.isEmpty()) return this

    if (rangeSelection && anchorId != null) {
        val anchorIndex = visibleIds.indexOf(anchorId)
        val targetIndex = visibleIds.indexOf(itemId)
        if (anchorIndex != -1 && targetIndex != -1) {
            val rangeIds = if (anchorIndex <= targetIndex) {
                visibleIds.subList(anchorIndex, targetIndex + 1)
            } else {
                visibleIds.subList(targetIndex, anchorIndex + 1)
            }
            val updatedSelection = if (additiveSelection) {
                selectedIds + rangeIds
            } else {
                rangeIds.toSet()
            }
            return copy(selectedIds = updatedSelection)
        }
    }

    val updatedSelection = when {
        additiveSelection && itemId in selectedIds -> selectedIds - itemId
        additiveSelection -> selectedIds + itemId
        else -> setOf(itemId)
    }
    return copy(selectedIds = updatedSelection, anchorId = itemId)
}
