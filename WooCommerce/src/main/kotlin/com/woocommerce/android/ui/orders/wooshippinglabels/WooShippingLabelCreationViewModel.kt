package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.extensions.formatToString
import com.woocommerce.android.extensions.sumByFloat
import com.woocommerce.android.model.Address
import com.woocommerce.android.model.Order
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.PackageSelectionState.DataAvailable
import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.PackageSelectionState.NotSelected
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.domain.GetShippingRates
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.CarrierUI
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.ShippingRateUI
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.ShippingSortOption
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val order = MutableStateFlow<Order?>(null)
    private val shippingAddresses = MutableStateFlow<WooShippingAddresses?>(null)
    private val storeOptions = MutableStateFlow<StoreOptionsModel?>(null)
    private val shippableItems = MutableStateFlow<List<ShippableItemModel>>(emptyList())

    private val selectedRatesSortOrder = MutableStateFlow(ShippingSortOption.FASTEST)
    private val refreshShippingRates = MutableSharedFlow<Unit>()

    private val packageSelection = MutableStateFlow<PackageSelectionState>(NotSelected)

    private val cheapestComparator = Comparator<ShippingRateUI> { r1, r2 -> r1.price.compareTo(r2.price) }
    private val fastestComparator = Comparator<ShippingRateUI> { r1, r2 -> r1.deliveryDays.compareTo(r2.deliveryDays) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val shippingRates =
        combine(
            packageSelection,
            shippingAddresses,
            refreshShippingRates.onStart { emit(Unit) }
        ) { packageSelection, addresses, _ ->
             Pair(packageSelection, addresses)
        }.flatMapLatest {
            val (packageSelection, addresses) = it
            refreshShippingRates(packageSelection, addresses)
        }

    val viewState: MutableStateFlow<WooShippingViewState> = MutableStateFlow(WooShippingViewState.Loading)

    init {
        launch { observeShippingLabelInformation() }
        launch { getOrderInformation() }
        launch { getStoreOptions() }
        launch { getShippingAddresses() }
    }

    private suspend fun getOrderInformation() {
        orderDetailRepository.getOrderById(navArgs.orderId).let {
            order.value = it
        }
    }

    private fun getStoreOptions() {
        storeOptions.value = mockStoreOptions
    }

    private suspend fun getShippingAddresses() {
        order.filterNotNull().combine(observeOriginAddresses()) { order, originAddresses ->
            val selectedOriginAddress = getSelectedOriginAddress(originAddresses)
            WooShippingAddresses(
                shipFrom = selectedOriginAddress,
                originAddresses = originAddresses,
                shipTo = order.shippingAddress
            )
        }.collect { shippingAddresses.value = it }
    }

    private fun refreshShippingRates(
        packageSelection: PackageSelectionState,
        addresses: WooShippingAddresses?
    ) = flow {
        if (addresses != null && addresses.shipTo != Address.EMPTY && packageSelection is DataAvailable) {
            val sortOrder = selectedRatesSortOrder.value
            emit(ShippingRatesState.Loading(sortOrder))
            val shippingRatesResult = getShippingRates(
                selectedPackage = packageSelection.selectedPackage,
                sortOrder = sortOrder,
                shipTo = addresses.shipTo,
                shipFrom = addresses.shipFrom
            )
            if (shippingRatesResult.isSuccess) {
                emit(ShippingRatesState.DataState(sortOrder, shippingRatesResult.getOrThrow()))
            } else {
                emit(ShippingRatesState.Error)
            }
        } else {
            emit(ShippingRatesState.NoAvailable)
        }
    }

    private suspend fun observeShippingLabelInformation() {
        combine(
            storeOptions.drop(1),
            order.drop(1),
            shippingAddresses.drop(1),
            shippingRates,
            packageSelection
        ) { storeOptions, order, addresses, shippingRates, packageSelection ->
            if (order == null || storeOptions == null || addresses == null) {
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
                shippingAddresses = addresses,
                shippingRates = shippingRates,
                packageSelection = packageSelection
            )
        }.collect {
            viewState.value = it
        }
    }

    private fun getSelectedOriginAddress(originAddresses: List<OriginShippingAddress>): OriginShippingAddress {
        return (viewState.value as? WooShippingViewState.DataState)?.shippingAddresses?.shipFrom
            ?: originAddresses.firstOrNull { it.isDefault } ?: originAddresses.first()
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
                shippingAddresses = currentData.shippingAddresses.copy(shipTo = address)
            )
        }
    }

    fun onRefreshShippingRates() {
        launch { refreshShippingRates.emit(Unit) }
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

        (viewState.value as? WooShippingViewState.DataState)
            ?.takeIf { it.shippingRates is ShippingRatesState.DataState }
            ?.let { currentState ->
                (currentState.shippingRates as ShippingRatesState.DataState).let { state ->
                    viewState.value = currentState.copy(
                        shippingRates = ShippingRatesState.DataState(
                            selectedRatesSortOrder = option,
                            shippingRates = sortShippingRates(option, state.shippingRates)
                        )
                    )
                }
            }
    }

    private fun sortShippingRates(
        option: ShippingSortOption,
        shippingRates: Map<CarrierUI, List<ShippingRateUI>>
    ): Map<CarrierUI, List<ShippingRateUI>> {
        val comparator = when (option) {
            ShippingSortOption.CHEAPEST -> {
                cheapestComparator
            }

            ShippingSortOption.FASTEST -> {
                fastestComparator
            }
        }
        return shippingRates.mapValues { it.value.sortedWith(comparator) }
    }

    fun onPackageSelected(packageData: PackageData) {
        packageSelection.update { content ->
            when (content) {
                is NotSelected -> DataAvailable(
                    selectedPackage = packageData,
                    totalWeight = packageData.weight
                )

                is DataAvailable -> content.copy(selectedPackage = packageData)
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
            val packageSelection: PackageSelectionState
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
            val shippingRates: Map<CarrierUI, List<ShippingRateUI>>
        ) : ShippingRatesState()
    }

    sealed class PackageSelectionState {
        data object NotSelected : PackageSelectionState()
        data class DataAvailable(
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
