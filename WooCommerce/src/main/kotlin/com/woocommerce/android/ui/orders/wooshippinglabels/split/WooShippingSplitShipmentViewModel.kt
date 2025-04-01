package com.woocommerce.android.ui.orders.wooshippinglabels.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.woocommerce.android.ui.orders.wooshippinglabels.ShippableItemUI
import com.woocommerce.android.ui.orders.wooshippinglabels.toSelectableUIModel
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.getValue

@HiltViewModel
class WooShippingSplitShipmentViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val currencyFormatter: CurrencyFormatter
) : ScopedViewModel(savedState) {
    private val navArgs: WooShippingSplitShipmentFragmentArgs by savedState.navArgs()
    private val storeOptions = navArgs.shipmentArgs.storeOptions

    private val shipments = MutableStateFlow(navArgs.shipmentArgs.shipments)
    private val currentShipments = MutableStateFlow(navArgs.shipmentArgs.shipments)

    val selectableItems = shipments.map { shipment ->
        shipment.mapValues {
            it.value.toSelectableUIModel(
                currencyFormatter = currencyFormatter,
                dimensionUnit = storeOptions.dimensionUnit,
                weightUnit = storeOptions.weightUnit
            )
        }
    }

    val viewState = combine(
        shipments,
        currentShipments,
        selectableItems
    ) { shipments, currentShipments, selectableItems ->
        SplitShipmentViewState(
            selectableItems = selectableItems,
            hasChanges = shipments != currentShipments
        )
    }.asLiveData()

    fun onNavigateBack() {
        triggerEvent(MultiLiveEvent.Event.Exit)
    }

    data class SplitShipmentViewState(
        val selectableItems: Map<Int, SelectableShippableItemsUI>,
        val hasChanges: Boolean = false
    )
}

data class SelectableShippableItemsUI(
    val shippableItems: List<SelectableShippableItemUI>,
    val formattedTotalWeight: String,
    val formattedTotalPrice: String
)

sealed class SelectableShippableItemUI {
    data class SingleSelectableShippableItemUI(
        val shippableItem: ShippableItemUI,
        val isSelected: Boolean = false
    ) : SelectableShippableItemUI()

    data class ExpandableSelectableShippableItemUI(
        val shippableItem: ShippableItemUI,
        val innerShippableItem: ShippableItemUI,
        val isExpanded: Boolean = false,
        val selectedIndexes: Set<Int> = emptySet(),
    ) : SelectableShippableItemUI() {
        val isSelected: Boolean
            get() = selectedIndexes.size == shippableItem.quantity.toInt()
    }
}
