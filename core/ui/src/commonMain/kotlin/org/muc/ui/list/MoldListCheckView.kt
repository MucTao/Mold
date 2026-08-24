package org.muc.ui.list

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
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
import org.muc.ui.i18n.MoldCommonStringRes
import org.muc.ui.textfields.MoldDropdownOption
import org.muc.ui.textfields.CompactTextField
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun <T> MoldListCheckView(
    sourceList: List<MoldDropdownOption<T>>,
    checkList: List<MoldDropdownOption<T>>,
    onCheckChange: (List<MoldDropdownOption<T>>) -> Unit,
    enabledSort: Boolean = true,
) {
    Row(Modifier.width(600.dp), horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingXSmall)) {
        var filter by remember { mutableStateOf("") }
        val filteredStudents by remember(filter, checkList) {
            derivedStateOf {
                val checkedIds = checkList.mapTo(HashSet()) { it.value }
                sourceList
                    .filterNot { it.value in checkedIds }
                    .filter { it.label.contains(filter, ignoreCase = true) }
            }
        }
        LazyColumn(
            Modifier.weight(1f).height(800.dp).background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            stickyHeader {
                CompactTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    placeholder = stringResource(MoldCommonStringRes.placeholderSearch),
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                        .padding(Dimensions.paddingSmall),
                )
            }
            items(filteredStudents, key = { it.key }, contentType = { "item" }) {
                val modifier = Modifier.clickable { onCheckChange(checkList.toMutableList().apply { add(it) }) }
                ListItem(
                    headlineContent = { Text(it.label) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowRight, "", modifier = modifier) },
                    modifier = modifier
                )
            }
        }
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
            modifier = Modifier.weight(1f).height(800.dp)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
        ) {
            items(checkList, key = { it.key }, contentType = { "item" }) { item ->
                val modifier =
                    Modifier.clickable { onCheckChange(checkList.toMutableList().apply { remove(item) }) }
                ReorderableItem(reorderableLazyListState, key = item.key, enabled = enabledSort) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                    ListItem(
                        headlineContent = { Text(item.label) },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.ArrowLeft, "", modifier = modifier) },
                        trailingContent = {
                            if (enabledSort)
                                Icon(
                                    Icons.AutoMirrored.Outlined.CompareArrows,
                                    "",
                                    modifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        },
                                    ).rotate(90f)
                                )
                        },
                        modifier = modifier.shadow(elevation)
                    )
                }
            }
        }
    }
}