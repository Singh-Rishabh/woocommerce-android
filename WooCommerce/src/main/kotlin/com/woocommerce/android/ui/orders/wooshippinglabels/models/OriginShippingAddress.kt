package com.woocommerce.android.ui.orders.wooshippinglabels.models

data class OriginShippingAddress(
    val id: String,
    val company: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val state: String?,
    val postcode: String,
    val country: String,
    val phone: String?,
    val isDefault: Boolean,
    val isVerified: Boolean
)
