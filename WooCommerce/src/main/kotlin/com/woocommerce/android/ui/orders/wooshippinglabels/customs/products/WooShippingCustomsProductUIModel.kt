package com.woocommerce.android.ui.orders.wooshippinglabels.customs.products

import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.InputValue

data class WooShippingCustomsProductUIModel(
    val name: String,
    val description: InputValue,
    val tariffNumber: InputValue,
    val valuePerUnit: InputValue,
    val weightPerUnit: InputValue,
    val originCountry: String,
    val isExpanded: Boolean
) {
    val valueAndWeightForDisplay: String
        get() = "${valuePerUnit.currentInput} • ${weightPerUnit.currentInput}"
}
