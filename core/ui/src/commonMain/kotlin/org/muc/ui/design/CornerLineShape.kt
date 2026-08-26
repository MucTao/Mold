package org.muc.ui.design

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * 四角画线 Shape —— 仅描绘矩形四个角的折线段，
 * 用于配合 Modifier.border() 实现"扫描框/聚焦框"效果。
 *
 * 参数语义与 [RoundedCornerShape] 完全对齐，只是把"圆角半径"换成"角线长度"。
 * @param topStartRadius/ ...   每个角的"圆弧半径"，0 表示直角
 */
class CornerLineShape(
    private val topStart: CornerSize,
    private val topEnd: CornerSize,
    private val bottomEnd: CornerSize,
    private val bottomStart: CornerSize,
    private val topStartRadius: CornerSize = CornerSize(0f),
    private val topEndRadius: CornerSize = CornerSize(0f),
    private val bottomEndRadius: CornerSize = CornerSize(0f),
    private val bottomStartRadius: CornerSize = CornerSize(0f)
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        var topStartPx = topStart.toPx(size, density)
        var topEndPx = topEnd.toPx(size, density)
        var bottomEndPx = bottomEnd.toPx(size, density)
        var bottomStartPx = bottomStart.toPx(size, density)
        val topStartRadiusPx = topStartRadius.toPx(size, density)
        val topEndRadiusPx = topEndRadius.toPx(size, density)
        val bottomEndRadiusPx = bottomEndRadius.toPx(size, density)
        val bottomStartRadiusPx = bottomStartRadius.toPx(size, density)
        val minDimension = size.minDimension
        if (topStartPx + bottomStartPx > minDimension) {
            val scale = minDimension / (topStartPx + bottomStartPx)
            topStartPx *= scale
            bottomStartPx *= scale
        }
        if (topEndPx + bottomEndPx > minDimension) {
            val scale = minDimension / (topEndPx + bottomEndPx)
            topEndPx *= scale
            bottomEndPx *= scale
        }
        val w = size.width
        val h = size.height
        val halfMin = minOf(w, h) / 2f

        fun clampLen(len: Float, radius: Float): Pair<Float, Float> {
            val r = radius.coerceIn(0f, len).coerceAtMost(halfMin)
            val l = len.coerceAtMost(halfMin).coerceAtLeast(r)
            return l to r
        }
        val (tsLen, tsR) = clampLen(topStartPx, topStartRadiusPx)
        val (teLen, teR) = clampLen(topEndPx, topEndRadiusPx)
        val (beLen, beR) = clampLen(bottomEndPx, bottomEndRadiusPx)
        val (bsLen, bsR) = clampLen(bottomStartPx, bottomStartRadiusPx)

        val path = Path().apply {
            // ┌ 左上 (topStart) 起点 (0, tsLen) → 直线到 (0, tsR) → 圆弧到 (tsR, 0) → 直线到 (tsLen, 0)
            moveTo(0f, tsLen)
            lineTo(0f, 0f)
            if (tsR > 0f) {
                arcTo(
                    rect = Rect(0f, 0f, tsR * 2, tsR * 2),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }
            lineTo(tsLen, 0f)

            // ┐ 右上 (topEnd)
            moveTo(w - teLen, 0f)
            lineTo(w, 0f)
            if (teR > 0f) {
                arcTo(
                    rect = Rect(w - teR * 2, 0f, w, teR * 2),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }
            lineTo(w, teLen)

            // ┘ 右下 (bottomEnd)
            moveTo(w, h - beLen)
            lineTo(w, h)
            if (beR > 0f) {
                arcTo(
                    rect = Rect(w - beR * 2, h - beR * 2, w, h),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }
            lineTo(w - beLen, h)

            // └ 左下 (bottomStart)
            moveTo(bsLen, h)
            lineTo(0f, h)
            if (bsR > 0f) {
                arcTo(
                    rect = Rect(0f, h - bsR * 2, bsR * 2, h),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
            }
            lineTo(0f, h - bsLen)
        }
        return Outline.Generic(path)
    }


    fun copy(
        topStart: CornerSize = this.topStart,
        topEnd: CornerSize = this.topEnd,
        bottomEnd: CornerSize = this.bottomEnd,
        bottomStart: CornerSize = this.bottomStart,
        topStartRadius: CornerSize = this.topStartRadius,
        topEndRadius: CornerSize = this.topEndRadius,
        bottomEndRadius: CornerSize = this.bottomEndRadius,
        bottomStartRadius: CornerSize = this.bottomStartRadius
    ) = CornerLineShape(topStart, topEnd, bottomEnd, bottomStart, topStartRadius, topEndRadius, bottomEndRadius, bottomStartRadius)
}


// ---------- 2. 统一 Dp（角线长度 + 可选圆角）----------
fun CornerLineShape(
    size: Dp,
    cornerRadius: Dp = 0.dp
): CornerLineShape {
    val len = CornerSize(size)
    val r = CornerSize(cornerRadius)
    return CornerLineShape(len, len, len, len, r, r, r, r)
}

// ---------- 3. 统一 Float (px) ----------
fun CornerLineShape(
    size: Float,
    cornerRadius: Float = 0f
): CornerLineShape {
    val len = CornerSize(size)
    val r = CornerSize(cornerRadius)
    return CornerLineShape(len, len, len, len, r, r, r, r)
}

// ---------- 4. 统一 Int 百分比 ----------
fun CornerLineShape(
    percent: Int,
    cornerRadiusPercent: Int = 0
): CornerLineShape {
    val len = CornerSize(percent)
    val r = CornerSize(cornerRadiusPercent)
    return CornerLineShape(len, len, len, len, r, r, r, r)
}

// ---------- 5. 统一 CornerSize ----------
fun CornerLineShape(
    corner: CornerSize,
    cornerRadius: CornerSize = CornerSize(0f)
): CornerLineShape =
    CornerLineShape(corner, corner, corner, corner, cornerRadius, cornerRadius, cornerRadius, cornerRadius)


// ---------- 7. 四角分别 Float ----------
fun CornerLineShape(
    topStart: Float = 0f,
    topEnd: Float = 0f,
    bottomEnd: Float = 0f,
    bottomStart: Float = 0f,
    cornerRadius: Float = 0f
): CornerLineShape = CornerLineShape(
    topStart = CornerSize(topStart),
    topEnd = CornerSize(topEnd),
    bottomEnd = CornerSize(bottomEnd),
    bottomStart = CornerSize(bottomStart),
    topStartRadius = CornerSize(cornerRadius),
    topEndRadius = CornerSize(cornerRadius),
    bottomEndRadius = CornerSize(cornerRadius),
    bottomStartRadius = CornerSize(cornerRadius)
)

// ---------- 8. 四角分别 Int 百分比 ----------
fun CornerLineShape(
    topStartPercent: Int = 0,
    topEndPercent: Int = 0,
    bottomEndPercent: Int = 0,
    bottomStartPercent: Int = 0,
    cornerRadiusPercent: Int = 0
): CornerLineShape = CornerLineShape(
    topStart = CornerSize(topStartPercent),
    topEnd = CornerSize(topEndPercent),
    bottomEnd = CornerSize(bottomEndPercent),
    bottomStart = CornerSize(bottomStartPercent),
    topStartRadius = CornerSize(cornerRadiusPercent),
    topEndRadius = CornerSize(cornerRadiusPercent),
    bottomEndRadius = CornerSize(cornerRadiusPercent),
    bottomStartRadius = CornerSize(cornerRadiusPercent)
)

// ---------- 9. 四角线长 + 四角圆角 完全独立控制（终极版）----------
fun CornerLineShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
    topStartRadius: Dp = 0.dp,
    topEndRadius: Dp = 0.dp,
    bottomEndRadius: Dp = 0.dp,
    bottomStartRadius: Dp = 0.dp
): CornerLineShape = CornerLineShape(
    topStart = CornerSize(topStart),
    topEnd = CornerSize(topEnd),
    bottomEnd = CornerSize(bottomEnd),
    bottomStart = CornerSize(bottomStart),
    topStartRadius = CornerSize(topStartRadius),
    topEndRadius = CornerSize(topEndRadius),
    bottomEndRadius = CornerSize(bottomEndRadius),
    bottomStartRadius = CornerSize(bottomStartRadius)
)