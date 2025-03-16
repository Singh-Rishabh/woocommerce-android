package com.cataloghub.android.ui.payments.cardreader.payment

import com.cataloghub.android.R
import com.cataloghub.android.model.Order
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.util.CurrencyFormatter
import com.cataloghub.android.viewmodel.ResourceProvider
import javax.inject.Inject

class CardReaderPaymentOrderHelper @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val selectedSite: SelectedSite,
    private val currencyFormatter: CurrencyFormatter,
) {
    fun getPaymentDescription(order: Order): String =
        resourceProvider.getString(
            R.string.card_reader_payment_description_v2,
            order.number,
            selectedSite.get().name.orEmpty(),
            selectedSite.get().remoteId().value
        )

    fun getAmountLabel(order: Order): String = currencyFormatter
        .formatAmountWithCurrency(order.total.toDouble(), order.currency)

    fun getReceiptDocumentName(orderId: Long) = "receipt-order-$orderId"
}
