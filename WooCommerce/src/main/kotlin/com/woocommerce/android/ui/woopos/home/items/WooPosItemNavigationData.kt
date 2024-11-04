package com.woocommerce.android.ui.woopos.home.items

sealed class WooPosItemNavigationData(open val id: Long) {
    data class SimpleProductData(
        override val id: Long
    ) : WooPosItemNavigationData(id)
}
