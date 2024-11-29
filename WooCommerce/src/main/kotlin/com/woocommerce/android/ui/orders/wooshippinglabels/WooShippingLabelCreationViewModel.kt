package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.extensions.formatToString
import com.woocommerce.android.extensions.sumByFloat
import com.woocommerce.android.model.Address
import com.woocommerce.android.model.Order
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelCreationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val orderDetailRepository: OrderDetailRepository,
    private val getShippableItems: GetShippableItems,
    private val currencyFormatter: CurrencyFormatter,
    private val observeOriginAddresses: ObserveOriginAddresses
) : ScopedViewModel(savedState) {
    private val navArgs: WooShippingLabelCreationFragmentArgs by savedState.navArgs()
    private val storeOptions = StoreOptionsModel(
        currencySymbol = "$",
        dimensionUnit = "cm",
        weightUnit = "kg",
        originCountry = "US"
    )

    private val shippableItems = MutableStateFlow<List<ShippableItemModel>>(emptyList())
    val viewState: MutableStateFlow<WooShippingViewState> = MutableStateFlow(WooShippingViewState.Loading)

    init {
        launch { observeShippingLabelInformation() }
    }

    private suspend fun observeShippingLabelInformation() {
        flowOf(orderDetailRepository.getOrderById(navArgs.orderId))
            .combine(observeOriginAddresses()) { order, originAddresses ->
                if (order == null) {
                    return@combine WooShippingViewState.Error
                }
                val selectedOriginAddress = getSelectedOriginAddress(originAddresses)
                val items = getShippableItems(order)
                shippableItems.value = items

                val shippableItemsUI = items.map { item -> item.toUIModel(currencyFormatter, storeOptions) }
                val formattedTotalPrice = getTotalPrice(items)
                val formattedTotalWeight = getTotalWeight(items)

                val shippingLineSummary = getShippingLinesSummary(order)

                return@combine WooShippingViewState.DataState(
                    shippableItems = ShippableItemsUI(
                        shippableItems = shippableItemsUI,
                        formattedTotalWeight = formattedTotalWeight,
                        formattedTotalPrice = formattedTotalPrice
                    ),
                    shippingLines = shippingLineSummary,
                    shippingAddresses = WooShippingAddresses(
                        shipFrom = selectedOriginAddress,
                        originAddresses = originAddresses,
                        shipTo = order.shippingAddress
                    )
                )
            }.collect {
                viewState.value = it
            }
    }

    private fun getSelectedOriginAddress(originAddresses: List<OriginShippingAddress>): OriginShippingAddress {
        return (viewState as? WooShippingViewState.DataState)?.let {
            it.shippingAddresses.shipFrom
        } ?: originAddresses.firstOrNull { it.isDefault } ?: originAddresses.first()
    }

    fun onShippingFromAddressChange(address: OriginShippingAddress) {
        (viewState.value as? WooShippingViewState.DataState)?.let { currentData ->
            viewState.value = currentData.copy(
                shippingAddresses = currentData.shippingAddresses.copy(
                    shipFrom = address
                )
            )
        }
    }

    fun onShippingToAddressChange(address: Address) {
        (viewState.value as? WooShippingViewState.DataState)?.let { currentData ->
            viewState.value = currentData.copy(
                shippingAddresses = currentData.shippingAddresses.copy(
                    shipTo = address
                )
            )
        }
    }

    private fun getTotalPrice(items: List<ShippableItemModel>): String {
        val totalPrice = items.sumOf { it.price }
        val formattedTotalPrice = items.firstOrNull()?.currency?.let {
            currencyFormatter.formatCurrency(totalPrice, it)
        } ?: currencyFormatter.formatCurrency(totalPrice)
        return formattedTotalPrice
    }

    private fun getTotalWeight(items: List<ShippableItemModel>): String {
        val totalWeight = items.sumByFloat { it.weight * it.quantity }
        return "${totalWeight.formatToString()} ${storeOptions.weightUnit}"
    }

    private fun getShippingLinesSummary(order: Order): List<ShippingLineSummaryUI> {
        return order.shippingLines.map {
            ShippingLineSummaryUI(
                title = it.methodTitle,
                amount = currencyFormatter.formatCurrency(it.total, order.currency)
            )
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
        data object Error : WooShippingViewState()
        data object Loading : WooShippingViewState()
        data class DataState(
            val shippableItems: ShippableItemsUI,
            val shippingLines: List<ShippingLineSummaryUI>,
            val shippingAddresses: WooShippingAddresses,
        ) : WooShippingViewState()
    }
}

data class WooShippingAddresses(
    val shipFrom: OriginShippingAddress,
    val shipTo: Address?,
    val originAddresses: List<OriginShippingAddress>
)
