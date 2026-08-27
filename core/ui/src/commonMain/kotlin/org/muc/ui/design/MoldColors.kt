package org.muc.ui.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.materialkolor.ktx.harmonize
import org.muc.ui.design.adaptive.LocalWindowWidthType
import org.muc.ui.design.adaptive.WindowWidthType
import kotlin.random.Random
import com.materialkolor.ktx.darken as dark
import com.materialkolor.ktx.isCool as Cool
import com.materialkolor.ktx.isLight as Light
import com.materialkolor.ktx.isWarm as Warm
import com.materialkolor.ktx.lighten as lighted

/**
 * 应用的语义色。
 *
 * 这些令牌描述的是含义，而非具体的 UI 元素：
 * - [info] — 信息状态；
 * - [success] — 成功状态；
 * - [warning] — 警告。
 *
 * 在 feature 模块中使用语义色可以：
 * - 消除屏幕中硬编码的 `Color(0x...)`；
 * - 根据 light/dark 主题集中调整调色板。
 */
@Immutable
data class MoldSemanticColors(
    val info: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
)

/** 浅色主题的语义色。 */
internal val LightMoldSemanticColors = MoldSemanticColors(
    info = MoldBlue,
    success = MoldGreen,
    warning = MoldAmber,
    error = MoldRed,
)

/** 深色主题的语义色。 */
internal val DarkMoldSemanticColors = MoldSemanticColors(
    info = Color(0xFF90CAF9),
    success = Color(0xFF81C784),
    warning = Color(0xFFFFB74D),
    error = MoldRed,
)

internal val LocalMoldSemanticColors = staticCompositionLocalOf { LightMoldSemanticColors }

/**
 * 设计系统令牌的访问入口。
 */
object MoldTheme {
    /**
     * 当前主题的 Material3 colorScheme。
     *
     * 作为 feature 模块中系统颜色角色的统一访问点。
     */
    val colorScheme: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        get() = MaterialTheme.shapes

    /** [MoldTheme] 中当前的语义色。 */
    val semanticColors: MoldSemanticColors
        @Composable
        get() = LocalMoldSemanticColors.current

    val windowWidthType: WindowWidthType
        @Composable
        get() = LocalWindowWidthType.current
}

val Color.isLight get() = this.Light()

val Color.isWarm get() = this.Warm()

val Color.isCool get() = this.Cool()

fun Color.lighten(ratio: Float): Color = this.lighted(ratio)

fun Color.darken(ratio: Float): Color = this.dark(ratio)

fun Color.harmonizeWith(other: Color, matchSaturation: Boolean = false): Color = this.harmonize(other, matchSaturation)

fun Color.rand(other: Color, matchSaturation: Boolean = false): Color = this.harmonize(other, matchSaturation)

/**
 * HSL 固定饱和度+亮度可保证鲜艳
 */
fun randomVividColor(): Color = Color.hsl(
    hue = Random.nextFloat() * 360f,
    saturation = 0.7f + Random.nextFloat() * 0.3f, // 70%~100%
    lightness = 0.4f + Random.nextFloat() * 0.2f   // 40%~60%
)

/**
 * 基于种子生成确定性随机色（相同 seed → 相同颜色）
 */
fun stableRandomColor(seed: Int): Color {
    val random = Random(seed)
    return Color.hsl(
        hue = random.nextFloat() * 360f,
        saturation = 0.65f,
        lightness = 0.55f
    )
}

fun Color.adapterIsDark(ratio: Float, isDark: Boolean): Color =
    if (isDark) this.lighted(ratio) else this.darken(ratio)

@Composable
fun Color.adapterIsDark(ratio: Float, isDark: Boolean?): Color =
    adapterIsDark(ratio,isDark ?: isSystemInDarkTheme())