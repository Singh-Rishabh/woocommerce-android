package com.woocommerce.android.ui.woopos.cashpayment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WooPosCashPaymentState : Parcelable {
    data class Collecting(
        val enteredAmount: String,
        val changeDue: String,
        val total: String,
        val canBeOrderBeCompleted: Boolean,
    ) : WooPosCashPaymentState()

    object Finishing : WooPosCashPaymentState()

    object Initiating : WooPosCashPaymentState()
}
