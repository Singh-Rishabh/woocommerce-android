package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
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

    private val emptyOrder = Order.getEmptyOrder(Date(), Date())
    private val order = MutableStateFlow<Order?>(emptyOrder)
    private val shippingAddresses = MutableStateFlow<WooShippingAddresses?>(WooShippingAddresses.EMPTY)
    private val storeOptions = MutableStateFlow<StoreOptionsModel?>(StoreOptionsModel.EMPTY)

    private val shippableItems = MutableStateFlow<List<ShippableItemModel>>(emptyList())

    private val packageSelected = MutableStateFlow<PackageData?>(null)
    private val packageWeight = MutableStateFlow<PackageWeight?>(null)
    private val packageSelection = MutableStateFlow<PackageSelectionState>(NotSelected)

    private val selectedRatesSortOrder = MutableStateFlow(ShippingSortOption.FASTEST)
    private val refreshShippingRates = MutableSharedFlow<Unit>()
    var customWeight by mutableStateOf("")
        private set

    private val cheapestComparator = Comparator<ShippingRateUI> { r1, r2 ->
        r1.defaultRate.rate.price.compareTo(r2.defaultRate.rate.price)
    }
    private val fastestComparator = Comparator<ShippingRateUI> { r1, r2 ->
        r1.defaultRate.rate.deliveryDays.compareTo(r2.defaultRate.rate.deliveryDays)
    }

    private val selectedRate = MutableStateFlow<ShippingRateUI?>(null)
    private val shippingRates = MutableStateFlow<Map<CarrierUI, List<ShippingRateUI>>>(emptyMap())
    private val shippingRatesState = MutableStateFlow<ShippingRatesState>(ShippingRatesState.NoAvailable)

    val viewState: MutableStateFlow<WooShippingViewState> = MutableStateFlow(WooShippingViewState.Loading)

    init {
        launch { observeShippingLabelInformation() }
        launch { getStoreOptions() }
        launch { getShippingAddresses() }
        launch { getOrderInformation() }
        launch { observePackageWeight() }
        launch { observePackageChanges() }
        launch { observeShippingRates() }
        launch { observeShippingRatesState() }
    }

    private suspend fun getOrderInformation() {
        orderDetailRepository.getOrderById(navArgs.orderId).let { order.value = it }
    }

    private fun getStoreOptions() {
        storeOptions.value = mockStoreOptions
    }

    @Suppress("ComplexCondition")
    @OptIn(FlowPreview::class)
    private suspend fun observeShippingRates() {
        combine(
            packageSelected,
            shippingAddresses,
            packageWeight,
            refreshShippingRates.onStart { emit(Unit) }
        ) { selectedPackage, addresses, packageWeight, _ ->
            if (
                selectedPackage != null &&
                addresses != null &&
                packageWeight != null &&
                addresses.shipTo != Address.EMPTY
            ) {
                ShippingRatesInfo(
                    orderId = navArgs.orderId,
                    packageSelected = selectedPackage,
                    shipFrom = addresses.shipFrom,
                    shipTo = addresses.shipTo,
                    weight = packageWeight.totalWeight,
                    currencyCode = order.value?.currency
                )
            } else {
                null
            }
        }
            .debounce(MULTIPLE_CALLS_DELAY)
            .collectLatest {
                updateShippingRates(it)
            }
    }

    private suspend fun observeShippingRatesState() {
        combine(
            shippingRates,
            selectedRate,
            selectedRatesSortOrder
        ) { shippingRates, selectedRate, selectedRatesSortOrder ->
            if (shippingRates.isEmpty()) {
                ShippingRatesState.NoAvailable
            } else {
                ShippingRatesState.DataState(
                    selectedRatesSortOrder,
                    sortShippingRates(selectedRatesSortOrder, shippingRates),
                    selectedRate
                )
            }
        }.collectLatest {
            shippingRatesState.value = it
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observePackageWeight() {
        combine(
            shippableItems,
            packageSelected,
            snapshotFlow { customWeight }.debounce(TYPING_DELAY)
        ) { shippableItems, selectedPackage, customWeightString ->
            val itemsWeight = shippableItems.sumByFloat { it.weight }
            val packageWeight = selectedPackage?.weight?.toFloatOrNull()
            PackageWeight(
                itemsWeight = itemsWeight,
                packageWeight = packageWeight,
                customWeight = customWeightString.toFloatOrNull()
            )
        }.collectLatest {
            packageWeight.value = it
        }
    }

    private suspend fun observePackageChanges() {
        packageSelected.combine(packageWeight) { packageSelected, packageWeight ->
            if (packageSelected == null || packageWeight == null) {
                NotSelected
            } else {
                DataAvailable(
                    selectedPackage = packageSelected,
                    defaultWeight = packageWeight.defaultWeight.toString(),
                    weightUnit = mockStoreOptions.weightUnit
                )
            }
        }.collectLatest {
            packageSelection.value = it
        }
    }

    private suspend fun getShippingAddresses() {
        order.combine(observeOriginAddresses()) { order, originAddresses ->
            if (order != null && originAddresses.isNotEmpty()) {
                val selectedOriginAddress = getSelectedOriginAddress(originAddresses)
                WooShippingAddresses(
                    shipFrom = selectedOriginAddress,
                    originAddresses = originAddresses,
                    shipTo = order.shippingAddress
                )
            } else {
                null
            }
        }.collect { shippingAddresses.value = it }
    }

    private suspend fun updateShippingRates(shippingRatesInfo: ShippingRatesInfo?) {
        if (shippingRatesInfo != null) {
            val sortOrder = selectedRatesSortOrder.value
            shippingRatesState.value = ShippingRatesState.Loading(sortOrder)

            val shippingRatesResult = getShippingRates(
                shippingRatesInfo.orderId,
                shippingRatesInfo.packageSelected,
                shippingRatesInfo.shipTo,
                shippingRatesInfo.shipFrom,
                shippingRatesInfo.weight,
                shippingRatesInfo.currencyCode
            )

            if (shippingRatesResult.isSuccess && shippingRatesResult.getOrThrow().isNotEmpty()) {
                shippingRates.value = shippingRatesResult.getOrThrow()
            } else {
                shippingRatesState.value = ShippingRatesState.Error
            }
        } else {
            shippingRatesState.value = ShippingRatesState.NoAvailable
        }
    }

    private suspend fun observeShippingLabelInformation() {
        combine(
            storeOptions.drop(1),
            order.drop(1),
            shippingAddresses.drop(1),
            shippingRatesState,
            packageSelection,
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
        }.collectLatest {
            viewState.value = it
        }
    }

    private fun getSelectedOriginAddress(originAddresses: List<OriginShippingAddress>): OriginShippingAddress {
        return shippingAddresses.value?.shipFrom?.takeIf {
            it != OriginShippingAddress.EMPTY
        } ?: originAddresses.first()
    }

    fun onShippingFromAddressChange(address: OriginShippingAddress) {
        shippingAddresses.value?.let {
            shippingAddresses.value = it.copy(shipFrom = address)
        }
    }

    fun onShippingToAddressChange(address: Address) {
        shippingAddresses.value?.let {
            shippingAddresses.value = it.copy(shipTo = address)
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
    }

    fun onSelectedSippingRateChanged(rate: ShippingRateUI) {
        selectedRate.update { rate }
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
        packageSelected.value = packageData
    }

    fun onCustomWeightChange(input: String) {
        customWeight = input
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
            val shippingRates: Map<CarrierUI, List<ShippingRateUI>>,
            val selectedRate: ShippingRateUI? = null
        ) : ShippingRatesState()
    }

    sealed class PackageSelectionState {
        data object NotSelected : PackageSelectionState()
        data class DataAvailable(
            val selectedPackage: PackageData,
            val defaultWeight: String,
            val weightUnit: String
        ) : PackageSelectionState()
    }

    data class PackageWeight(
        val itemsWeight: Float,
        val packageWeight: Float? = null,
        val customWeight: Float? = null
    ) {
        val defaultWeight: Float
            get() = itemsWeight + (packageWeight ?: 0f)
        val totalWeight: Float
            get() = customWeight ?: defaultWeight
    }

    data class ShippingRatesInfo(
        val orderId: Long,
        val packageSelected: PackageData,
        val shipFrom: OriginShippingAddress,
        val shipTo: Address,
        val weight: Float,
        val currencyCode: String?
    )

    companion object {
        private const val TYPING_DELAY = 800L
        private const val MULTIPLE_CALLS_DELAY = 50L
    }
}

data class WooShippingAddresses(
    val shipFrom: OriginShippingAddress,
    val shipTo: Address,
    val originAddresses: List<OriginShippingAddress>
) {
    companion object {
        val EMPTY = WooShippingAddresses(
            shipFrom = OriginShippingAddress.EMPTY,
            shipTo = Address.EMPTY,
            originAddresses = emptyList()
        )
    }
}
