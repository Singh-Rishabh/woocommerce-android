package com.woocommerce.android.ui.woopos.home.items.variations

sealed class WooPosVariationsUIEvents {
    data class EndOfItemsListReached(val productId: Long) : WooPosVariationsUIEvents()
}
