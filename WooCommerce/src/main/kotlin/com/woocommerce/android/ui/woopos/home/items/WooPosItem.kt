package com.woocommerce.android.ui.woopos.home.items

sealed class WooPosItem(
    open val id: Long,
    open val name: String,
    open val price: String,
) {
    data class SimpleProduct(
        override val id: Long,
        override val name: String,
        override val price: String,
        val imageUrl: String?,
    ) : WooPosItem(id, name, price)

    data class VariableProduct(
        override val id: Long,
        override val name: String,
        override val price: String,
        val imageUrl: String?,
        val numOfVariations: Int,
        val variationIds: List<Long>,
    ) : WooPosItem(id, name, price)

    data class Variation(
        override val id: Long,
        override val name: String,
        val productId: Long,
        override val price: String,
        val imageUrl: String?,
    ) : WooPosItem(id, name, price)
}
