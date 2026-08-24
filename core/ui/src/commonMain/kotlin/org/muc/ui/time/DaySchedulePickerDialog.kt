package org.muc.ui.time

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adbdeck.core.ui.time.TimeRange
import com.adbdeck.core.ui.time.TimeRangePickerDialog
import com.adbdeck.core.ui.time.isShow
import com.adbdeck.core.ui.time.show
import kotlinx.datetime.LocalTime
import org.muc.ui.buttons.MoldButtonSize
import org.muc.ui.buttons.MoldButtonType
import org.muc.ui.buttons.MoldFilledButton
import org.muc.ui.buttons.MoldOutlinedButton
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MoldCornerRadius
import org.muc.ui.design.MoldGreen
import org.muc.ui.textfields.MoldOutlinedTextField
import org.muc.ui.textfields.MoldTextFieldSize
import org.muc.ui.textfields.MoldTextFieldType

data class DaySchedule(
    val id: String? = null,
    val name: String? = null,
    val color: Color = MoldGreen,
    val time: List<TimeRange> = emptyList(),
)

internal typealias DayScheduleDialogState = MutableState<DaySchedule?>


@Composable
internal fun rememberDaySchedulePickerDialogState() = remember { mutableStateOf<DaySchedule?>(null) }

fun DayScheduleDialogState.show(schedule: DaySchedule) {
    this.value = schedule
}

internal fun DayScheduleDialogState.hide() {
    this.value = null
}

@Composable
fun DaySchedulePickerDialog(
    state: DayScheduleDialogState = rememberDaySchedulePickerDialogState(),
    onValue: (DaySchedule) -> Unit,
    title: String = "",
    tips: String = "",
    activeLabel: String = "",
    inactiveLabel: String = "",
    modifier: Modifier = Modifier
): DayScheduleDialogState = state.value?.let { value ->
    Dialog(
        onDismissRequest = state::hide,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Column(modifier.background(MaterialTheme.colorScheme.surface)) {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = Dimensions.paddingDefault, vertical = Dimensions.paddingXSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${if (value.id == null) "新建" else "编辑"}$title",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Dimensions.paddingXSmall),
                )
                IconButton(
                    onClick = state::hide,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "close",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            var schedule by remember { mutableStateOf(value) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            Row(Modifier.fillMaxWidth().weight(1f)) {
                val pickDialog = TimeRangePickerDialog(
                    timeList = schedule.time,
                    onConfirm = { res, index ->
                        val newPlan = if (index == -1) {//新增
                            schedule.copy(time = schedule.time.plus(res).sortedBy { it.startTime })
                        } else
                            schedule.copy(time = schedule.time.toMutableList().apply {
                                this[index] = res
                            }.sortedBy { it.startTime })
                        schedule = newPlan
                        if (schedule.time.isNotEmpty())
                            errorMessage = null
                    }
                )
                LazyVerticalGrid(
                    GridCells.Fixed(3),
                    Modifier.weight(1.5f).fillMaxHeight(),
                    contentPadding = PaddingValues(Dimensions.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall),
                ) {
                    schedule.name?.let { name ->
                        item(key = "name", contentType = "name", span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                            ) {
                                Text(
                                    text = "${title}名称：",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                val onValueChange: (String) -> Unit = remember {
                                    { input ->
                                        val newName = input.trim()
                                        if (newName.length <= 15) {
                                            schedule = schedule.copy(name = newName)
                                        }
                                    }
                                }
                                MoldOutlinedTextField(
                                    value = name,
                                    onValueChange = onValueChange,
                                    modifier = Modifier.width(230.dp),
                                    placeholder = "请输入",
                                    type = MoldTextFieldType.NEUTRAL,
                                    size = MoldTextFieldSize.MEDIUM,
                                    cornerRadius = MoldCornerRadius.LARGE,
                                    leadingIcon = null,
                                    trailingIcon = if (name.isNotEmpty()) Icons.Outlined.Close else null,
                                    onTrailingIconClick = if (name.isNotEmpty()) {
                                        { onValueChange("") }
                                    } else {
                                        null
                                    },
                                    singleLine = true,
                                )
                                Text(
                                    text = "${name.length}/15",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                    )
                                )
                            }
                        }
                    }
                    item(key = "time", contentType = "time", span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                        ) {
                            Text(
                                text = "时间段设置：",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = tips,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                )
                            )
                            MoldFilledButton(
                                onClick = { pickDialog.show(TimeRange(LocalTime(0, 0), LocalTime(0, 0)), -1) },
                                enabled = !pickDialog.isShow,
                                text = "添加时间段",
                                loading = pickDialog.isShow,
                                leadingIcon = Icons.Filled.Add,
                                contentDescription = "添加时间段",
                                size = MoldButtonSize.SMALL,
                                modifier = Modifier.padding(start = Dimensions.paddingSmall, end = Dimensions.paddingDefault),
                            )
                        }
                    }
                    errorMessage?.let {
                        item(key = "errorMessage", contentType = "errorMessage", span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                            )
                        }
                    }
                    itemsIndexed(
                        schedule.time,
                        key = { _, it -> it },
                        contentType = { _, _ -> "timeItem" }) { index, timeRange ->
                        Row(
                            modifier = Modifier
                                .padding(horizontal = Dimensions.paddingSmall)
                                .clickable { pickDialog.show(timeRange, index) }
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(Dimensions.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)
                        ) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = timeRange.format(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.clickable {
                                val newTimes = schedule.time.toMutableList().apply { removeAt(index) }
                                schedule = schedule.copy(time = newTimes)
                            }.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                DayScheduleChart(
                    schedule.time, Modifier.weight(1f).wrapContentHeight(),
                    active = activeLabel to schedule.color,
                    inactive = inactiveLabel to MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
            Row(
                Modifier.padding(Dimensions.paddingMedium).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoldOutlinedButton(
                    onClick = state::hide,
                    text = "取消",
                    type = MoldButtonType.NEUTRAL,
                    size = MoldButtonSize.MEDIUM,
                )
                MoldFilledButton(
                    onClick = {
                        if (schedule.time.isEmpty()) {
                            errorMessage = "请选择时间段"
                        } else {
                            onValue(schedule)
                            state.hide()
                        }
                    },
                    text = "确定",
                    type = MoldButtonType.DANGER,
                    size = MoldButtonSize.MEDIUM,
                )
            }
        }
    }
    state
} ?: state
