package com.woocommerce.android.ui.payments.cardreader.payment.controller

import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderType
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderType.BUILT_IN
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderType.EXTERNAL
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentFailed.BuiltInReaderFailedPayment
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentFailed.CallToAction
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentFailed.ExternalReaderFailedPayment
import javax.inject.Inject

class CardReaderPaymentStateProvider @Inject constructor() {
    fun provideFailedPaymentState(
        cardReaderType: CardReaderType,
        errorType: PaymentFlowError,
        amountLabel: String,
        onCancel: () -> Unit,
        onRetry: (() -> Unit)? = null,
        cta: CallToAction? = null,
    ) = when (cardReaderType) {
        BUILT_IN -> BuiltInReaderFailedPayment(
            errorType = errorType,
            amountWithCurrencyLabel = amountLabel,
            onCancel = onCancel,
            onRetry = onRetry,
            cta = cta
        )
        EXTERNAL -> ExternalReaderFailedPayment(
            errorType = errorType,
            amountWithCurrencyLabel = amountLabel,
            onCancel = onCancel,
            onRetry = onRetry,
            cta = cta
        )
    }
}
