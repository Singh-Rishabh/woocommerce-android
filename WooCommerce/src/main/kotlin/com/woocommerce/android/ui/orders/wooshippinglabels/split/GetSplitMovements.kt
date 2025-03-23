package com.woocommerce.android.ui.orders.wooshippinglabels.split

import com.woocommerce.android.extensions.sumByFloat
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import javax.inject.Inject

class GetSplitMovements @Inject constructor() {
    operator fun invoke(
        currentShipment: Int,
        items: Map<Int, List<ShippableItemModel>>,
        selection: Map<Int, SelectableShippableItemsUI>
    ): List<SplitMovements> {
        val currentShipmentItems = mutableListOf<ShippableItemModel>()
        val nextShipmentItems = mutableListOf<ShippableItemModel>()

        selection.getValue(currentShipment).shippableItems.forEachIndexed { index, item ->
            when {
                item is SelectableShippableItemUI.SingleSelectableShippableItemUI && item.isSelected -> {
                    nextShipmentItems.add(items.getValue(currentShipment)[index])
                }

                item is SelectableShippableItemUI.SingleSelectableShippableItemUI && !item.isSelected -> {
                    currentShipmentItems.add(items.getValue(currentShipment)[index])
                }

                item is SelectableShippableItemUI.ExpandableSelectableShippableItemUI && item.isSelected -> {
                    nextShipmentItems.add(items.getValue(currentShipment)[index])
                }

                item is SelectableShippableItemUI.ExpandableSelectableShippableItemUI &&
                    !item.isSelected &&
                    item.selectedIndexes.isNotEmpty() -> {
                    val selected = item.selectedIndexes.size
                    val currentItem = items.getValue(currentShipment)[index]

                    currentShipmentItems.add(currentItem.copy(quantity = currentItem.quantity - selected))
                    nextShipmentItems.add(currentItem.copy(quantity = selected.toFloat()))
                }

                else -> {
                    currentShipmentItems.add(items.getValue(currentShipment)[index])
                }
            }
        }

        return if (nextShipmentItems.isNotEmpty()) {
            getPossibleKeys(
                currentShipment = currentShipment,
                items = items
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
        val possibleKeys = items.keys.filter { it != currentShipment }.toMutableList()
        if (possibleKeys.isEmpty()) {
            possibleKeys.add(currentShipment + 1)
        } else {
            possibleKeys.max().let { maxKey ->
                if (currentShipment != maxKey + 1) {
                    maxKey + 1
                } else {
                    maxKey + 2
                }
            }.let { possibleKeys.add(it) }
        }
        return possibleKeys
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
