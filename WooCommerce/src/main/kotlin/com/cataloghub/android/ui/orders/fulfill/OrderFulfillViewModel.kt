package com.cataloghub.android.ui.orders.fulfill

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.Callback
import com.cataloghub.android.AppPrefs
import com.cataloghub.android.R
import com.cataloghub.android.R.string
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsEvent.ORDER_TRACKING_ADD
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.model.Order
import com.cataloghub.android.model.Order.Item
import com.cataloghub.android.model.OrderShipmentTracking
import com.cataloghub.android.model.getNonRefundedProducts
import com.cataloghub.android.tools.NetworkStatus
import com.cataloghub.android.ui.orders.OrderNavigationTarget.AddOrderShipmentTracking
import com.cataloghub.android.ui.orders.OrderStatusUpdateSource
import com.cataloghub.android.ui.orders.details.OrderDetailRepository
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowUndoSnackbar
import com.cataloghub.android.viewmodel.ResourceProvider
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.wordpress.android.fluxc.store.WCOrderStore.OnOrderChanged
import javax.inject.Inject

@HiltViewModel
class OrderFulfillViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val appPrefs: AppPrefs,
    private val networkStatus: NetworkStatus,
    private val resourceProvider: ResourceProvider,
    private val repository: OrderDetailRepository,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
) : ScopedViewModel(savedState) {
    companion object {
        const val KEY_ORDER_FULFILL_RESULT = "key_order_fulfill_result"
        const val KEY_REFRESH_SHIPMENT_TRACKING_RESULT = "key_refresh_shipment_tracking_result"
    }

    private val navArgs: OrderFulfillFragmentArgs by savedState.navArgs()

    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val viewStateData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateData

    private val _productList = MutableLiveData<List<Item>>()
    val productList: LiveData<List<Item>> = _productList

    private val _shipmentTrackings = MutableLiveData<List<OrderShipmentTracking>>()
    val shipmentTrackings: LiveData<List<OrderShipmentTracking>> = _shipmentTrackings

    // Keep track of the deleted shipment tracking number in case
    // the request to server fails, we need to display an error message
    // and add the deleted tracking number back to the list
    private var deletedOrderShipmentTrackingSet = mutableSetOf<String>()

    final var order: Order
        get() = requireNotNull(viewState.order)
        set(value) {
            viewState = viewState.copy(
                order = value
            )
        }

    init {
        start()
    }

    final fun start() {
        launch {
            val order = repository.getOrderById(navArgs.orderId)
            order?.let {
                displayOrderDetails(it)
                displayOrderProducts(it)
                displayShipmentTrackings()
            }
        }
    }

    private fun displayOrderDetails(order: Order) {
        viewState = viewState.copy(
            order = order,
            toolbarTitle = resourceProvider.getString(R.string.order_fulfill_title)
        )
    }

    private fun displayOrderProducts(order: Order) {
        val products = repository.getOrderRefunds(navArgs.orderId).getNonRefundedProducts(order.items)
        _productList.value = products
    }

    private fun displayShipmentTrackings() {
        val isShippingLabelAvailable = repository.getOrderShippingLabels(navArgs.orderId).isNotEmpty()
        val trackingAvailable = appPrefs.isTrackingExtensionAvailable() &&
            !hasVirtualProductsOnly() && !isShippingLabelAvailable
        viewState = viewState.copy(isShipmentTrackingAvailable = trackingAvailable)
        if (trackingAvailable) {
            _shipmentTrackings.value = repository.getOrderShipmentTrackings(navArgs.orderId)
        }
    }

    fun hasVirtualProductsOnly(): Boolean {
        return if (order.items.isNotEmpty()) {
            val remoteProductIds = order.getProductIds()
            repository.hasVirtualProductsOnly(remoteProductIds)
        } else {
            false
        }
    }

    fun onMarkOrderCompleteButtonClicked() {
        if (networkStatus.isConnected()) {
            triggerEvent(
                ExitWithResult(
                    data = OrderStatusUpdateSource.FullFillScreen(oldStatus = order.status.value),
                    key = KEY_ORDER_FULFILL_RESULT
                )
            )
        } else {
            triggerEvent(ShowSnackbar(string.offline_error))
        }
    }

    fun onAddShipmentTrackingClicked() {
        triggerEvent(
            AddOrderShipmentTracking(
                orderId = order.id,
                orderTrackingProvider = appPrefs.getSelectedShipmentTrackingProviderName(),
                isCustomProvider = appPrefs.getIsSelectedShipmentTrackingProviderCustom()
            )
        )
    }

    fun onNewShipmentTrackingAdded(shipmentTracking: OrderShipmentTracking) {
        analyticsTrackerWrapper.track(
            ORDER_TRACKING_ADD,
            mapOf(
                AnalyticsTracker.KEY_ID to order.id,
                AnalyticsTracker.KEY_STATUS to order.status,
                AnalyticsTracker.KEY_CARRIER to shipmentTracking.trackingProvider
            )
        )
        viewState = viewState.copy(shouldRefreshShipmentTracking = true)
        _shipmentTrackings.value = repository.getOrderShipmentTrackings(navArgs.orderId)
    }

    fun onDeleteShipmentTrackingClicked(trackingNumber: String) {
        if (networkStatus.isConnected()) {
            repository.getOrderShipmentTrackingByTrackingNumber(
                navArgs.orderId,
                trackingNumber
            )?.let { deletedShipmentTracking ->
                deletedOrderShipmentTrackingSet.add(trackingNumber)

                val shipmentTrackings = _shipmentTrackings.value?.toMutableList() ?: mutableListOf()
                shipmentTrackings.remove(deletedShipmentTracking)
                _shipmentTrackings.value = shipmentTrackings

                triggerEvent(
                    ShowUndoSnackbar(
                        message = resourceProvider.getString(string.order_shipment_tracking_delete_snackbar_msg),
                        undoAction = { onDeleteShipmentTrackingReverted(deletedShipmentTracking) },
                        dismissAction = object : Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                if (event != DISMISS_EVENT_ACTION) {
                                    // delete the shipment only if user has not clicked on the undo snackbar
                                    deleteOrderShipmentTracking(deletedShipmentTracking)
                                }
                            }
                        }
                    )
                )
            }
        } else {
            triggerEvent(ShowSnackbar(string.offline_error))
        }
    }

    private fun onDeleteShipmentTrackingReverted(shipmentTracking: OrderShipmentTracking) {
        deletedOrderShipmentTrackingSet.remove(shipmentTracking.trackingNumber)
        val shipmentTrackings = _shipmentTrackings.value?.toMutableList() ?: mutableListOf()
        shipmentTrackings.add(shipmentTracking)
        _shipmentTrackings.value = shipmentTrackings
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun deleteOrderShipmentTracking(shipmentTracking: OrderShipmentTracking) {
        launch {
            val onOrderChanged = repository.deleteOrderShipmentTracking(
                navArgs.orderId,
                shipmentTracking.toDataModel()
            )
            if (!onOrderChanged.isError) {
                analyticsTrackerWrapper.track(AnalyticsEvent.ORDER_TRACKING_DELETE_SUCCESS)
                viewState = viewState.copy(shouldRefreshShipmentTracking = true)
                triggerEvent(ShowSnackbar(string.order_shipment_tracking_delete_success))
            } else {
                analyticsTrackerWrapper.track(
                    AnalyticsEvent.ORDER_TRACKING_DELETE_FAILED,
                    prepareTracksEventsDetails(onOrderChanged)
                )
                onDeleteShipmentTrackingReverted(shipmentTracking)
                triggerEvent(ShowSnackbar(string.order_shipment_tracking_delete_error))
            }
        }
    }

    fun onBackButtonClicked() {
        if (viewState.shouldRefreshShipmentTracking) {
            triggerEvent(ExitWithResult(true, key = KEY_REFRESH_SHIPMENT_TRACKING_RESULT))
        } else {
            triggerEvent(Exit)
        }
    }

    private fun prepareTracksEventsDetails(event: OnOrderChanged) = mapOf(
        AnalyticsTracker.KEY_ERROR_CONTEXT to this::class.java.simpleName,
        AnalyticsTracker.KEY_ERROR_TYPE to event.error.type.toString(),
        AnalyticsTracker.KEY_ERROR_DESC to event.error.message
    )

    @Parcelize
    data class ViewState(
        val order: Order? = null,
        val toolbarTitle: String? = null,
        val isShipmentTrackingAvailable: Boolean? = null,
        val shouldRefreshShipmentTracking: Boolean = false
    ) : Parcelable
}
