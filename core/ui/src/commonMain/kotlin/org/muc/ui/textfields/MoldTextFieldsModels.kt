package org.muc.ui.textfields

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 预配置颜色语义的固定文本框类型。
 */
enum class MoldTextFieldType {
    NORMAL,

    PRIMARY,

    /** 中性状态。 */
    NEUTRAL,

    /** 错误/危险状态。 */
    DANGER,

    /** 成功/有效状态。 */
    SUCCESS,
}

/**
 * 文本框尺寸。
 *
 * 尺寸与其他通用组件（`buttons`、`segmented`、`split`）保持一致。
 */
enum class MoldTextFieldSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconSize: Dp,
    val contentSpacing: Dp,
) {
    /** 大尺寸。 */
    LARGE(
        height = 40.dp,
        horizontalPadding = 14.dp,
        iconSize = 18.dp,
        contentSpacing = 8.dp,
    ),

    /** 中尺寸。 */
    MEDIUM(
        height = 34.dp,
        horizontalPadding = 12.dp,
        iconSize = 16.dp,
        contentSpacing = 6.dp,
    ),

    /** 小尺寸。 */
    SMALL(
        height = 30.dp,
        horizontalPadding = 10.dp,
        iconSize = 14.dp,
        contentSpacing = 6.dp,
    ),

    /** 超小尺寸。 */
    XSMALL(
        height = 26.dp,
        horizontalPadding = 8.dp,
        iconSize = 12.dp,
        contentSpacing = 4.dp,
    ),
}

/**
 * Опция выпадающего списка для dropdown text field.
 *
 * @param value Бизнес-значение опции.
 * @param label Текст, отображаемый в поле и в меню.
 * @param enabled Доступность опции в меню.
 */
@Immutable
data class MoldDropdownOption<T>(
    val value: T,
    val label: String,
    val key: String = value.toString(),
    val enabled: Boolean = true,
)

// 树形下拉选项模型
data class MoldTreeDropdownOption<T>(
    val label: String,
    val value: T,
    val children: List<MoldTreeDropdownOption<T>> = emptyList(),
    val id: String = value.toString(),
    val key: String = value.toString(),//相同id的叶子可以在不同的枝
    val enabled: Boolean = true,
    val isLeaf: Boolean = children.isEmpty(), // 是否为叶子节点（可选中）
)

/**
 * 递归过滤，直到根节点数量不为 1
 */
tailrec fun <T> List<MoldTreeDropdownOption<T>>.filterSingleRootRecursive(): List<MoldTreeDropdownOption<T>> {
    val nodes = filterNot { it.children.isEmpty() }
    return if (nodes.size == 1) {
        nodes.first().children.filterSingleRootRecursive()
    } else {
        nodes
    }
}

/**
 * 严格去重（整棵树只保留第一次遇见的 ID）
 */
fun <T> List<MoldTreeDropdownOption<T>>.dfsDistinct(visited: MutableSet<String> = mutableSetOf()): List<MoldTreeDropdownOption<T>> {
    return this
        .filter { it.key !in visited } // 过滤掉已经存在的 ID
        .map { node ->
            visited.add(node.key) // 记录当前 ID
            node.copy(children = node.children.dfsDistinct(visited)) // 递归处理子节点
        }
}


internal enum class MoldTextFieldStyle {
    FILLED,
    OUTLINED,
    PLAIN,
}

@Immutable
internal data class MoldTextFieldResolvedColors(
    val containerColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val placeholderColor: Color,
    val iconColor: Color,
    val cursorColor: Color,
    val supportingColor: Color,
    val disabledContainerColor: Color,
    val disabledBorderColor: Color,
    val disabledTextColor: Color,
    val disabledPlaceholderColor: Color,
    val disabledIconColor: Color,
)
