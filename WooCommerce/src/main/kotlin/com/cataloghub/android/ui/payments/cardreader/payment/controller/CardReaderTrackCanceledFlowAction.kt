package com.cataloghub.android.ui.payments.cardreader.payment.controller

import com.cataloghub.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderInteracRefundState
import com.cataloghub.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState
import com.cataloghub.android.ui.payments.tracking.PaymentsFlowTracker
import com.cataloghub.android.util.WooLog
import javax.inject.Inject

class CardReaderTrackCanceledFlowAction @Inject constructor(
    private val tracker: PaymentsFlowTracker,
) {
    operator fun invoke(state: CardReaderPaymentOrRefundState) = when (state) {
        is CardReaderPaymentState -> {
            val nameForTracking = when (state) {
                is CardReaderPaymentState.CollectingPayment -> "Collecting"
                is CardReaderPaymentState.PaymentCapturing -> "Capturing"
                is CardReaderPaymentState.ProcessingPayment -> "Processing"
                is CardReaderPaymentState.LoadingData -> "Loading"
                else -> null
            }
            if (nameForTracking == null) {
                WooLog.e(WooLog.T.CARD_READER, "Invalid state received")
            } else {
                tracker.trackPaymentCancelled(nameForTracking)
            }
        }
        is CardReaderInteracRefundState -> {
            val nameForTracking = when (state) {
                is CardReaderInteracRefundState.CollectingInteracRefund -> "Collecting"
                is CardReaderInteracRefundState.LoadingData -> "Loading"
                is CardReaderInteracRefundState.ProcessingInteracRefund -> "Processing"
                else -> null
            }
            if (nameForTracking == null) {
                WooLog.e(WooLog.T.CARD_READER, "Invalid state received")
            } else {
                tracker.trackInteracRefundCancelled(nameForTracking)
            }
        }
    }
}
