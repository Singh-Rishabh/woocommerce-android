package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.extensions.formatToString
import com.woocommerce.android.extensions.sumByFloat
import com.woocommerce.android.model.Address
import com.woocommerce.android.model.Order
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.PackageSelectionState.NotAvailable
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.update

@HiltViewModel
class WooShippingLabelCreationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val orderDetailRepository: OrderDetailRepository,
    private val getShippableItems: GetShippableItems,
    private val currencyFormatter: CurrencyFormatter,
    private val observeOriginAddresses: ObserveOriginAddresses,
    private val getShippingRates: GetShippingRates
) : ScopedViewModel(savedState) {
    private val navArgs: WooShippingLabelCreationFragmentArgs by savedState.navArgs()
    private val mockStoreOptions = StoreOptionsModel(
        currencySymbol = "$",
        dimensionUnit = "cm",
        weightUnit = "kg",
        originCountry = "US"
    )

    private val shippableItems = MutableStateFlow<List<ShippableItemModel>>(emptyList())
    private val selectedPackage = MutableStateFlow<PackageSelectionState>(NotAvailable)
    private val storeOptions = MutableStateFlow(mockStoreOptions)
    private val selectedRatesSortOrder = MutableStateFlow(ShippingSortOption.FASTEST)
    private val refreshShippingRates = MutableSharedFlow<Unit>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val shippingRates =
        combine(
            selectedPackage,
            selectedRatesSortOrder,
            refreshShippingRates.onStart { emit(Unit) }
        ) { selectedPackage, sortOrder, _ ->
            when (selectedPackage) {
                is NotAvailable -> PackageData.EMPTY
                is PackageSelectionState.Data -> selectedPackage.selectedPackage
            }.let { Pair(it, sortOrder) }
        }.flatMapLatest {
            val (selectedPackage, sortOrder) = it
            refreshShippingRates(selectedPackage, sortOrder)
        }

    val viewState: MutableStateFlow<WooShippingViewState> = MutableStateFlow(WooShippingViewState.Loading)

    init {
        launch { observeShippingLabelInformation() }
    }

    private fun refreshShippingRates(
        selectedPackage: PackageData,
        sortOrder: ShippingSortOption
    ) = flow {
        emit(ShippingRatesState.Loading(sortOrder))
        val shippingRatesResult = getShippingRates(selectedPackage, sortOrder)
        if (shippingRatesResult.isSuccess) {
            emit(ShippingRatesState.DataState(sortOrder, shippingRatesResult.getOrThrow()))
        } else {
            emit(ShippingRatesState.Error)
        }
    }

    private suspend fun observeShippingLabelInformation() {
        combine(
            storeOptions,
            flowOf(orderDetailRepository.getOrderById(navArgs.orderId)),
            observeOriginAddresses(),
            shippingRates
        ) { storeOptions, order, originAddresses, shippingRates ->
            val selectedOriginAddress = getSelectedOriginAddress(originAddresses)
            if (order == null || selectedOriginAddress == null) {
                return@combine WooShippingViewState.Error
            }
            val items = getShippableItems(order)
            shippableItems.value = items

            val shippableItemsUI = items.map { item -> item.toUIModel(currencyFormatter, storeOptions) }
            val formattedTotalPrice = getTotalPrice(items)
            val formattedTotalWeight = getTotalWeight(items, storeOptions)

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
                ),
                shippingRates = shippingRates
            )
        }.collect {
            viewState.value = it
        }
    }

    private fun getSelectedOriginAddress(originAddresses: List<OriginShippingAddress>): OriginShippingAddress? {
        return (viewState as? WooShippingViewState.DataState)?.let {
            it.shippingAddresses.shipFrom
        } ?: originAddresses.firstOrNull { it.isDefault } ?: originAddresses.firstOrNull()
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

    fun onRefreshShippingRates() {
        launch {
            refreshShippingRates.emit(Unit)
        }
    }

    private fun getTotalPrice(items: List<ShippableItemModel>): String {
        val totalPrice = items.sumOf { it.price }
        val formattedTotalPrice = items.firstOrNull()?.currency?.let {
            currencyFormatter.formatCurrency(totalPrice, it)
        } ?: currencyFormatter.formatCurrency(totalPrice)
        return formattedTotalPrice
    }

    private fun getTotalWeight(items: List<ShippableItemModel>, storeOptions: StoreOptionsModel): String {
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

    fun onSelectedRateSortOrderChanged(option: ShippingSortOption) {
        selectedRatesSortOrder.value = option
    }

    fun onPackageSelected(packageData: PackageData) {
        selectedPackage.update { content ->
            when (content) {
                is NotAvailable -> PackageSelectionState.Data(
                    selectedPackage = packageData,
                    totalWeight = packageData.weight
                )
                is PackageSelectionState.Data -> content.copy(selectedPackage = packageData)
            }
        }
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
            val shippingRates: ShippingRatesState,
        ) : WooShippingViewState()
    }

    sealed class ShippingRatesState {
        data object NoAvailable : ShippingRatesState()
        data object Error : ShippingRatesState()

        data class Loading(
            val selectedRatesSortOrder: ShippingSortOption
        ) : ShippingRatesState()

        data class DataState(
            val selectedRatesSortOrder: ShippingSortOption,
            val shippingRates: Map<Carrier, List<ShippingRateUI>>
        ) : ShippingRatesState()
    }

    sealed class PackageSelectionState {
        data object NotAvailable : PackageSelectionState()
        data class Data(
            val selectedPackage: PackageData,
            val totalWeight: String
        ) : PackageSelectionState()
    }
}

data class WooShippingAddresses(
    val shipFrom: OriginShippingAddress,
    val shipTo: Address,
    val originAddresses: List<OriginShippingAddress>
)
