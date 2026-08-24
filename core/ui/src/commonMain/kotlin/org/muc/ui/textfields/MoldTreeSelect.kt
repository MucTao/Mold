package org.muc.ui.textfields

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 专为 UI 渲染设计的平铺轻量结构
 */
data class FlatTreeItem<T>(
    val option: MoldTreeDropdownOption<T>,
    val level: Int
)

/**
 * 根据外部的展开状态，动态平铺原始树结构，并计算绝对层级
 */
fun <T> flattenTreeWithState(
    options: List<MoldTreeDropdownOption<T>>,
    expandedIds: Set<T>, // 外部传入的已展开节点的 Value 集合
    currentLevel: Int = 0
): List<FlatTreeItem<T>> {
    val result = mutableListOf<FlatTreeItem<T>>()
    for (option in options) {
        // 1. 计入当前节点，层级由上层递归传入
        result.add(FlatTreeItem(option, currentLevel))

        // 2. 如果不是叶子节点，且外部状态表明它已展开，则递归平铺子节点
        if (!option.isLeaf && expandedIds.contains(option.value) && option.children.isNotEmpty()) {
            result.addAll(flattenTreeWithState(option.children, expandedIds, currentLevel + 1))
        }
    }
    return result
}

/**
 * 核心逻辑：根据外部勾选的 Value 集合，动态计算任意一个节点应该呈现的 Checkbox 状态
 */
fun <T> calculateCheckState(
    node: MoldTreeDropdownOption<T>,
    checkedIds: Set<T>
): ToggleableState {
    // 1. 如果是叶子节点，状态非黑即白
    if (node.isLeaf) {
        return if (checkedIds.contains(node.value)) ToggleableState.On else ToggleableState.Off
    }

    // 2. 如果是父节点，递归检查它所有后代叶子节点的勾选情况
    val leafChildren = mutableListOf<MoldTreeDropdownOption<T>>()
    fun collectLeafs(n: MoldTreeDropdownOption<T>) {
        if (n.isLeaf) leafChildren.add(n) else n.children.forEach { collectLeafs(it) }
    }
    collectLeafs(node)

    // 3. 计算后代叶子节点在外部集合中的命中率
    val checkedCount = leafChildren.count { checkedIds.contains(it.value) }

    return when {
        checkedCount == 0 -> ToggleableState.Off
        checkedCount == leafChildren.size -> ToggleableState.On
        else -> ToggleableState.Indeterminate
    }
}

/**
 * 辅助逻辑：当点击某个节点时，获取它旗下所有叶子节点的 Value（用于级联勾选或取消勾选）
 */
fun <T> getAllLeafValues(node: MoldTreeDropdownOption<T>): List<T> {
    val leafs = mutableListOf<T>()
    fun walk(n: MoldTreeDropdownOption<T>) {
        if (n.isLeaf) leafs.add(n.value) else n.children.forEach { walk(it) }
    }
    walk(node)
    return leafs
}


@Composable
fun <T> MoldStatelessTreeSelect(
    suggestions: List<MoldTreeDropdownOption<T>>, // 原始只读树
    expandedIds: Set<T>,                         // 外部控制的展开集合
    checkedIds: Set<T>,                          // 外部控制的选中集合（通常存叶子节点的value）
    onExpandedIdsChanged: (Set<T>) -> Unit,      // 展开状态回调
    onCheckedIdsChanged: (Set<T>) -> Unit,        // 选中状态回调
    modifier: Modifier = Modifier,
    treeIndent: Dp = 24.dp
) {
    val scrollState = rememberScrollState()

    // 动态平铺当前可见的节点列表
    val visibleItems = remember(suggestions, expandedIds) {
        flattenTreeWithState(suggestions, expandedIds)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(vertical = 8.dp)
    ) {
        visibleItems.forEach { item ->
            val option = item.option

            // 实时计算当前节点在复选框中应当显示的 3 种状态之一
            val currentCheckState = remember(option, checkedIds) {
                calculateCheckState(option, checkedIds)
            }

            val isExpanded = expandedIds.contains(option.value)
            val arrowRotateDegrees by animateFloatAsState(
                targetValue = if (isExpanded) 0f else -90f,
                label = "ArrowRotate"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!option.isLeaf) {
                            // 点击父节点整行：切换展开/折叠
                            val nextExpanded = expandedIds.toMutableSet()
                            if (isExpanded) nextExpanded.remove(option.value) else nextExpanded.add(option.value)
                            onExpandedIdsChanged(nextExpanded)
                        } else {
                            // 点击叶子节点整行：切换勾选
                            val nextChecked = checkedIds.toMutableSet()
                            if (checkedIds.contains(option.value)) nextChecked.remove(option.value) else nextChecked.add(
                                option.value
                            )
                            onCheckedIdsChanged(nextChecked)
                        }
                    }
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 动态缩进
                Spacer(modifier = Modifier.width(treeIndent * item.level))

                // 2. 折叠/展开箭头
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    if (!option.isLeaf) {
                        IconButton(
                            onClick = {
                                val nextExpanded = expandedIds.toMutableSet()
                                if (isExpanded) nextExpanded.remove(option.value) else nextExpanded.add(option.value)
                                onExpandedIdsChanged(nextExpanded)
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(arrowRotateDegrees)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 3. 三态复选框
                TriStateCheckbox(
                    state = currentCheckState,
                    onClick = {
                        val nextChecked = checkedIds.toMutableSet()
                        val relatedLeafs = getAllLeafValues(option)

                        if (currentCheckState == ToggleableState.On) {
                            // 如果本来是全选，点击则变为【取消全选】（移除该节点下所有叶子）
                            nextChecked.removeAll(relatedLeafs)
                        } else {
                            // 如果本来是未选或半选，点击则变为【全选】（加入该节点下所有叶子）
                            nextChecked.addAll(relatedLeafs)
                        }
                        onCheckedIdsChanged(nextChecked)
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 4. 文本
                Text(text = option.label)
            }
        }
    }
}