package org.muc.ui.sectioncards
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.muc.ui.design.Dimensions
import org.muc.ui.design.MucTheme

/**
 * 通用分区卡片（带标题和自定义内容插槽）
 *
 * 该组件用于统一设置/信息区块的样式：
 * - 标题（必填）；
 * - 副标题（可选）；
 * - 标题右侧的操作区域（可选）；
 * - 可承载任意 Composable 内容的容器。
 *
 * @param title 分区标题文本。
 * @param modifier 外部修饰符 [Modifier]。
 * @param subtitle 标题下方的补充文本。
 * @param titleUppercase 是否将标题转为大写。
 * @param titleColor 标题颜色。
 * @param titleTextStyle 分区标题的文字样式。
 * @param subtitleTextStyle 分区副标题的文字样式。
 * @param containerColor 分区容器的背景色。
 * @param border 容器边框。
 * @param shape 容器形状，默认使用主题内置样式。
 * @param tonalElevation 容器的色调提升值（视觉层级）。
 * @param contentPadding 容器内边距。
 * @param contentSpacing 分区内元素的垂直间距。
 * @param headerTrailing 标题右侧的附加内容（如按钮）。
 * @param content 分区的自定义内容插槽。
 */
@Composable
fun MucSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleUppercase: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.primary,
    titleTextStyle: TextStyle = MaterialTheme.typography.labelSmall,
    subtitleTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    shape: Shape = MaterialTheme.shapes.small,
    tonalElevation: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = Dimensions.paddingMedium,
        vertical = Dimensions.paddingSmall,
    ),
    contentSpacing: Dp = Dimensions.paddingXSmall,
    headerTrailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val resolvedTitle = if (titleUppercase) title.uppercase() else title

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingXSmall),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resolvedTitle,
                    style = titleTextStyle,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = subtitleTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (headerTrailing != null) {
                Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
                headerTrailing()
            }
        }

        Surface(
            shape = shape,
            color = containerColor,
            border = border,
            tonalElevation = tonalElevation,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(contentSpacing),
                content = content,
            )
        }
    }
}

@Preview
@Composable
private fun MucSectionCardLightPreview() {
    MucTheme(isDarkTheme = false) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingDefault),
        ) {
            MucSectionCard(
                title = "连接",
                subtitle = "设备基础参数",
                titleUppercase = true,
            ) {
                Text(text = "设备ID: emulator-5554")
                Text(text = "状态: device")
            }
        }
    }
}

@Preview
@Composable
private fun MucSectionCardDarkPreview() {
    MucTheme(isDarkTheme = true) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingDefault),
        ) {
            MucSectionCard(
                title = "高危操作",
                subtitle = "需要确认的操作",
                titleColor = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                ),
            ) {
                Text(text = "恢复模式")
                Text(text = "引导加载程序 / 快速启动")
            }
        }
    }
}