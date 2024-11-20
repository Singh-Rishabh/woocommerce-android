package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import javax.inject.Inject
import javax.inject.Singleton
import org.wordpress.android.fluxc.store.WCShippingLabelStore

@Singleton
class WooShippingLabelPackageRepository @Inject constructor(
    private val shippingLabelStore: WCShippingLabelStore
) {
}
