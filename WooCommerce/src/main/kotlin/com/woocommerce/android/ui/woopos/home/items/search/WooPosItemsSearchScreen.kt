package com.woocommerce.android.ui.woopos.home.items.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WooPosItemsSearchScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<WooPosItemsSearchViewModel>()
    val state = viewModel.viewState.collectAsState().value
    WooPosItemsSearchScreen(
        modifier = modifier,
        state = state,
    )
}

@Composable
private fun WooPosItemsSearchScreen(
    modifier: Modifier = Modifier,
    state: WooPosItemsSearchViewState,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        when (state) {
            is WooPosItemsSearchViewState.EmptySearchQuery -> {
                WooPosItemsEmptySearchQueryState(state)
            }

            is WooPosItemsSearchViewState.Content -> {
            }

            WooPosItemsSearchViewState.Empty -> {
            }
        }
    }
}
