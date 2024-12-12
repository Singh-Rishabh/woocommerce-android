package com.woocommerce.android.ui.woopos.cashpayment

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class WooPosCashPaymentState : Parcelable {
    data class Collecting(
        val enteredAmount: String,
        val changeDue: String,
        val total: String,
        val button: Button
    ) : WooPosCashPaymentState() {
        @Parcelize
        data class Button(
            val text: String,
            val status: Status,
        ) : Parcelable {
            @Parcelize
            enum class Status : Parcelable {
                ENABLED,
                DISABLED,
                LOADING
            }
        }
    }

    object Complete : WooPosCashPaymentState()

    object Initiating : WooPosCashPaymentState()
}
