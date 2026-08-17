package org.muc.ui.textfields

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.muc.ui.design.Dimensions
import org.muc.ui.i18n.MucCommonStringRes
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

// 临时携带层级信息的包装类
private data class FlatTreeOption<T>(
    val option: MucTreeDropdownOption<T>,
    val level: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> MucTreeCheckView(
    sourceList: List<MucTreeDropdownOption<T>>,
    checkList: List<MucTreeDropdownOption<T>>,
    onCheckChange: (List<MucTreeDropdownOption<T>>) -> Unit,
) {
    Row(Modifier.width(600.dp), horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXSmall)) {
        var filter by remember { mutableStateOf("") }

        // 记录展开的父节点 value
        var expandedNodes by remember { mutableStateOf(setOf<String>()) }

        // 1. 左侧树数据处理：根据搜索、已选、展开状态，将树展平成 List
        val filteredTreeItems by remember(sourceList, checkList, filter, expandedNodes) {
            derivedStateOf {
                val checkedValues = checkList.mapTo(HashSet()) { it.id }
                val result = mutableListOf<FlatTreeOption<T>>()

                fun traverse(nodes: List<MucTreeDropdownOption<T>>, level: Int) {
                    for (node in nodes) {
                        // 如果当前节点已经是叶子且被选中，或者是非叶子但其下所有子孙都被选中了，就从左侧隐藏
                        if (node.isLeaf && node.id in checkedValues) continue

                        // 检查非叶子节点下的所有子孙是否全部已被右侧选中
                        if (!node.isLeaf && isAllChildrenChecked(node.children, checkedValues)) continue

                        val matchesFilter = node.label.contains(filter, ignoreCase = true)

                        if (!node.isLeaf) {
                            val hasMatchingChild = hasVisibleChild(node.children, checkedValues, filter)
                            if (filter.isEmpty() || matchesFilter || hasMatchingChild) {
                                result.add(FlatTreeOption(node, level))
                                if (expandedNodes.contains(node.key) || filter.isNotEmpty()) {
                                    traverse(node.children, level + 1)
                                }
                            }
                        } else {
                            if (filter.isEmpty() || matchesFilter) {
                                result.add(FlatTreeOption(node, level))
                            }
                        }
                    }
                }

                traverse(sourceList, 0)
                result
            }
        }

        // --- 左侧：源数据树形展示栏 ---
        LazyColumn(
            Modifier
                .weight(1f)
                .height(800.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            stickyHeader {
                CompactTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    placeholder = stringResource(MucCommonStringRes.placeholderSearch),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(Dimensions.paddingSmall),
                )
            }

            items(
                items = filteredTreeItems,
                key = { it.option.key },
                contentType = { "tree_item" }
            ) { flatItem ->
                val node = flatItem.option
                val isExpanded = expandedNodes.contains(node.key)

                val itemModifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = node.enabled) {
                        if (node.isLeaf) {
                            // 1. 叶子节点：直接单选穿梭到右边
                            onCheckChange(checkList.toMutableList().apply { add(node) })
                        } else {
                            // 2. 父节点：由于你想支持点击时“展开/折叠”，为了兼容“选中所有子节点”
                            // 我们把“点击整行”保留为展开/折叠，而在右侧的 Trailing 图标处提供“全选穿梭”功能
                            // 或者如果你希望点击整行就是选中所有，可根据业务调整。这里采用点击整行切换展开，点击右侧图标穿梭
                            expandedNodes = if (isExpanded) {
                                expandedNodes - node.key
                            } else {
                                expandedNodes + node.key
                            }
                        }
                    }
                    .padding(start = (flatItem.level * 16).dp)

                ListItem(
                    modifier = itemModifier,
                    headlineContent = {
                        Text(
                            text = node.label,
                            color = if (node.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    },
                    leadingContent = {
                        if (!node.isLeaf) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Toggle"
                            )
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    },
                    trailingContent = {
                        // 无论叶子还是父节点，右侧都提供穿梭按钮
                        IconButton(
                            onClick = {
                                val checkedValues = checkList.mapTo(HashSet()) { it.id }
                                val leavesToAdd = mutableListOf<MucTreeDropdownOption<T>>()

                                // 提取该节点下所有未被选中的叶子节点
                                getAllUncheckedLeaves(node, checkedValues, leavesToAdd)

                                if (leavesToAdd.isNotEmpty()) {
                                    onCheckChange(checkList.toMutableList().apply { addAll(leavesToAdd) })
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "Select All")
                        }
                    }
                )
            }
        }

        // --- 右侧：已选数据拖拽排序栏 ---
        val hapticFeedback = LocalHapticFeedback.current
        val lazyListState = rememberLazyListState()
        val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            val newItems = checkList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
            onCheckChange(newItems)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .height(800.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            items(
                items = checkList,
                key = { it.key },
                contentType = { "selected_item" }
            ) { item ->
                ReorderableItem(reorderableLazyListState, key = item.key) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                    ListItem(
                        modifier = Modifier
                            .shadow(elevation)
                            .background(MaterialTheme.colorScheme.surface),
                        headlineContent = { Text(item.label) },
                        leadingContent = {
                            IconButton(onClick = { onCheckChange(checkList.toMutableList().apply { remove(item) }) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowLeft, "Remove")
                            }
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.CompareArrows,
                                contentDescription = "Drag Handle",
                                modifier = Modifier
                                    .draggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        },
                                    )
                                    .rotate(90f)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 递归获取当前节点下所有未被选中的叶子节点
 */
private fun <T> getAllUncheckedLeaves(
    node: MucTreeDropdownOption<T>,
    checkedValues: Set<String>,
    result: MutableList<MucTreeDropdownOption<T>>
) {
    if (node.isLeaf) {
        if (node.key !in checkedValues) {
            result.add(node)
        }
    } else {
        for (child in node.children) {
            getAllUncheckedLeaves(child, checkedValues, result)
        }
    }
}

/**
 * 递归判断某个父节点下的所有叶子节点是否都已经全部被选中了
 */
private fun <T> isAllChildrenChecked(
    children: List<MucTreeDropdownOption<T>>,
    checkedValues: Set<String>
): Boolean {
    for (child in children) {
        if (child.isLeaf) {
            if (child.id !in checkedValues) return false
        } else {
            if (!isAllChildrenChecked(child.children, checkedValues)) return false
        }
    }
    return true
}

/**
 * 辅助函数：递归判断某个父节点的子孙节点中，是否有未被选中且匹配搜索关键词的节点
 */
private fun <T> hasVisibleChild(
    children: List<MucTreeDropdownOption<T>>,
    checkedValues: Set<String>,
    filter: String
): Boolean {
    for (child in children) {
        if (child.isLeaf && child.id in checkedValues) continue
        if (child.label.contains(filter, ignoreCase = true)) return true
        if (!child.isLeaf && hasVisibleChild(child.children, checkedValues, filter)) return true
    }
    return false
}