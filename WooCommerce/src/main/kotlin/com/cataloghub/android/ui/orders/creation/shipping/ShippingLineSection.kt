package com.cataloghub.android.ui.orders.creation.shipping

data class ShippingLineSection(
    val shippingLines: List<ShippingLineDetails>,
    val isEnabled: Boolean
)
