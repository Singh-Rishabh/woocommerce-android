package com.cataloghub.android.ui.payments.refunds

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.extensions.calculateTotals
import com.cataloghub.android.extensions.isCashPayment
import com.cataloghub.android.model.Order
import com.cataloghub.android.model.OrderMapper
import com.cataloghub.android.model.Refund
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.payments.refunds.RefundProductListAdapter.ProductRefundListItem
import com.cataloghub.android.ui.products.addons.AddonRepository
import com.cataloghub.android.util.CoroutineDispatchers
import com.cataloghub.android.util.CurrencyFormatter
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.ResourceProvider
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.wordpress.android.fluxc.store.WCOrderStore
import org.wordpress.android.fluxc.store.WCRefundStore
import org.wordpress.android.mediapicker.util.map
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class RefundDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    orderStore: WCOrderStore,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val selectedSite: SelectedSite,
    private val currencyFormatter: CurrencyFormatter,
    private val resourceProvider: ResourceProvider,
    private val addonsRepository: AddonRepository,
    private val refundStore: WCRefundStore,
    private val orderMapper: OrderMapper,
) : ScopedViewModel(savedState) {
    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val viewStateData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateData

    private val _refundItems = MutableLiveData<List<ProductRefundListItem>>()
    val refundItems: LiveData<List<ProductRefundListItem>> = _refundItems.map {
        it.apply { checkAddonAvailability(this) }
    }

    private lateinit var formatCurrency: (BigDecimal) -> String

    private val navArgs: RefundDetailFragmentArgs by savedState.navArgs()

    init {
        launch {
            val orderModel = orderStore.getOrderByIdAndSite(navArgs.orderId, selectedSite.get())
            orderModel?.let { orderMapper.toAppModel(it) }?.let { order ->
                formatCurrency = currencyFormatter.buildBigDecimalFormatter(order.currency)
                if (navArgs.refundId > 0) {
                    refundStore.getRefund(selectedSite.get(), navArgs.orderId, navArgs.refundId)
                        ?.toAppModel()?.let { refund ->
                            displayRefundDetails(refund, order)
                        }
                } else {
                    val refunds = refundStore.getAllRefunds(selectedSite.get(), navArgs.orderId).map { it.toAppModel() }
                    displayRefundedProducts(order, refunds)
                }
            }
        }
    }

    fun onViewOrderedAddonButtonTapped(orderItem: Order.Item) {
        AnalyticsTracker.track(AnalyticsEvent.PRODUCT_ADDONS_REFUND_DETAIL_VIEW_PRODUCT_ADDONS_TAPPED)
        triggerEvent(
            ViewOrderedAddons(
                navArgs.orderId,
                orderItem.itemId,
                orderItem.productId
            )
        )
    }

    private fun displayRefundedProducts(order: Order, refunds: List<Refund>) {
        val groupedRefunds = refunds.flatMap { it.items }.groupBy { it.orderItemId }
        val refundedProducts = groupedRefunds.keys.mapNotNull { id ->
            order.items.firstOrNull { it.itemId == id }?.let { item ->
                groupedRefunds[id]?.sumOf { it.quantity }?.let { quantity ->
                    ProductRefundListItem(item, quantity = quantity)
                }
            }
        }

        viewState = viewState.copy(
            currency = order.currency,
            screenTitle = resourceProvider.getString(R.string.orderdetail_refunded_products),
            areItemsVisible = true,
            areDetailsVisible = false
        )

        _refundItems.value = refundedProducts
    }

    private fun displayRefundDetails(refund: Refund, order: Order) {
        if (refund.items.isNotEmpty()) {
            val items = refund.items.map { refundItem ->
                ProductRefundListItem(
                    order.items.first { it.itemId == refundItem.orderItemId },
                    quantity = refundItem.quantity
                )
            }

            val (subtotal, taxes) = items.calculateTotals()
            viewState = viewState.copy(
                currency = order.currency,
                areItemsVisible = true,
                subtotal = formatCurrency(subtotal),
                taxes = formatCurrency(taxes)
            )

            _refundItems.value = items
        } else {
            viewState = viewState.copy(areItemsVisible = false)
        }

        viewState = viewState.copy(
            screenTitle = "${resourceProvider.getString(R.string.order_refunds_refund)} #${refund.id}",
            refundAmount = formatCurrency(refund.amount),
            refundMethod = resourceProvider.getString(
                R.string.order_refunds_refunded_via,
                getRefundMethod(order, refund)
            ),
            refundReason = refund.reason,
            areDetailsVisible = true
        )
    }

    private fun getRefundMethod(order: Order, refund: Refund): String {
        val manualRefund = resourceProvider.getString(R.string.order_refunds_manual_refund)
        return if (order.paymentMethodTitle.isNotBlank() &&
            (refund.automaticGatewayRefund || order.paymentMethod.isCashPayment)
        ) {
            order.paymentMethodTitle
        } else if (order.paymentMethodTitle.isNotBlank()) {
            "$manualRefund - ${order.paymentMethodTitle}"
        } else {
            manualRefund
        }
    }

    private fun checkAddonAvailability(refunds: List<ProductRefundListItem>) {
        launch(coroutineDispatchers.computation) {
            refunds.forEach { it.orderItem.containsAddons = addonsRepository.containsAddonsFrom(it.orderItem) }
        }
    }

    @Parcelize
    data class ViewState(
        val screenTitle: String? = null,
        val refundAmount: String? = null,
        val subtotal: String? = null,
        val taxes: String? = null,
        val refundMethod: String? = null,
        val refundReason: String? = null,
        val currency: String? = null,
        val areItemsVisible: Boolean? = null,
        val areDetailsVisible: Boolean? = null
    ) : Parcelable

    data class ViewOrderedAddons(
        val remoteOrderID: Long,
        val orderItemID: Long,
        val addonsProductID: Long
    ) : Event()
}
