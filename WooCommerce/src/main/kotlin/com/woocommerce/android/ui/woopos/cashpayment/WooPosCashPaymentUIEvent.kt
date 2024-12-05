package com.woocommerce.android.ui.woopos.cashpayment

sealed class WooPosCashPaymentUIEvent {
    object CompleteOrderClicked: WooPosCashPaymentUIEvent()
    data class AmountChanged(val newAmount: String): WooPosCashPaymentUIEvent()
}
