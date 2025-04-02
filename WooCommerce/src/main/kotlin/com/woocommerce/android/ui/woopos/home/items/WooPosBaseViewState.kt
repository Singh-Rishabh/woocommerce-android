package com.woocommerce.android.ui.woopos.home.items

sealed class WooPosBaseViewState(
    open val pullToRefreshState: WooPosPullToRefreshState
)

interface ContentViewState {
    val items: List<WooPosItemSelectionViewState>
    val pullToRefreshState: WooPosPullToRefreshState
    val paginationState: PaginationState
}

sealed class PaginationState {
    data object None : PaginationState()
    data object Loading : PaginationState()
    data object Error : PaginationState()
}

enum class WooPosPullToRefreshState {
    Disabled, Enabled, Refreshing,
}
