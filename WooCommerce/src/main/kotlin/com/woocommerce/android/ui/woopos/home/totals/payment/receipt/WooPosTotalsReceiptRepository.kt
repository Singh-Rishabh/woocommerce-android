package com.woocommerce.android.ui.woopos.home.totals.payment.receipt

import org.wordpress.android.fluxc.store.WCOrderStore
import javax.inject.Inject

class WooPosTotalsReceiptRepository @Inject constructor(
    orderStore: WCOrderStore,
) {
    suspend fun sendReceiptByEmail(orderId: Long, email: String) {

    }
}
