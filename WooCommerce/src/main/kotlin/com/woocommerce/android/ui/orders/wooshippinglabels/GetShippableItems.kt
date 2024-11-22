package com.woocommerce.android.ui.orders.wooshippinglabels

import com.woocommerce.android.extensions.formatToString
import com.woocommerce.android.extensions.sumByFloat
import com.woocommerce.android.model.Order
import com.woocommerce.android.model.getNonRefundedProducts
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptions
import com.woocommerce.android.ui.products.details.ProductDetailRepository
import com.woocommerce.android.util.CurrencyFormatter
import javax.inject.Inject

class GetShippableItems @Inject constructor(
    private val orderDetailRepository: OrderDetailRepository,
    private val productDetailRepository: ProductDetailRepository,
    private val currencyFormatter: CurrencyFormatter
) {
    suspend operator fun invoke(
        order: Order,
        storeOptions: StoreOptions
    ): ShippableItems {
        val refunds = orderDetailRepository.getOrderRefunds(order.id)
        val noRefundedProducts = refunds.getNonRefundedProducts(order.items)

        val shippableProductItems = noRefundedProducts.mapNotNull { item ->
            (
                productDetailRepository.getProductAsync(item.productId)
                    ?: productDetailRepository.fetchAndGetProduct(item.productId)
                )?.let {
                Pair(it, item)
            }
        }.filter { product ->
            product.first.isSampleProduct.not() && product.first.isVirtual.not()
        }

        val shippableItems = shippableProductItems.map {
            val (product, item) = it
            ShippableItem(
                productId = product.remoteId,
                title = product.name,
                description = product.getSizeWithUnits(storeOptions.dimensionUnit),
                weight = product.getWeightWithUnits(storeOptions.weightUnit),
                price = currencyFormatter.formatCurrency(
                    amount = item.total,
                    currencyCode = order.currency
                ),
                quantity = item.quantity,
                imageUrl = product.firstImageUrl
            )
        }

        val totalPrice = shippableProductItems.sumOf { it.second.total }
        val totalWeight = shippableProductItems.sumByFloat { it.first.weight * it.second.quantity }

        return ShippableItems(
            shippableItems = shippableItems,
            totalWeight = totalWeight.formatToString() + storeOptions.weightUnit,
            totalPrice = currencyFormatter.formatCurrency(
                amount = totalPrice,
                currencyCode = order.currency
            )
        )
    }
}
