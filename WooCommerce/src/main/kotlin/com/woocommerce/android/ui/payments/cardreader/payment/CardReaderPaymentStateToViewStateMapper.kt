package com.woocommerce.android.ui.payments.cardreader.payment

import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState
import javax.inject.Inject

class CardReaderPaymentStateToViewStateMapper @Inject constructor() {
    operator fun invoke(): (CardReaderPaymentOrRefundState) -> ViewState = TODO()
}