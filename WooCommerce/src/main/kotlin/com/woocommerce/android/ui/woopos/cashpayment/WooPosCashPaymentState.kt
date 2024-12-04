package com.woocommerce.android.ui.woopos.cashpayment

data class WooPosCashPaymentState(
    val enteredAmount: String,
    val changeDue: String,
    val total: String,
    val canBeOrderBeCompleted: Boolean,
)
