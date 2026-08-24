package org.muc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider

typealias EntryProviderInstaller = EntryProviderScope<NavKey>.() -> Unit

interface BaseNavKey : NavKey {
    @Composable
    fun PageTopBar()  {}

    @Composable
    fun PageContent()
}

fun baseEntryProvider(list: Set<EntryProviderInstaller>): (NavKey) -> NavEntry<NavKey> = entryProvider {
    list.forEach { builder -> this.builder() }
}

inline fun <reified K : BaseNavKey> EntryProviderScope<NavKey>.pageEntry(metadata: Map<String, Any> = emptyMap()) {
    entry<K>(metadata = metadata) { it.PageContent() }
}
