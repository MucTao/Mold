package org.muc.ui.time

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adbdeck.core.ui.time.minutes
import kotlinx.datetime.LocalTime
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MucGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


/**
 * 24小时进度环组件 (Compose Multiplatform / M3)
 * @param intervals 时间段数据列表
 */

@Composable
fun DayScheduleChart(
    intervals: List<ClosedRange<LocalTime>>,
    modifier: Modifier = Modifier,
    active: Pair<String, Color> = "" to MucGreen, // 选中的颜色
    inactive: Pair<String, Color> = "" to MaterialTheme.colorScheme.surfaceContainerHighest, // 未选中颜色
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant // 文字和刻度的颜色
) {
    val textMeasurer = rememberTextMeasurer()
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.weight(1f).aspectRatio(1f)) {
            val padding = size.width.times(0.1f) // 半径减小10%
            inset(padding) {
                val center = size.center
                val baseRadius = (size.minDimension / 2).times(0.7f)
                // 环的物理尺寸控制
                val ringOuterRadius = baseRadius.times(0.9f) // 进度环外边缘半径
                val ringInRadius = baseRadius.times(0.6f)
                val ringThickness = ringInRadius.times(0.95f)
                // ==========================================
                // 1. 绘制底色环 (未选中区域)
                // ==========================================
                drawCircle(
                    color = inactive.second,
                    radius = ringInRadius,
                    style = Stroke(width = ringThickness)
                )

                // ==========================================
                // 2. 绘制有色时间段 (Arcs)
                // ==========================================
                intervals.forEach { interval ->
                    val startMins = interval.start.minutes
                    val endMins = interval.endInclusive.minutes

                    // 起始角度：-90度对应正上方 00:00
                    val startAngle = (startMins / 1440f * 360f) - 90f
                    var sweepAngle = ((endMins - startMins) / 1440f * 360f)

                    // 跨天处理 (例如 22:00 到 03:00)
                    if (sweepAngle <= 0) sweepAngle += 360f

                    drawArc(
                        color = active.second,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - ringInRadius, center.y - ringInRadius),
                        size = Size(ringInRadius * 2, ringInRadius * 2),
                        style = Stroke(width = ringThickness, cap = StrokeCap.Butt)
                    )
                }

                // ==========================================
                // 3. 绘制外侧刻度线 (细密排布)
                // ==========================================
                // 共 120 个刻度，每刻度代表 12 分钟
                val totalTicks = 120
                for (tick in 0 until totalTicks) {
                    val currentTickMinutes = tick * 12
                    val hasTick = intervals.any { interval ->
                        val startMins = interval.start.minutes
                        val endMins = interval.endInclusive.minutes
                        if (startMins <= endMins) {
                            // 普通时间段 (如 09:00 - 17:00)
                            currentTickMinutes in startMins..endMins
                        } else {
                            // 跨天时间段 (如 22:00 - 03:00)
                            // 刻度在开始时间之后 OR 在结束时间之前
                            currentTickMinutes !in (endMins + 1)..<startMins
                        }
                    }
                    val angleDegrees = (tick * (360f / totalTicks)) - 90f
                    val angleRadians = toRadians(angleDegrees.toDouble())

                    // 区分整点刻度与普通细分刻度
                    val isWholeHour = tick % 5 == 0 // 每 5 个刻度为一个整点 (120 / 24 = 5)

                    // 刻度线的内外半径
                    val innerRadius = ringOuterRadius + 8f
                    val outerRadius = innerRadius + if (isWholeHour) 14f else 8f
                    val tickWidth = if (isWholeHour) 2f else 1f


                    val startOffset = Offset(
                        (center.x + innerRadius * cos(angleRadians)).toFloat(),
                        (center.y + innerRadius * sin(angleRadians)).toFloat()
                    )
                    val endOffset = Offset(
                        (center.x + outerRadius * cos(angleRadians)).toFloat(),
                        (center.y + outerRadius * sin(angleRadians)).toFloat()
                    )

                    drawLine(
                        color = if (hasTick) textColor else textColor.copy(0.2f),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = tickWidth
                    )
                }

                // ==========================================
                // 4. 绘制整点数字 (0-23)
                // ==========================================
                for (hour in 0 until 24) {
                    val hasHour = intervals.any { hour in it.start.hour..it.endInclusive.hour }
                    val textStyle = TextStyle(color = if (hasHour) textColor else textColor.copy(0.2f), fontSize = 11.sp)
                    val angleDegrees = (hour * 15f) - 90f // 24小时，每小时15度
                    val angleRadians = toRadians(angleDegrees.toDouble())

                    // 数字的放置半径 (在刻度线外侧)
                    val textRadius = ringOuterRadius + 34f
                    val x = (center.x + textRadius * cos(angleRadians)).toFloat()
                    val y = (center.y + textRadius * sin(angleRadians)).toFloat()

                    val textLayoutResult = textMeasurer.measure(
                        text = hour.toString(),
                        style = textStyle
                    )

                    // 核心：减去文字自身的宽高的一半，实现完美的绝对居中对齐
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x - textLayoutResult.size.width / 2f,
                            y - textLayoutResult.size.height / 2f
                        )
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(Dimensions.paddingMedium), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                Box(Modifier.size(18.dp).background(active.second, CircleShape))
                Text(active.first, style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)) {
                Box(Modifier.size(18.dp).background(inactive.second, CircleShape))
                Text(inactive.first, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun toRadians(degrees: Double): Double = degrees * PI / 180.0


