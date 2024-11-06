package com.woocommerce.android.ui.woopos.home.items

sealed class WooPosItem(
    open val id: Long,
    open val name: String
) {
    data class SimpleProduct(
        override val id: Long,
        override val name: String,
        val price: String,
        val imageUrl: String?,
    ) : WooPosItem(id, name), ClickableItem {
        override fun onItemClick(onUIEvent: (WooPosItemsUIEvent) -> Unit) {
            onUIEvent(WooPosItemsUIEvent.ItemClicked(this))
        }
    }

    data class VariableProduct(
        override val id: Long,
        override val name: String,
        val price: String,
        val imageUrl: String?,
        val numOfVariations: Int,
        val variationIds: List<Long>,
    ) : WooPosItem(id, name), ClickableItem {
        override fun onItemClick(onUIEvent: (WooPosItemsUIEvent) -> Unit) {
            onUIEvent(
                WooPosItemsUIEvent.NavigateToVariationsScreen(
                    WooPosItemNavigationData.VariableProductData(
                        id = id,
                        name = name,
                        numOfVariations = numOfVariations,
                        variationIds = variationIds
                    )
                )
            )
        }
    }
}

interface ClickableItem {
    fun onItemClick(onUIEvent: (WooPosItemsUIEvent) -> Unit)
}
