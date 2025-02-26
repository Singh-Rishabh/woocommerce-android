package com.woocommerce.android.ui.woopos.util.analytics

import javax.inject.Singleton

@Singleton
class WooPosAnalyticsTrackingDataKeeper(
    var interactionWithCustomerStartedTimestamp: Long = EMPTY_TIMESTAMP,
    var orderSyncSuccessTimestamp: Long = EMPTY_TIMESTAMP,
    var readerReadyForPaymentTimestamp: Long = EMPTY_TIMESTAMP,
    var cardTappedTimestamp: Long = EMPTY_TIMESTAMP,
    var checkoutButtonTapsCount: Int = 0,
) {

    fun reset() {
        interactionWithCustomerStartedTimestamp = EMPTY_TIMESTAMP
        orderSyncSuccessTimestamp = EMPTY_TIMESTAMP
        readerReadyForPaymentTimestamp = EMPTY_TIMESTAMP
        cardTappedTimestamp = EMPTY_TIMESTAMP
        checkoutButtonTapsCount = 0
    }

    private companion object  {
        private const val EMPTY_TIMESTAMP = -1L
    }
}
