package com.woocommerce.android.ui.orders.wooshippinglabels.split

import com.woocommerce.android.extensions.sumByFloat
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import javax.inject.Inject

class GetSplitMovements @Inject constructor() {
    operator fun invoke(
        currentShipment: Int,
        shipments: Map<Int, List<ShippableItemModel>>,
        selection: Map<Int, SelectableShippableItemsUI>
    ): List<SplitMovements> {
        val currentShipmentItems = mutableListOf<ShippableItemModel>()
        val nextShipmentItems = mutableListOf<ShippableItemModel>()

        selection.getValue(currentShipment).shippableItems.forEachIndexed { index, item ->
            when {
                item is SelectableShippableItemUI.SingleSelectableShippableItemUI && item.isSelected -> {
                    nextShipmentItems.add(shipments.getValue(currentShipment)[index])
                }

                item is SelectableShippableItemUI.SingleSelectableShippableItemUI && !item.isSelected -> {
                    currentShipmentItems.add(shipments.getValue(currentShipment)[index])
                }

                item is SelectableShippableItemUI.ExpandableSelectableShippableItemUI && item.isSelected -> {
                    nextShipmentItems.add(shipments.getValue(currentShipment)[index])
                }

                item is SelectableShippableItemUI.ExpandableSelectableShippableItemUI &&
                    !item.isSelected &&
                    item.selectedIndexes.isNotEmpty() -> {
                    val selected = item.selectedIndexes.size
                    val currentItem = shipments.getValue(currentShipment)[index]

                    currentShipmentItems.add(currentItem.copy(quantity = currentItem.quantity - selected))
                    nextShipmentItems.add(currentItem.copy(quantity = selected.toFloat()))
                }

                else -> {
                    currentShipmentItems.add(shipments.getValue(currentShipment)[index])
                }
            }
        }

        return if (nextShipmentItems.isNotEmpty()) {
            getPossibleKeys(
                currentShipment = currentShipment,
                items = shipments
            ).map { key ->
                SplitMovements(
                    currentShipment = currentShipment,
                    updatedCurrentShipmentItems = currentShipmentItems,
                    updatedShipment = key,
                    updatedShipmentItems = nextShipmentItems
                )
            }
        } else {
            emptyList()
        }
    }

    private fun getPossibleKeys(
        currentShipment: Int,
        items: Map<Int, List<ShippableItemModel>>,
    ): List<Int> {
        val otherKeys = items.keys.filter { it != currentShipment }
        var nextKey = (otherKeys.maxOrNull() ?: currentShipment) + 1
        if (nextKey == currentShipment) nextKey++
        return otherKeys + nextKey
    }
}

data class SplitMovements(
    val currentShipment: Int,
    val updatedCurrentShipmentItems: List<ShippableItemModel>,
    val updatedShipment: Int,
    val updatedShipmentItems: List<ShippableItemModel>
) {
    val totalItemsToMove: Int
        get() = updatedShipmentItems.sumByFloat { it.quantity }.toInt()
}
