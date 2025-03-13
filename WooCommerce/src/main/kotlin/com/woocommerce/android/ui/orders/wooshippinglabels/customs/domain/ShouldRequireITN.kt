package com.woocommerce.android.ui.orders.wooshippinglabels.customs.domain

import javax.inject.Inject

class ShouldRequireITN @Inject constructor() {
    operator fun invoke(
        destinationCountryCode: String,
        totalShippingItemValue: Float
    ): Boolean {
        return false
    }

    private val itnExemptCountries = listOf("CA")
    private val itnRequiredCountries = listOf("IR", "KP", "SY", "CU", "SD")
}
