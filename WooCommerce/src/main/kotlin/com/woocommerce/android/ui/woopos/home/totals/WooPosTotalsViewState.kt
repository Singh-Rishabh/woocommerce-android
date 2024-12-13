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
        val readerStatus: ReaderStatus,
        val cashPaymentAvailability: CashPaymentAvailability,
    ) : WooPosTotalsViewState() {
        @Parcelize
        sealed class CashPaymentAvailability : Parcelable {
            data class Available(val orderId: Long) : CashPaymentAvailability()
            data object Unavailable : CashPaymentAvailability()
        }
    }

    sealed class ReaderStatus(
        open val title: String,
        open val subtitle: String,
    ) : Parcelable {
        @Parcelize
        data class Preparing(
            override val title: String,
            override val subtitle: String,
        ) : ReaderStatus(
            title = title,
            subtitle = subtitle
        )

        @Parcelize
        data class CheckingOrder(
            override val title: String,
            override val subtitle: String,
        ) : ReaderStatus(
            title = title,
            subtitle = subtitle
        )

        @Parcelize
        data class ReadyForPayment(
            override val title: String,
            override val subtitle: String,
        ) : ReaderStatus(
            title = title,
            subtitle = subtitle
        )

        @Parcelize
        data class Disconnected(
            override val title: String,
            override val subtitle: String,
            val actionButonLabel: String,
            val onAction: () -> Unit,
        ) : ReaderStatus(
            title = title,
            subtitle = subtitle
        )
    }

    data class PaymentInProgress(
        val title: String,
        val subtitle: String,
    ) : WooPosTotalsViewState()

    data class PaymentFailed(
        val title: String,
        val subtitle: String,
        val retryPaymentButtonLabel: String,
        val isReturnToCheckoutButtonVisible: Boolean = false,
    ) : WooPosTotalsViewState()

    data class PaymentSuccess(
        val orderTotalText: String,
        val isReceiptAvailable: Boolean,
    ) : WooPosTotalsViewState()

    data class ReceiptSending(
        val email: String,
    ) : WooPosTotalsViewState()

    data class Error(val message: String) : WooPosTotalsViewState()
}
