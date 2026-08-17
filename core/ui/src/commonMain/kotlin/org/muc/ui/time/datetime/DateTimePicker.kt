package org.muc.ui.time.datetime

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import org.jetbrains.compose.resources.stringResource
import org.muc.ui.action.ConfirmDialog
import org.muc.ui.buttons.MucButtonType
import org.muc.ui.buttons.MucFilledButton
import org.muc.ui.buttons.MucOutlinedButton
import org.muc.ui.design.Dimensions
import org.muc.ui.i18n.MucCommonStringRes
import kotlin.time.Clock
import kotlin.time.Instant


fun LocalDateTime.Companion.now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
fun LocalDate.Companion.now() = LocalDateTime.now().date

// --- 核心组件 1: DateTimeRangePickerModal (范围选择) ---
@Composable
fun DateTimeRangePickerModal(
    onRangeSelected: (start: LocalDateTime, end: LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    // 1: 开始日期, 2: 开始 time, 3: 结束日期, 4: 结束 time
    var currentStep by remember { mutableStateOf(1) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    when (currentStep) {
        // STEP 1: 选择开始日期
        1 -> {
            DatePickerModal(
                title = "选择开始日期",
                onDateSelected = { date ->
                    startDate = date
                    currentStep = 2
                },
                onDismiss = onDismiss // 第一步取消则直接关闭
            )
        }

        // STEP 2: 选择开始时间
        2 -> {
            TimePickerModal(
                title = "选择开始时间",
                onTimeSelected = { time ->
                    startTime = time
                    currentStep = 3
                },
                onDismiss = { currentStep = 1 } // 体验优化：点击取消返回上一步选择日期
            )
        }

        // STEP 3: 选择结束日期
        3 -> {
            // 体验优化：限制结束日期必须大于或等于开始日期
            val endSelectableDates = remember(startDate) {
                object : SelectableDates {
                    val minStart =
                        startDate?.plus(1, DateTimeUnit.DAY)?.atStartOfDayIn(TimeZone.currentSystemDefault())
                            ?.toEpochMilliseconds() ?: 0L
                    val end = startDate?.plus(12, DateTimeUnit.MONTH)
                        ?.atStartOfDayIn(TimeZone.currentSystemDefault())
                        ?.toEpochMilliseconds() ?: 0L

                    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minStart..end
                    override fun isSelectableYear(year: Int): Boolean = year >= (startDate?.year ?: 0)
                }
            }

            DatePickerModal(
                title = "选择结束日期",
                selectableDates = endSelectableDates,
                onDateSelected = { date ->
                    endDate = date
                    currentStep = 4
                },
                onDismiss = { currentStep = 2 } // 体验优化：点击取消返回选开始时间
            )
        }

        // STEP 4: 选择结束时间
        4 -> {
            TimePickerModal(
                title = "选择结束时间",
                onTimeSelected = { endTime ->
                    val finalStart = startDate?.atTime(startTime!!)
                    val finalEnd = endDate?.atTime(endTime)
                    if (finalStart != null && finalEnd != null && finalStart < finalEnd) {
                        onRangeSelected(finalStart, finalEnd)
                    }
                },
                onDismiss = { currentStep = 3 } // 体验优化：点击取消返回选结束日期
            )
        }
    }
}

// --- 核心组件 2: DateTimePickerModal (单日期时间选择) ---
@Composable
fun DateTimePickerModal(
    selectableDates: SelectableDates = remember { DefaultSelectableDates() },
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    when (currentStep) {
        1 -> {
            DatePickerModal(
                title = "选择日期",
                selectableDates = selectableDates,
                onDateSelected = { date ->
                    selectedDate = date
                    currentStep = 2
                },
                onDismiss = onDismiss
            )
        }

        2 -> {
            TimePickerModal(
                title = "选择时间",
                onTimeSelected = { time ->
                    selectedDate?.let { date ->
                        onDateTimeSelected(date.atTime(time))
                    }
                },
                onDismiss = { currentStep = 1 } // 体验优化：允许退回日期选择
            )
        }
    }
}

// --- 基础组件 3: DatePickerModal (被改造，支持标题与自定义限制) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    title: String? = null,
    selectableDates: SelectableDates = remember { DefaultSelectableDates() }, // 支持默认或外部传入限制
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDatePickerState(selectableDates = selectableDates)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            MucFilledButton(
                onClick = {
                    val selectedDateMillis = dateRangePickerState.selectedDateMillis ?: return@MucFilledButton
                    onDateSelected(
                        Instant.fromEpochMilliseconds(selectedDateMillis)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    )
                },
                text = stringResource(MucCommonStringRes.actionConfirm),
                type = MucButtonType.NEUTRAL,
            )
        },
        dismissButton = {
            MucOutlinedButton(onClick = onDismiss, text = stringResource(MucCommonStringRes.actionCancel))
        }
    ) {
        DatePicker(
            state = dateRangePickerState,
            title = title?.let { { Text(text = it, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) } },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(vertical = Dimensions.paddingDefault)
        )
    }
}

// 默认的日期可选限制（T+1 到 12个月内）
@OptIn(ExperimentalMaterial3Api::class)
private class DefaultSelectableDates : SelectableDates {
    val todayDate = LocalDate.now()
    val start = todayDate
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()

    val end = todayDate.plus(12, DateTimeUnit.MONTH)
        .atStartOfDayIn(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()

    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in start..end
    override fun isSelectableYear(year: Int): Boolean = year >= todayDate.year
}

// --- 基础组件 4: TimePickerModal (被改造，支持标题) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    title: String? = null,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(is24Hour = true)
    ConfirmDialog(title, onConfirm = {
        onTimeSelected(LocalTime(state.hour, state.minute))
    }, onDismiss = onDismiss) {
        TimePicker(
            state = state
        )
    }
}

// --- 基础组件 5: DateRangePickerModal (原封不动保留，单选日期范围用) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    title: String? = null,
    selectableDates: SelectableDates = remember { DefaultSelectableDates() }, // 支持默认或外部传入限制
    onRangeSelected: (LocalDateRange) -> Unit,
    onDismiss: () -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState(selectableDates = selectableDates)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            MucFilledButton(
                onClick = {
                    val startMillis = dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val zone = TimeZone.currentSystemDefault()
                        val startDate = Instant.fromEpochMilliseconds(startMillis).toLocalDateTime(zone).date
                        val endDate = Instant.fromEpochMilliseconds(endMillis).toLocalDateTime(zone).date
                        val dates = startDate..endDate
                        onRangeSelected(dates)
                    }
                },
                text = stringResource(MucCommonStringRes.actionConfirm),
                type = MucButtonType.NEUTRAL,
            )
        },
        dismissButton = {
            MucOutlinedButton(onClick = onDismiss, text = stringResource(MucCommonStringRes.actionCancel))
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = title?.let { { Text(text = it, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) } },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(vertical = Dimensions.paddingDefault)
        )
    }
}