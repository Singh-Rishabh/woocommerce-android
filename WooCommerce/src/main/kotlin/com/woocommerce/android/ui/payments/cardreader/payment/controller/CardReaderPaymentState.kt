package com.woocommerce.android.ui.payments.cardreader.payment.controller

sealed class CardReaderPaymentState {
    data class LoadingData(
        val onCancelPaymentCollection: () -> Unit,
    ) : CardReaderPaymentState()
}