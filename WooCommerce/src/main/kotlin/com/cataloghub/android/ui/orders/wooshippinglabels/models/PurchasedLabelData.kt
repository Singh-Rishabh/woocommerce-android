package com.cataloghub.android.ui.orders.wooshippinglabels.models

import com.cataloghub.android.model.Address
import com.cataloghub.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRateModel

data class PurchasedLabelData(
    val labels: List<ShippingLabelModel>,
    val origin: Map<String, OriginShippingAddress>,
    val destination: Map<String, Address>,
    val rates: Map<String, WooShippingRateModel>,
)
