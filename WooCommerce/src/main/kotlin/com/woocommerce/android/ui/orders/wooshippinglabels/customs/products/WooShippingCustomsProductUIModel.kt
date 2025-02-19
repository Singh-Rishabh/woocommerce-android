package com.woocommerce.android.ui.orders.wooshippinglabels.customs.products

data class WooShippingCustomsProductUIModel(
    val name: String,
    val description: String,
    val tariffNumber: String,
    val valuePerUnit: String,
    val weightPerUnit: String,
    val originCountry: String,
    val isExpanded: Boolean
)
