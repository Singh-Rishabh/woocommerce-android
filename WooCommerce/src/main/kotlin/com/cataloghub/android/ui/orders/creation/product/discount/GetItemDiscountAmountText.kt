package com.cataloghub.android.ui.orders.creation.product.discount

import com.cataloghub.android.util.CurrencyFormatter
import java.math.BigDecimal
import javax.inject.Inject

class GetItemDiscountAmountText @Inject constructor(
    private val formatter: CurrencyFormatter
) {
    operator fun invoke(discountAmount: BigDecimal, currency: String): String {
        return formatter.formatCurrency(discountAmount, currency)
    }
}
