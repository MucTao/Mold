package org.muc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule

@Serializable
object Home : NavKey

/**
 *  SerializersModule {
 *             polymorphic(baseClass = NavKey::class) {
 *                 subclass(serializer = Home.serializer())
 *             }
 *         }
 * 创建持久化配置更改和进程死亡的导航状态
 */
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
    serializers: SerializersModule
): NavigationState {
    val config = SavedStateConfiguration {
        serializersModule = serializers
    }
    val topLevelStack = rememberNavBackStack(config, startKey)
    val subStacks = topLevelKeys.associateWith { key -> rememberNavBackStack(config, key) }
    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}

/**
 * 导航状态的状态持有者
 *
 * @param startKey - 开始导航键。用户将通过此密钥退出应用程序.
 * @param topLevelStack - 最顶层的反向堆栈。它只保存顶级键
 * @param subStacks - 每个顶级键的后面堆叠
 */
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }

    val topLevelKeys get() = subStacks.keys

    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey]
            ?: error("Sub stack for $currentTopLevelKey does not exist")

    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }

    val currentIsTopLevel get() = topLevelKeys.contains(currentKey)
}

/**
 * 将NavigationState转换为NavEntries。
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator<NavKey>(),
    )
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    return remember(topLevelStack, decoratedEntries) {
        derivedStateOf {
            topLevelStack
                .flatMap { decoratedEntries[it] ?: emptyList() }
                .toMutableStateList()
        }
    }.value
}
