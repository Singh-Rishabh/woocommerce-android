package com.woocommerce.android.util

import com.woocommerce.android.cardreader.CardReaderStore.CapturePaymentResponse.Error.CaptureError
import com.woocommerce.android.cardreader.CardReaderStore.CapturePaymentResponse.Error.GenericError
import com.woocommerce.android.cardreader.CardReaderStore.CapturePaymentResponse.Error.MissingOrder
import com.woocommerce.android.cardreader.CardReaderStore.CapturePaymentResponse.Error.NetworkError
import com.woocommerce.android.cardreader.CardReaderStore.CapturePaymentResponse.Error.ServerError
import com.woocommerce.android.cardreader.CardReaderStore.CapturePaymentResponse.Successful.PaymentAlreadyCaptured
import com.woocommerce.android.cardreader.CardReaderStore.CapturePaymentResponse.Successful.Success
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentError
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentErrorType.CAPTURE_ERROR
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentErrorType.GENERIC_ERROR
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentErrorType.MISSING_ORDER
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentErrorType.NETWORK_ERROR
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentErrorType.PAYMENT_ALREADY_CAPTURED
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentErrorType.SERVER_ERROR
import org.wordpress.android.fluxc.model.payments.inperson.WCCapturePaymentResponsePayload
import javax.inject.Inject

class CapturePaymentResponseMapper @Inject constructor() {
    fun mapResponse(response: WCCapturePaymentResponsePayload) = when (response.error?.type) {
        null -> Success
        PAYMENT_ALREADY_CAPTURED -> PaymentAlreadyCaptured
        GENERIC_ERROR -> GenericError(response.error.messageOrDefault())
        MISSING_ORDER -> MissingOrder(response.error.messageOrDefault())
        CAPTURE_ERROR -> mapCaptureError(response)
        SERVER_ERROR -> ServerError(response.error.messageOrDefault())
        NETWORK_ERROR -> NetworkError(response.error.messageOrDefault())
    }

    private fun mapCaptureError(response: WCCapturePaymentResponsePayload): CaptureError {
        val extraData = response.error?.extraData ?: return CaptureError.Generic(response.error.messageOrDefault())

        return when (extraData[ERROR_TYPE_KEY]) {
            AMOUNT_TOO_SMALL_TYPE -> CaptureError.AmountTooSmall(
                message = response.error.messageOrDefault(),
                minAmountInMicroUnits = extraData[MINIMUM_AMOUNT_KEY] as? Long ?: 0L,
                currency = extraData[MINIMUM_AMOUNT_CURRENCY_KEY] as? String ?: "Unknown"
            )
            else -> CaptureError.Generic(response.error.messageOrDefault())
        }
    }

    private fun WCCapturePaymentError?.messageOrDefault() = this?.message ?: "No error message provided"

    companion object {
        private const val ERROR_TYPE_KEY = "error_type"
        private const val MINIMUM_AMOUNT_CURRENCY_KEY = "minimum_amount_currency"
        private const val AMOUNT_TOO_SMALL_TYPE = "amount_too_small"
        private const val MINIMUM_AMOUNT_KEY = "minimum_amount"
    }
}
