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
        val paymentStateText: String,
        val error: Error? = null,
    ) : WooPosTotalsViewState() {
        @Parcelize
        data class Error(
            val title: String,
            val subtitle: String,
            val actionButonLabel: String,
            val onAction: () -> Unit,
        ) : Parcelable
    }

    data class PaymentProcessing(
        val title: String,
        val subtitle: String,
    ) : WooPosTotalsViewState()

    data class PaymentFailed(
        val title: String,
        val subtitle: String,
    ) : WooPosTotalsViewState()

    data class PaymentSuccess(var orderTotalText: String) : WooPosTotalsViewState()

    data class Error(val message: String) : WooPosTotalsViewState()
}
