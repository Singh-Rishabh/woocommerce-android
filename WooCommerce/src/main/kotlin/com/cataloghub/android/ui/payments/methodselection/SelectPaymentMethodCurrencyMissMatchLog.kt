package com.cataloghub.android.ui.payments.methodselection

import com.cataloghub.android.util.WooLog
import com.cataloghub.android.util.WooLogWrapper
import javax.inject.Inject

class SelectPaymentMethodCurrencyMissMatchLog @Inject constructor(
    private val wooLogWrapper: WooLogWrapper,
) {
    operator fun invoke(storeCurrency: String, orderCurrency: String) {
        if (!storeCurrency.equals(orderCurrency, ignoreCase = true)) {
            val message = "⚠️ Order currency: $orderCurrency differs from store's currency: " +
                "$storeCurrency which can lead to payment methods being unavailable"
            wooLogWrapper.w(WooLog.T.ORDERS, message)
        }
    }
}
