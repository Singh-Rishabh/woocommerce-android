package com.cataloghub.android.ui.orders.tracking

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsEvent.ORDER_SHIPMENT_TRACKING_CARRIER_SELECTED
import com.cataloghub.android.analytics.AnalyticsEvent.ORDER_SHIPMENT_TRACKING_CUSTOM_PROVIDER_SELECTED
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.model.OrderShipmentProvider
import com.cataloghub.android.ui.orders.details.OrderDetailRepository
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.ResourceProvider
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class AddOrderTrackingProviderListViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val shipmentProvidersRepository: OrderShipmentProvidersRepository,
    private val orderDetailRepository: OrderDetailRepository,
    private val resourceProvider: ResourceProvider
) : ScopedViewModel(savedState) {
    private val navArgs: AddOrderTrackingProviderListFragmentArgs by savedState.navArgs()

    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val trackingProviderListViewStateData = LiveDataDelegate(
        savedState = savedState,
        initialValue = ViewState()
    )
    private var trackingProviderListViewState by trackingProviderListViewStateData

    private var providersList = emptyList<OrderShipmentProvider>()

    val currentSelectedProvider: String
        get() = navArgs.selectedProvider

    val countryCode: String?
        get() = orderDetailRepository.getStoreCountryCode()

    init {
        fetchProviders()
    }

    private fun fetchProviders() {
        trackingProviderListViewState = trackingProviderListViewState.copy(showSkeleton = true)
        launch {
            val shipmentProviders = shipmentProvidersRepository.fetchOrderShipmentProviders(navArgs.orderId)
            trackingProviderListViewState = trackingProviderListViewState.copy(showSkeleton = false)
            when {
                shipmentProviders == null -> {
                    triggerEvent(ShowSnackbar(R.string.order_shipment_tracking_provider_list_error_fetch_generic))
                }
                shipmentProviders.isEmpty() -> {
                    triggerEvent(ShowSnackbar(R.string.order_shipment_tracking_provider_list_error_empty_list))
                }
                else -> {
                    AnalyticsTracker.track(AnalyticsEvent.ORDER_TRACKING_PROVIDERS_LOADED)
                    providersList = shipmentProviders
                    trackingProviderListViewState = trackingProviderListViewState.copy(
                        providersList = shipmentProviders
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        trackingProviderListViewState = trackingProviderListViewState.copy(query = query)
        val filteredList = if (query.isEmpty()) {
            providersList
        } else {
            providersList.filter {
                it.carrierName.contains(query) ||
                    it.country.contains(query) ||
                    it.carrierLink.contains(query)
            }
        }
        trackingProviderListViewState = trackingProviderListViewState.copy(providersList = filteredList)
    }

    fun onProviderSelected(provider: OrderShipmentProvider) {
        val isCustom = provider.carrierName ==
            resourceProvider.getString(R.string.order_shipment_tracking_custom_provider_section_name)

        if (isCustom) {
            AnalyticsTracker.track(ORDER_SHIPMENT_TRACKING_CUSTOM_PROVIDER_SELECTED)
        } else {
            AnalyticsTracker.track(
                ORDER_SHIPMENT_TRACKING_CARRIER_SELECTED,
                mapOf(AnalyticsTracker.KEY_OPTION to provider.carrierName)
            )
        }
        val carrier = Carrier(
            name = if (isCustom) "" else provider.carrierName,
            isCustom = isCustom
        )
        triggerEvent(ExitWithResult(carrier))
    }

    @Parcelize
    data class ViewState(
        val showSkeleton: Boolean = false,
        val providersList: List<OrderShipmentProvider> = emptyList(),
        val query: String = ""
    ) : Parcelable
}
