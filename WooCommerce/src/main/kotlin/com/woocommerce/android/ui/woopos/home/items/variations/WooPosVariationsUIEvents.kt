package com.woocommerce.android.ui.woopos.home.items.variations

sealed class WooPosVariationsUIEvents {
    data class EndOfItemsListReached(val productId: Long) : WooPosVariationsUIEvents()
    data class PullToRefreshTriggered(val productId: Long) : WooPosVariationsUIEvents()
}
