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
    val quantity: Float,
    val isExpanded: Boolean
) : Parcelable {
    val valueAndWeightForDisplay: String
        get() = "${valuePerUnit.currentInput} • ${weightPerUnit.currentInput}"

    val isValid: Boolean
        get() = description is InputValue.Data &&
            tariffNumber is InputValue.Data &&
            valuePerUnit is InputValue.Data &&
            weightPerUnit is InputValue.Data

    val shippingTotalValue: Float?
        get() = valuePerUnit
            .takeIf { valuePerUnit is InputValue.Data && quantity > 0 }
            ?.run { this as? InputValue.Data }
            ?.currentInput
            ?.toFloatOrNull()
            ?.let { it * quantity }
}
