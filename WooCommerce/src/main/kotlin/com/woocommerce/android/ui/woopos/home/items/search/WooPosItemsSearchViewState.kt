package com.woocommerce.android.ui.woopos.home.items.search

import com.woocommerce.android.ui.woopos.home.items.ContentViewState
import com.woocommerce.android.ui.woopos.home.items.PaginationState
import com.woocommerce.android.ui.woopos.home.items.WooPosItem

sealed class WooPosItemsSearchViewState {
    data class EmptySearchQuery(
        val popularItems: List<WooPosItem.Product>,
        val recentSearches: List<String>,
    ) : WooPosItemsSearchViewState()

    data class Content(
        override val items: List<WooPosItem.Product>,
        override val reloadingProductsWithPullToRefresh: Boolean = false,
        override val paginationState: PaginationState = PaginationState.None,
    ) : WooPosItemsSearchViewState(), ContentViewState

    data object Empty : WooPosItemsSearchViewState()
    data object Error : WooPosItemsSearchViewState()
    data object Loading : WooPosItemsSearchViewState()
}
