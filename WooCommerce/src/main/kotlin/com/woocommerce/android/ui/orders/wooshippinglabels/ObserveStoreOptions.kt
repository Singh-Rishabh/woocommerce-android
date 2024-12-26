package com.woocommerce.android.ui.orders.wooshippinglabels

import com.woocommerce.android.ui.orders.wooshippinglabels.datasource.ShippingLabelsDataStore
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveStoreOptions @Inject constructor(
    val dataStore: ShippingLabelsDataStore
) {
    operator fun invoke(): Flow<StoreOptionsModel?> = dataStore.observeStoreOptions()
}
