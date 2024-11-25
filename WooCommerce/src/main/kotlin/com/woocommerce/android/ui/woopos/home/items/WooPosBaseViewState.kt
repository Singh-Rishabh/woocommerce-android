package com.woocommerce.android.ui.woopos.home.items

sealed class WooPosBaseViewState(
    open val reloadingProductsWithPullToRefresh: Boolean
)

interface ContentViewState {
    val items: List<WooPosItem>
    val loadingMore: Boolean
    val reloadingProductsWithPullToRefresh: Boolean
}
