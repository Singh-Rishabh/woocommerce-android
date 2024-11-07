package com.woocommerce.android.ui.payments.cardreader.payment.controller

import androidx.annotation.StringRes
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError

sealed class CardReaderPaymentOrRefundState {
    sealed class CardReaderPaymentState : CardReaderPaymentOrRefundState() {
        data class LoadingData(
            val onCancel: () -> Unit,
        ) : CardReaderPaymentState()

        data object ReFetchingOrder : CardReaderPaymentState()

        sealed class CollectingPayment(
            open val amountWithCurrencyLabel: String,
        ) : CardReaderPaymentOrRefundState() {
            data class BuiltInReaderCollectPaymentState(override val amountWithCurrencyLabel: String) :
                CollectingPayment(amountWithCurrencyLabel)

            data class ExternalReaderCollectPaymentState(
                override val amountWithCurrencyLabel: String,
                val onCancel: (() -> Unit)
            ) : CollectingPayment(amountWithCurrencyLabel)
        }

        sealed class ProcessingPayment(
            open val amountWithCurrencyLabel: String,
        ) : CardReaderPaymentState() {
            data class BuiltInReaderProcessingPayment(override val amountWithCurrencyLabel: String) :
                ProcessingPayment(amountWithCurrencyLabel)

            data class ExternalReaderProcessingPayment(
                override val amountWithCurrencyLabel: String,
                val onCancel: () -> Unit
            ) : ProcessingPayment(amountWithCurrencyLabel)
        }

        data class PrintingReceipt(val amountWithCurrencyLabel: String): CardReaderPaymentState()

        sealed class PaymentCapturing(open val amountWithCurrencyLabel: String) : CardReaderPaymentState() {
            data class BuiltInReaderPaymentCapturing(override val amountWithCurrencyLabel: String) :
                PaymentCapturing(amountWithCurrencyLabel)

            data class ExternalReaderPaymentCapturing(override val amountWithCurrencyLabel: String) :
                PaymentCapturing(amountWithCurrencyLabel)
        }

        sealed class PaymentSuccessful : CardReaderPaymentState() {
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
        ) : CardReaderPaymentState() {
            data class BuiltInReaderFailedPayment(
                override val errorType: PaymentFlowError,
                override val amountWithCurrencyLabel: String?,
                override val onCancel: (() -> Unit)? = null,
                override val onRetry: (() -> Unit)? = null,
                override val cta: CallToAction? = null,
            ) : PaymentFailed(
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
            ) : PaymentFailed(
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

        data object SharingReceipt: CardReaderPaymentState()
    }

    sealed class CardReaderRefundState {
        data object InitializingInteracRefund
        data object CollectingInteracRefund
        data object ProcessingInteracRefund
        data object InteracRefundFailure
        data object InteracRefundSuccessful
    }
}
