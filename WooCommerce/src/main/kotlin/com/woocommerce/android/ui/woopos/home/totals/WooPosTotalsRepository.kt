package com.woocommerce.android.ui.woopos.home.totals

import com.woocommerce.android.model.Order
import com.woocommerce.android.ui.orders.creation.OrderCreateEditRepository
import com.woocommerce.android.ui.woopos.common.data.WooPosGetProductById
import com.woocommerce.android.ui.woopos.common.data.WooPosGetVariationById
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsViewModel
import com.woocommerce.android.util.DateUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class WooPosTotalsRepository @Inject constructor(
    private val orderCreateEditRepository: OrderCreateEditRepository,
    private val dateUtils: DateUtils,
    private val getProductById: WooPosGetProductById,
    private val getVariationById: WooPosGetVariationById,
) {
    private var orderCreationJob: Deferred<Result<Order>>? = null

    suspend fun createOrderWithProducts(
        itemClickedDataList: List<WooPosItemsViewModel.ItemClickedData>
    ): Result<Order> {
        check(itemClickedDataList.map { it.id }.isNotEmpty()) { "List of IDs is empty" }

        orderCreationJob?.cancel()

        return withContext(IO) {
            itemClickedDataList.map { it.id }.forEach { productId ->
                require(productId >= 0) { "Invalid product ID: $productId" }
            }

            orderCreationJob = async {
                val order = Order.getEmptyOrder(
                    dateCreated = dateUtils.getCurrentDateInSiteTimeZone() ?: Date(),
                    dateModified = dateUtils.getCurrentDateInSiteTimeZone() ?: Date()
                ).copy(
                    status = Order.Status.Custom(Order.Status.AUTO_DRAFT),
                    items = itemClickedDataList
                        .groupingBy { it.id }
                        .eachCount()
                        .map { (id, quantity) ->
                            val itemData = itemClickedDataList.find { it.id == id }!!
                            val productResult = when (itemData) {
                                is WooPosItemsViewModel.ItemClickedData.SimpleProduct -> getProductById(itemData.id)!!
                                is WooPosItemsViewModel.ItemClickedData.Variation ->
                                    getProductById(itemData.productId)!!
                            }
                            when (itemData) {
                                is WooPosItemsViewModel.ItemClickedData.SimpleProduct -> {
                                    Order.Item.EMPTY.copy(
                                        itemId = 0L,
                                        productId = id,
                                        variationId = 0L,
                                        quantity = quantity.toFloat(),
                                        total = EMPTY_TOTALS_SUBTOTAL_VALUE,
                                        subtotal = EMPTY_TOTALS_SUBTOTAL_VALUE,
                                        attributesList = emptyList(),
                                        name = productResult.name,
                                    )
                                }
                                is WooPosItemsViewModel.ItemClickedData.Variation -> {
                                    val variationResult = getVariationById(
                                        productId = itemData.productId,
                                        variationId = itemData.id
                                    )!!
                                    Order.Item.EMPTY.copy(
                                        itemId = 0L,
                                        productId = id,
                                        variationId = variationResult.remoteVariationId,
                                        quantity = quantity.toFloat(),
                                        total = EMPTY_TOTALS_SUBTOTAL_VALUE,
                                        subtotal = EMPTY_TOTALS_SUBTOTAL_VALUE,
                                        attributesList = emptyList(),
                                        name = variationResult.getName(productResult),
                                    )
                                }
                            }
                        }
                )

                orderCreateEditRepository.createOrUpdateOrder(order)
            }
            orderCreationJob!!.await()
        }
    }

    private companion object {
        /**
         * This magic value used to indicate that we don't want to send subtotals and totals
         * And let the backend to calculate them.
         */
        val EMPTY_TOTALS_SUBTOTAL_VALUE = -Double.MAX_VALUE.toBigDecimal()
    }
}
