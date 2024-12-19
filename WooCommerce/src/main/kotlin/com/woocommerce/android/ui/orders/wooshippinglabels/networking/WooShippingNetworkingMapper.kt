package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import javax.inject.Inject

class WooShippingNetworkingMapper @Inject constructor() {
    operator fun invoke(storeOptionsDTO: StoreOptionsDTO): StoreOptionsModel {
        return StoreOptionsModel(
            currencySymbol = storeOptionsDTO.currencySymbol.orEmpty(),
            dimensionUnit = storeOptionsDTO.dimensionUnit.orEmpty(),
            weightUnit = storeOptionsDTO.weightUnit.orEmpty(),
            originCountry = storeOptionsDTO.originCountry.orEmpty()
        )
    }
}
