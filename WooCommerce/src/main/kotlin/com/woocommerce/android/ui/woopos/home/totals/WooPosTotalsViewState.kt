package com.woocommerce.android.ui.woopos.home.totals

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WooPosTotalsViewState : Parcelable {
    data object Loading : WooPosTotalsViewState()

    data class Totals(
        val orderSubtotalText: String,
        val orderTaxText: String,
        val orderTotalText: String,
        val cashPaymentAvailability: CashPaymentAvailability,
    ) : WooPosTotalsViewState() {
        @Parcelize
        sealed class CashPaymentAvailability : Parcelable {
            data class Available(val orderId: Long) : CashPaymentAvailability()
            object Unavailable : CashPaymentAvailability()
        }
    }

    data class PaymentSuccess(
        val orderTotalText: String,
        val isReceiptAvailable: Boolean,
    ) : WooPosTotalsViewState()

    data class ReceiptSending(
        val email: String,
    ) : WooPosTotalsViewState()

    data class Error(val message: String) : WooPosTotalsViewState()
}
