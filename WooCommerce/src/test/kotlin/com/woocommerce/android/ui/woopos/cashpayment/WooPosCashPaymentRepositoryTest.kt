package com.woocommerce.android.ui.woopos.cashpayment

import com.woocommerce.android.model.Order
import com.woocommerce.android.model.OrderMapper
import com.woocommerce.android.tools.SelectedSite
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.model.OrderEntity
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.model.WCOrderStatusModel
import org.wordpress.android.fluxc.model.gateways.WCGatewayModel
import org.wordpress.android.fluxc.store.WCGatewayStore
import org.wordpress.android.fluxc.store.WCOrderStore
import org.wordpress.android.fluxc.store.WCOrderStore.OnOrderChanged
import org.wordpress.android.fluxc.store.WCOrderStore.UpdateOrderResult

class WooPosCashPaymentRepositoryTest {

    private val selectedSite: SelectedSite = mock()
    private val orderStore: WCOrderStore = mock()
    private val orderMapper: OrderMapper = mock()
    private val gatewayStore: WCGatewayStore = mock()

    private lateinit var repository: WooPosCashPaymentRepository

    @Before
    fun setUp() {
        repository = WooPosCashPaymentRepository(
            selectedSite,
            orderStore,
            orderMapper,
            gatewayStore
        )
    }

    @Test
    fun `given valid orderId and site, when getOrderById, then return mapped order`() = runTest {
        // GIVEN
        val orderId = 123L
        val site: SiteModel = mock()
        val mockOrder: OrderEntity = mock()
        val mappedOrder: Order = mock()

        whenever(selectedSite.get()).thenReturn(site)
        whenever(orderStore.getOrderByIdAndSite(orderId, site)).thenReturn(mockOrder)
        whenever(orderMapper.toAppModel(mockOrder)).thenReturn(mappedOrder)

        // WHEN
        val result = repository.getOrderById(orderId)

        // THEN
        assertThat(result).isEqualTo(mappedOrder)
        verify(orderStore).getOrderByIdAndSite(orderId, site)
        verify(orderMapper).toAppModel(mockOrder)
    }

    @Test
    fun `given invalid orderId, when getOrderById, then return null`() = runTest {
        // GIVEN
        val orderId = 456L
        val site: SiteModel = mock()

        whenever(selectedSite.get()).thenReturn(site)
        whenever(orderStore.getOrderByIdAndSite(orderId, site)).thenReturn(null)

        // WHEN
        val result = repository.getOrderById(orderId)

        // THEN
        assertThat(result).isNull()
        verify(orderStore).getOrderByIdAndSite(orderId, site)
    }

    @Test
    fun `given valid orderId, when completeOrder, then return success`() = runTest {
        // GIVEN
        val orderId = 123L
        val site: SiteModel = mock()
        val gatewayTitle = "Pay in Person"
        val codGateway: WCGatewayModel = mock { on { title }.thenReturn(gatewayTitle) }
        val statusModel = WCOrderStatusModel(statusKey = Order.Status.Completed.value)
        val updateResult = UpdateOrderResult.RemoteUpdateResult(mock { on { isError }.thenReturn(false) })

        whenever(selectedSite.get()).thenReturn(site)
        whenever(gatewayStore.getGateway(site, "cod")).thenReturn(codGateway)
        whenever(orderStore.getOrderStatusForSiteAndKey(site, Order.Status.Completed.value)).thenReturn(statusModel)
        whenever(
            orderStore.updateOrderStatusAndPaymentMethod(
                orderId = orderId,
                site = site,
                newStatus = statusModel,
                newPaymentMethodId = "cod",
                newPaymentMethodTitle = gatewayTitle
            )
        ).thenReturn(flowOf(updateResult))

        // WHEN
        val result = repository.completeOrder(orderId)

        // THEN
        assertThat(result.isSuccess).isTrue()
        verify(orderStore).updateOrderStatusAndPaymentMethod(
            orderId = orderId,
            site = site,
            newStatus = statusModel,
            newPaymentMethodId = "cod",
            newPaymentMethodTitle = gatewayTitle
        )
    }

    @Test
    fun `given valid orderId, when completeOrder, then return failure`() = runTest {
        // GIVEN
        val orderId = 123L
        val site: SiteModel = mock()
        val gatewayTitle = "Pay in Person"
        val codGateway: WCGatewayModel = mock { on { title }.thenReturn(gatewayTitle) }
        val statusModel = WCOrderStatusModel(statusKey = Order.Status.Completed.value)
        val errorMessage = "Order update failed"
        val updateResult = UpdateOrderResult.RemoteUpdateResult(
            event = OnOrderChanged(
                orderError = WCOrderStore.OrderError(
                    message = errorMessage
                )
            )
        )

        whenever(selectedSite.get()).thenReturn(site)
        whenever(gatewayStore.getGateway(site, "cod")).thenReturn(codGateway)
        whenever(orderStore.getOrderStatusForSiteAndKey(site, Order.Status.Completed.value)).thenReturn(statusModel)
        whenever(
            orderStore.updateOrderStatusAndPaymentMethod(
                orderId = orderId,
                site = site,
                newStatus = statusModel,
                newPaymentMethodId = "cod",
                newPaymentMethodTitle = gatewayTitle
            )
        ).thenReturn(flowOf(updateResult))

        // WHEN
        val result = repository.completeOrder(orderId)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
        verify(orderStore).updateOrderStatusAndPaymentMethod(
            orderId = orderId,
            site = site,
            newStatus = statusModel,
            newPaymentMethodId = "cod",
            newPaymentMethodTitle = gatewayTitle
        )
    }

    @Test
    fun `given no status model, when completeOrder, then default status is used`() = runTest {
        // GIVEN
        val orderId = 789L
        val site: SiteModel = mock()
        val gatewayTitle = "Pay in Person"
        val codGateway: WCGatewayModel = mock { on { title }.thenReturn(gatewayTitle) }
        val updateResult = UpdateOrderResult.RemoteUpdateResult(mock { on { isError }.thenReturn(false) })

        whenever(selectedSite.get()).thenReturn(site)
        whenever(gatewayStore.getGateway(site, "cod")).thenReturn(codGateway)
        whenever(orderStore.getOrderStatusForSiteAndKey(site, Order.Status.Completed.value)).thenReturn(null)
        whenever(
            orderStore.updateOrderStatusAndPaymentMethod(
                orderId = orderId,
                site = site,
                newStatus = WCOrderStatusModel(statusKey = Order.Status.Completed.value).apply {
                    label = Order.Status.Completed.value
                },
                newPaymentMethodId = "cod",
                newPaymentMethodTitle = gatewayTitle
            )
        ).thenReturn(flowOf(updateResult))

        // WHEN
        val result = repository.completeOrder(orderId)

        // THEN
        assertThat(result.isSuccess).isTrue()
        verify(orderStore).updateOrderStatusAndPaymentMethod(
            orderId = orderId,
            site = site,
            newStatus = WCOrderStatusModel(statusKey = Order.Status.Completed.value).apply {
                label = Order.Status.Completed.value
            },
            newPaymentMethodId = "cod",
            newPaymentMethodTitle = gatewayTitle
        )
    }
}
