package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ContentType
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.RestrictionType

data class WooShippingCustomsForm(
    val packageId: String,
    val packageName: String,
    val contentType: ContentType,
    val contentDescription: String,
    val restrictionType: RestrictionType,
    val restrictionDescription: String,
    val noDeliveryOption: Boolean,
    val itn: String,
)
