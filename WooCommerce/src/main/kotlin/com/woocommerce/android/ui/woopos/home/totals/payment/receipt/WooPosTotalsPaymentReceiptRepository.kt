package com.woocommerce.android.ui.woopos.home.totals.payment.receipt

import com.woocommerce.android.model.Order
import com.woocommerce.android.model.OrderMapper
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.creation.OrderCreateEditRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCOrderStore
import javax.inject.Inject

class WooPosTotalsPaymentReceiptRepository @Inject constructor(
    private val selectedSite: SelectedSite,
    private val orderStore: WCOrderStore,
    private val orderCreateEditRepository: OrderCreateEditRepository,
    private val orderMapper: OrderMapper,
) {
    suspend fun sendReceiptByEmail(orderId: Long, email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val order = getOrderById(orderId) ?: return@withContext Result.failure(Exception("Order not found"))
        val updatedCustomer = order.customer?.copy(email = email) ?: Order.Customer.EMPTY.copy(email = email)
        val updatedOrder = order.copy(customer = updatedCustomer)
        val updateOrderResult = orderCreateEditRepository.createOrUpdateOrder(updatedOrder)

        if (updateOrderResult.isFailure) return@withContext Result.failure(Exception("Failed to update order"))

        val sendOrderResult = orderStore.sendOrderReceipt(selectedSite.get(), orderId)
        return@withContext if (sendOrderResult.isError) {
            Result.failure(Exception("Failed to send order receipt"))
        } else {
            Result.success(Unit)
        }
    }

    private suspend fun getOrderById(orderId: Long) =
        orderStore.getOrderByIdAndSite(orderId, selectedSite.get())?.let {
            orderMapper.toAppModel(it)
        }
}
