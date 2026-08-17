package com.adbdeck.core.ui.time

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.serializers.LocalTimeIso8601Serializer
import kotlinx.serialization.Serializable

@Stable
inline val LocalTime.minutes: Int
    get() = hour * 60 + minute

@Serializable
data class TimeRange(
    //    开始显示时间 12:00
    @Serializable(with = LocalTimeIso8601Serializer::class)
    val startTime: LocalTime = LocalTime(0, 0),

    //    结束显示时间 13:00
    @Serializable(with = LocalTimeIso8601Serializer::class)
    val endTime: LocalTime = LocalTime(23, 59),
) : ClosedRange<LocalTime>, OpenEndRange<LocalTime> {
    // 碰撞检测：两个区间有交集
    fun intersects(other: TimeRange): Boolean {
        return maxOf(this.startTime, other.startTime) < minOf(this.endTime, other.endTime)
    }

    fun intersects(other: LocalTime): Boolean {
        return other in startTime..endTime
    }

    fun format(): String {
        return "${
            startTime.format(LocalTime.Format {
                hour(); char(':')
                minute()
            })
        } - ${
            endTime.format(LocalTime.Format {
                hour(); char(':')
                minute()
            })
        }"
    }

    override val start: LocalTime
        get() = startTime
    override val endInclusive: LocalTime
        get() = endTime

    override fun contains(value: LocalTime): Boolean = value >= startTime && startTime <= endTime


    override fun isEmpty(): Boolean = startTime > endTime

    override val endExclusive: LocalTime
        get() = LocalTime(endInclusive.hour, endInclusive.minute, endInclusive.second, endInclusive.nanosecond + 1)
}

internal typealias TimeRangePickerDialogState = MutableState<Pair<TimeRange, Int>?>


@Composable
internal fun rememberTimeRangePickerDialogState() = remember { mutableStateOf<Pair<TimeRange, Int>?>(null) }

@Stable
inline val TimeRangePickerDialogState.isShow: Boolean
    get() = value != null

fun TimeRangePickerDialogState.show(range: TimeRange, index: Int = -1) {
    this.value = range to index
}

fun TimeRangePickerDialogState.hide() {
    this.value = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangePickerDialog(
    timeList: List<TimeRange>,
    onConfirm: (TimeRange, Int) -> Unit,
    state: TimeRangePickerDialogState = rememberTimeRangePickerDialogState()
): TimeRangePickerDialogState {
    state.value?.let { (timeRange, index) ->
        val filter = timeList.filter { index == -1 || it != timeRange }
        val startState = rememberTimePickerState(timeRange.start.hour, timeRange.start.minute, is24Hour = true)
        val endState = rememberTimePickerState(timeRange.endInclusive.hour, timeRange.endInclusive.minute, is24Hour = true)

        var selectingEnd by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = state::hide,
            confirmButton = {
                TextButton(onClick = {
                    val start = LocalTime(startState.hour, startState.minute)
                    if (!selectingEnd) {
                        errorMessage = checkTime(filter, start)
                        if (errorMessage == null)
                            selectingEnd = true
                    } else {
                        val end = LocalTime(endState.hour, endState.minute)
                        val newPeriod = TimeRange(start, end)
                        errorMessage = checkResult(filter, newPeriod)
                        if (errorMessage == null) {
                            onConfirm(newPeriod, index)
                            state.hide()
                        }
                    }
                }) {
                    Text(if (!selectingEnd) "下一步 (选择结束时间)" else "完成")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (!selectingEnd) {
                        state.hide()
                    } else {
                        errorMessage = null
                        selectingEnd = false
                    }
                }) {
                    Text(if (!selectingEnd) "取消" else "上一步(选择开始时间)")
                }
            },
            title = { Text(if (!selectingEnd) "开始时间" else "结束时间") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectingEnd) {
                        Text(
                            text = "开始时间：${
                                LocalTime(startState.hour, startState.minute).format(LocalTime.Format {
                                    hour(); char(':')
                                    minute()
                                })
                            }",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                        )
                    }
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        if (!selectingEnd) {
                            TimePicker(state = startState)
                        } else {
                            TimePicker(state = endState)
                        }
                    }
                }
            }
        )
    }
    return state
}

private fun checkTime(
    existingPeriods: List<TimeRange>,
    newTime: LocalTime
): String? {
    val hasCollision = existingPeriods.find { it.intersects(newTime) }
    return if (hasCollision != null) {
        "该时间与已有时间段(${hasCollision.format()})冲突"
    } else {
        null
    }
}

private fun checkResult(
    existingPeriods: List<TimeRange>,
    newPeriod: TimeRange
): String? {
    if (newPeriod.endTime <= newPeriod.startTime) {
        return "结束时间必须晚于开始时间"
    }

    val hasCollision = existingPeriods.find { it.intersects(newPeriod) }

    return if (hasCollision != null) {
        "该时间段与已有时间段(${hasCollision.format()}冲突"
    } else {
        null
    }
}