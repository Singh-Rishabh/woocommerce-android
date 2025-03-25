package com.woocommerce.android.ui.woopos.home.items.search

import com.woocommerce.android.ui.woopos.home.items.WooPosItem

sealed class WooPosItemsSearchUiEvent {
    data class ItemClicked(val item: WooPosItem) : WooPosItemsSearchUiEvent()
    data object EndOfItemsListReached : WooPosItemsSearchUiEvent()
    data object LoadingErrorRetryButtonClicked : WooPosItemsSearchUiEvent()
}
