package com.woocommerce.android.ui.woopos.home.totals

sealed class WooPosTotalsUIEvent {
    data object OnNewTransactionClicked : WooPosTotalsUIEvent()
    data object RetryFailedTransactionClicked : WooPosTotalsUIEvent()
    data object GoBackToCheckoutAfterFailedPayment : WooPosTotalsUIEvent()
    data object RetryOrderCreationClicked : WooPosTotalsUIEvent()
}
