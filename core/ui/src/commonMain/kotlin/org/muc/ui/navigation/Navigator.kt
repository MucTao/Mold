package org.muc.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No LocalNavigator found!") }

/**
 * 通过更新导航状态来处理导航事件（向前和向后）
 * @param state -将在响应导航事件时更新的导航状态。
 */
class Navigator(
    val state: NavigationState,
    private val onNavigateToRestrictedKey: (targetKey: NavKey?) -> NavKey,
    private val isLoggedIn: () -> Boolean,
) {

    /**
     * 导航到NavKey
     *
     * @param key - 要导航到的NavKey
     */
    fun navigate(key: NavKey) {
        if (!isLoggedIn()) {
            val loginKey = onNavigateToRestrictedKey(key)
            goToKey(loginKey)
        } else {
            when (key) {
                state.currentTopLevelKey -> clearSubStack()
                in state.topLevelKeys -> goToTopLevel(key)
                else -> goToKey(key)
            }
        }
    }

    /**
     * 返回到上一个NavKey
     */
    fun goBack() {
        when (state.currentKey) {
            state.startKey -> goToTopLevel(state.startKey)
            state.currentTopLevelKey -> {
                // 我们在当前子堆栈的底部，回到之前的顶层
                state.topLevelStack.removeLastOrNull()
            }

            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    /** Removes transient pages above the requested destination. */
    fun backTo(key: NavKey) {
        val destinationIndex = state.currentSubStack.indexOfLast { it::class == key::class }
        if (destinationIndex < 0) {
            navigate(key)
            return
        }
        if (destinationIndex < state.currentSubStack.lastIndex) {
            state.currentSubStack.subList(destinationIndex + 1, state.currentSubStack.size).clear()
        }
    }

    fun isTopLevel(key: NavKey): Boolean = key == state.topLevelKeys

    /**
     * 转到非顶级NavKey.
     */
    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            removeAll { it::class == key::class }
            add(key)
            println(this.joinToString())
        }
    }

    /**
     * 转到顶级NavKey.
     */
    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                // 这是起始键。清除堆栈，以便将其作为唯一键添加。
                clear()
            } else {
                // 如果它已经在堆栈中，则将其删除，以便在最后添加它
                remove(key)
            }
            add(key)
        }
    }

    /**
     * 清除当前子栈中的所有键，但保留根键。
     */
    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
