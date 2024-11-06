package com.woocommerce.android.ui.payments.cardreader.payment.controller

import androidx.annotation.StringRes
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError

sealed class CardReaderPaymentOrRefundState {
    sealed class CardReaderPaymentState: CardReaderPaymentOrRefundState() {
        data class LoadingData(
            val onCancel: () -> Unit,
        ) : CardReaderPaymentState()

        data object ReFetchingOrder : CardReaderPaymentState()

        sealed class CollectingPayment {
            data object BuiltInReaderCollectPaymentState
            data object ExternalReaderCollectPaymentState
        }

        sealed class ProcessingPayment {
            data object BuiltInReaderProcessingPaymentState
            data object ExternalReaderProcessingPaymentState
        }

        data object PrintingReceipt

        sealed class PaymentCapturing(open val amountWithCurrencyLabel: String) {
            data class BuiltInReaderPaymentCapturing(override val amountWithCurrencyLabel: String) :
                PaymentCapturing(amountWithCurrencyLabel)

            data class ExternalReaderPaymentCapturing(override val amountWithCurrencyLabel: String) :
                PaymentCapturing(amountWithCurrencyLabel)
        }

        sealed class PaymentSuccessful: CardReaderPaymentState() {
            data object BuiltInReaderPaymentSuccessful
            data object ExternalReaderPaymentSuccessful
            data object BuiltInReaderPaymentSuccessfulReceiptSentAutomatically
            data object ExternalReaderPaymentSuccessfulReceiptSentAutomatically
        }

        sealed class PaymentFailed(
            open val errorType: PaymentFlowError,
            open val amountWithCurrencyLabel: String?,
            open val onCancel: (() -> Unit)? = null,
            open val onRetry: (() -> Unit)?,
            open val cta: CallToAction? = null,
        ): CardReaderPaymentState() {
            data class BuiltInReaderFailedPayment(
                override val errorType: PaymentFlowError,
                override val amountWithCurrencyLabel: String?,
                override val onCancel: (() -> Unit)? = null,
                override val onRetry: (() -> Unit)? = null,
                override val cta: CallToAction? = null,
            ): PaymentFailed(
                errorType,
                amountWithCurrencyLabel,
                onCancel,
                onRetry,
                cta,
            )
            data class ExternalReaderFailedPayment(
                override val errorType: PaymentFlowError,
                override val amountWithCurrencyLabel: String?,
                override val onCancel: (() -> Unit)? = null,
                override val onRetry: (() -> Unit)? = null,
                override val cta: CallToAction? = null,
            ): PaymentFailed(
                errorType,
                amountWithCurrencyLabel,
                onCancel,
                onRetry,
                cta,
            )

            data class CallToAction(
                @StringRes val label: Int,
                val onCallToActionTapped: () -> Unit,
            )
        }

        data object SharingReceipt
    }

    sealed class CardReaderRefundState {
        data object InitializingInteracRefund
        data object CollectingInteracRefund
        data object ProcessingInteracRefund
        data object InteracRefundFailure
        data object InteracRefundSuccessful
    }
}