package com.woocommerce.android.ui.woopos.emailreceipt

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WooPosEmailReceiptState(
    val email: String,
    val errorMessage: String?,
    val button: Button
) : Parcelable {
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
