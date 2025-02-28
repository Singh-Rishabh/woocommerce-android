package com.woocommerce.android.ui.orders.wooshippinglabels.customs.products

import android.os.Parcelable
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.InputValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class WooShippingCustomsProductUIModel(
    val name: String,
    val description: InputValue,
    val tariffNumber: InputValue,
    val valuePerUnit: InputValue,
    val weightPerUnit: InputValue,
    val originCountry: String,
    val isExpanded: Boolean
) : Parcelable {
    val valueAndWeightForDisplay: String
        get() = "${valuePerUnit.currentInput} • ${weightPerUnit.currentInput}"
}
