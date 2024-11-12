package com.woocommerce.android.ui.payments.cardreader.payment

import com.woocommerce.android.R
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderInteracRefundState
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.CollectingPayment
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.LoadingData
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentCapturing
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentFailed
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentSuccessful
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PrintingReceipt
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.ProcessingPayment
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.ReFetchingOrder
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.SharingReceipt
import javax.inject.Inject

class CardReaderPaymentStateToViewStateMapper @Inject constructor(
    private val cardReaderPaymentReaderTypeStateProvider: CardReaderPaymentReaderTypeStateProvider,
) {
    operator fun invoke(): (CardReaderPaymentOrRefundState) -> ViewState = { paymentState ->
        when (paymentState) {
            is CardReaderInteracRefundState.CollectingInteracRefund -> {
                ViewState.CollectRefundState(
                    amountWithCurrencyLabel = paymentState.amountWithCurrencyLabel,
                    onSecondaryActionClicked = paymentState.onCancel,
                    hintLabel = paymentState.cardReaderHint
                        ?: R.string.card_reader_interac_refund_refund_payment_hint,
                )
            }
            is CardReaderInteracRefundState.InteracRefundFailure -> TODO()
            is CardReaderInteracRefundState.InteracRefundSuccessful -> TODO()
            is CardReaderInteracRefundState.LoadingData -> TODO()
            is CardReaderInteracRefundState.ProcessingInteracRefund -> TODO()
            is CollectingPayment.BuiltInReaderCollectPaymentState -> TODO()
            is CollectingPayment.ExternalReaderCollectPaymentState -> TODO()
            is LoadingData -> ViewState.LoadingDataState(paymentState.onCancel)
            is PaymentCapturing.BuiltInReaderPaymentCapturing -> TODO()
            is PaymentCapturing.ExternalReaderPaymentCapturing -> TODO()
            is PaymentFailed.BuiltInReaderFailedPayment -> TODO()
            is PaymentFailed.ExternalReaderFailedPayment -> TODO()
            is PaymentSuccessful.BuiltInReaderPaymentSuccessful -> TODO()
            is PaymentSuccessful.BuiltInReaderPaymentSuccessfulReceiptSentAutomatically -> TODO()
            is PaymentSuccessful.ExternalReaderPaymentSuccessful -> TODO()
            is PaymentSuccessful.ExternalReaderPaymentSuccessfulReceiptSentAutomatically -> TODO()
            is PrintingReceipt -> TODO()
            is ProcessingPayment.BuiltInReaderProcessingPayment -> TODO()
            is ProcessingPayment.ExternalReaderProcessingPayment -> TODO()
            ReFetchingOrder -> ViewState.ReFetchingOrderState
            SharingReceipt -> ViewState.SharingReceiptState
        }
    }
}