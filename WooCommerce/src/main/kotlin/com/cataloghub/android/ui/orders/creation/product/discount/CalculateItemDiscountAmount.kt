package com.cataloghub.android.ui.orders.creation.product.discount

import com.cataloghub.android.model.Order
import java.math.BigDecimal
import javax.inject.Inject

class CalculateItemDiscountAmount @Inject constructor() {
    operator fun invoke(item: Order.Item): BigDecimal {
        return (item.subtotal - item.total)
    }
}
