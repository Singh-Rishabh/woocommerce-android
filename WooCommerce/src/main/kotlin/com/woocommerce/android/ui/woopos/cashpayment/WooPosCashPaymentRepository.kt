package com.woocommerce.android.ui.woopos.cashpayment

import com.woocommerce.android.model.Order
import com.woocommerce.android.model.OrderMapper
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.util.WooLog.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.model.WCOrderStatusModel
import org.wordpress.android.fluxc.store.WCGatewayStore
import org.wordpress.android.fluxc.store.WCOrderStore
import javax.inject.Inject

class WooPosCashPaymentRepository @Inject constructor(
    private val selectedSite: SelectedSite,
    private val orderStore: WCOrderStore,
    private val orderMapper: OrderMapper,
    private val gatewayStore: WCGatewayStore,
) {
    suspend fun getOrderById(orderId: Long) = withContext(Dispatchers.IO) {
        orderStore.getOrderByIdAndSite(orderId, selectedSite.get())?.let {
            orderMapper.toAppModel(it)
        }
    }

    suspend fun completeOrder(orderId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val codGateway = gatewayStore.getGateway(selectedSite.get(), CASH_ON_DELIVERY_PAYMENT_TYPE)

        val statusModel = orderStore.getOrderStatusForSiteAndKey(
            selectedSite.get(),
            Order.Status.Completed.value
        ) ?: WCOrderStatusModel(statusKey = Order.Status.Completed.value).apply {
            label = statusKey
        }

        orderStore.updateOrderStatusAndPaymentMethod(
            orderId = orderId,
            site = selectedSite.get(),
            newStatus = statusModel,
            newPaymentMethodId = CASH_ON_DELIVERY_PAYMENT_TYPE,
            codGateway?.title ?: "Pay in Person",
        )
            .filterIsInstance<WCOrderStore.UpdateOrderResult.RemoteUpdateResult>()
            .map { result ->
                if (result.event.isError) {
                    WooLog.e(T.POS, "Order completion failed - ${result.event.error.message}")
                    Result.failure(Exception(result.event.error.message))
                } else {
                    Result.success(Unit)
                }
            }.first()
    }

    private companion object {
        const val CASH_ON_DELIVERY_PAYMENT_TYPE = "cod"
    }
}
