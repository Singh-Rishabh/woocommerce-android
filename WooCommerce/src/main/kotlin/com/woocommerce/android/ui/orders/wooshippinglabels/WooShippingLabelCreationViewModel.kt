package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptions
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelCreationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val orderDetailRepository: OrderDetailRepository,
    private val getShippableItems: GetShippableItems
) : ScopedViewModel(savedState) {
    private val navArgs: WooShippingLabelCreationFragmentArgs by savedState.navArgs()
    private val storeOptions = StoreOptions(
        currencySymbol = "$",
        dimensionUnit = "cm",
        weightUnit = "kg",
        originCountry = "US"
    )

    val viewState: MutableStateFlow<WooShippingViewState> = MutableStateFlow(WooShippingViewState.Loading)

    init {
        launch {
            orderDetailRepository.getOrderById(navArgs.orderId)?.let {
                val shippableItems = getShippableItems(
                    order = it,
                    storeOptions = storeOptions
                )
                viewState.value = WooShippingViewState.DataState(shippableItems)
            }
        }
    }

    fun onSelectPackageClicked() {
        triggerEvent(StartPackageSelection)
    }

    fun onPurchaseShippingLabel() {
        triggerEvent(LabelPurchased)
    }

    data object StartPackageSelection : Event()
    data object LabelPurchased : Event()

    sealed class WooShippingViewState {
        data object Loading : WooShippingViewState()
        data class DataState(
            val shippableItems: ShippableItems
        ) : WooShippingViewState()
    }
}
