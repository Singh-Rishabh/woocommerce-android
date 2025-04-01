package com.woocommerce.android.ui.woopos.home.items

sealed class WooPosVariationsViewState(
    override val pullToRefreshState: WooPosPullToRefreshState
) : WooPosBaseViewState(pullToRefreshState) {

    data class Content(
        override val items: List<WooPosItemSelectionViewState.Variation>,
        override val pullToRefreshState: WooPosPullToRefreshState = WooPosPullToRefreshState.Enabled(
            isRefreshing = false
        ),
        override val paginationState: PaginationState = PaginationState.None,
    ) : WooPosVariationsViewState(pullToRefreshState), ContentViewState

    data class Loading(
        override val pullToRefreshState: WooPosPullToRefreshState = WooPosPullToRefreshState.Enabled(
            isRefreshing = false
        ),
        val withCart: Boolean
    ) : WooPosVariationsViewState(pullToRefreshState)

    data class Error(
        override val pullToRefreshState: WooPosPullToRefreshState = WooPosPullToRefreshState.Enabled(
            isRefreshing = false
        ),
    ) : WooPosVariationsViewState(pullToRefreshState)

    data class Empty(
        override val pullToRefreshState: WooPosPullToRefreshState = WooPosPullToRefreshState.Enabled(
            isRefreshing = false
        ),
    ) : WooPosVariationsViewState(pullToRefreshState)
}
