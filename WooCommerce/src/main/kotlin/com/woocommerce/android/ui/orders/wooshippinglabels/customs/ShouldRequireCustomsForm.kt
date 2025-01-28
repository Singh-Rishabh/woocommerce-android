package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingAddresses

class ShouldRequireCustomsForm {

    operator fun invoke(addressData: WooShippingAddresses): Boolean {
        return false
    }

    private fun isAddressInMilitaryState(
        countryCode: String,
        stateCode: String
    ) = countryCode == US_COUNTRY_CODE && stateCode in US_MILITARY_STATES

    companion object {
        const val US_COUNTRY_CODE = "US"
        val US_MILITARY_STATES = arrayOf("AA", "AE", "AP")
    }
}
