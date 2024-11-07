package com.woocommerce.android.ui.payments.cardreader.payment.controller

import androidx.annotation.StringRes
import com.woocommerce.android.ui.payments.cardreader.payment.InteracRefundFlowError
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError

sealed class CardReaderPaymentOrRefundState {
    interface TrackableState {
        val nameForTracking: String
    }

    sealed class CardReaderPaymentState : CardReaderPaymentOrRefundState() {
        data class LoadingData(val onCancel: () -> Unit,) : CardReaderPaymentState(), TrackableState {
            override val nameForTracking: String = "Loading"
        }

        data object ReFetchingOrder : CardReaderPaymentState()

        sealed class CollectingPayment(
            open val amountWithCurrencyLabel: String,
            @StringRes open val cardReaderHint: Int? = null,
        ) : CardReaderPaymentState(), TrackableState {
            data class BuiltInReaderCollectPaymentState(
                override val amountWithCurrencyLabel: String,
                override val cardReaderHint: Int? = null,
            ) : CollectingPayment(amountWithCurrencyLabel, cardReaderHint) {
                override val nameForTracking: String
                    get() = "Collecting"
            }

            data class ExternalReaderCollectPaymentState(
                override val amountWithCurrencyLabel: String,
                override val cardReaderHint: Int? = null,
                val onCancel: (() -> Unit)
            ) : CollectingPayment(amountWithCurrencyLabel, cardReaderHint) {
                override val nameForTracking: String
                    get() = "Collecting"
            }
        }

        sealed class ProcessingPayment(
            open val amountWithCurrencyLabel: String,
        ) : CardReaderPaymentState(), TrackableState {
            data class BuiltInReaderProcessingPayment(override val amountWithCurrencyLabel: String) :
                ProcessingPayment(amountWithCurrencyLabel) {
                override val nameForTracking: String
                    get() = "Processing"
            }

            data class ExternalReaderProcessingPayment(
                override val amountWithCurrencyLabel: String,
                val onCancel: () -> Unit
            ) : ProcessingPayment(amountWithCurrencyLabel) {
                override val nameForTracking: String
                    get() = "Processing"
            }
        }

        data class PrintingReceipt(val amountWithCurrencyLabel: String) : CardReaderPaymentState()

        sealed class PaymentCapturing(open val amountWithCurrencyLabel: String) :
            CardReaderPaymentState(), TrackableState {
            data class BuiltInReaderPaymentCapturing(override val amountWithCurrencyLabel: String) :
                PaymentCapturing(amountWithCurrencyLabel) {
                override val nameForTracking: String
                    get() = "Capturing"
            }

            data class ExternalReaderPaymentCapturing(override val amountWithCurrencyLabel: String) :
                PaymentCapturing(amountWithCurrencyLabel) {
                override val nameForTracking: String
                    get() = "Capturing"
            }
        }

        sealed class PaymentSuccessful(
            open val amountWithCurrencyLabel: String,
        ) : CardReaderPaymentState() {
            data class BuiltInReaderPaymentSuccessful(
                override val amountWithCurrencyLabel: String,
                val onPrintReceiptClicked: () -> Unit,
                val onSendReceiptClicked: () -> Unit,
                val onSaveUserClicked: () -> Unit
            ) : PaymentSuccessful(amountWithCurrencyLabel)

            data class ExternalReaderPaymentSuccessful(
                override val amountWithCurrencyLabel: String,
                val onPrintReceiptClicked: () -> Unit,
                val onSendReceiptClicked: () -> Unit,
                val onSaveUserClicked: () -> Unit
            ) : PaymentSuccessful(amountWithCurrencyLabel)
            data class BuiltInReaderPaymentSuccessfulReceiptSentAutomatically(
                override val amountWithCurrencyLabel: String,
                val recipientEmail: String,
                val onPrintReceiptClicked: () -> Unit,
                val onSaveUserClicked: () -> Unit
            ) : PaymentSuccessful(amountWithCurrencyLabel)
            data class ExternalReaderPaymentSuccessfulReceiptSentAutomatically(
                override val amountWithCurrencyLabel: String,
                val recipientEmail: String,
                val onPrintReceiptClicked: () -> Unit,
                val onSaveUserClicked: () -> Unit
            ) : PaymentSuccessful(amountWithCurrencyLabel)
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
        }

        data object SharingReceipt : CardReaderPaymentState()
    }

    sealed class CardReaderInteracRefundState : CardReaderPaymentOrRefundState() {
        data class LoadingData(
            val onCancel: () -> Unit,
        ) : CardReaderInteracRefundState(), TrackableState {
            override val nameForTracking: String = "Loading"
        }

        data class CollectingInteracRefund(
            val amountWithCurrencyLabel: String,
            @StringRes val cardReaderHint: Int? = null,
        ) : CardReaderInteracRefundState(), TrackableState {
            override val nameForTracking: String = "Collecting"
        }

        data class ProcessingInteracRefund(
            val amountWithCurrencyLabel: String,
        ) : CardReaderInteracRefundState(), TrackableState {
            override val nameForTracking: String = "Processing"
        }

        data class InteracRefundFailure(
            val amountWithCurrencyLabel: String?,
            val errorType: InteracRefundFlowError,
            val onCancel: (() -> Unit)? = null,
            val onRetry: (() -> Unit)? = null,
            val cta: CallToAction? = null,
        ) : CardReaderInteracRefundState()

        data class InteracRefundSuccessful(
            val amountWithCurrencyLabel: String,
        ) : CardReaderInteracRefundState()
    }

    data class CallToAction(
        @StringRes val label: Int,
        val onCallToActionTapped: () -> Unit,
    )
}
