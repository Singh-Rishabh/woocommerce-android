package com.cataloghub.android.ui.orders.wooshippinglabels

import com.cataloghub.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import com.cataloghub.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import com.cataloghub.android.util.CurrencyFormatter

fun ShippableItemModel.toUIModel(
    currencyFormatter: CurrencyFormatter,
    storeOptions: StoreOptionsModel
): ShippableItemUI {
    return ShippableItemUI(
        itemId = itemId,
        productId = productId,
        title = title,
        formattedPrice = currencyFormatter.formatCurrency(price, currency),
        quantity = quantity,
        imageUrl = imageUrl,
        formattedSize = getSizeWithUnits(storeOptions.dimensionUnit),
        formattedWeight = getWeightWithUnits(storeOptions.weightUnit)
    )
}
