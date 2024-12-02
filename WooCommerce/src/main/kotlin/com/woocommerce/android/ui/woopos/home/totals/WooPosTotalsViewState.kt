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
        val isCashPaymentAvailable: Boolean,
    ) : WooPosTotalsViewState()

    data class PaymentSuccess(
        val orderTotalText: String,
        val isReceiptAvailable: Boolean,
    ) : WooPosTotalsViewState()

    data class ReceiptSending(
        val email: String,
    ) : WooPosTotalsViewState()

    data class CashPayment(
        val enteredAmount: String,
        val changeDue: String,
        val total: String,
        val canBeOrderBeCompleted: Boolean,
    )

    data class Error(val message: String) : WooPosTotalsViewState()
}
