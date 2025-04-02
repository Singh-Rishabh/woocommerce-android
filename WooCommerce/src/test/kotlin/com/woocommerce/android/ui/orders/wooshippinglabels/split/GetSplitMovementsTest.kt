package com.woocommerce.android.ui.orders.wooshippinglabels.split

import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import com.woocommerce.android.ui.orders.wooshippinglabels.toSelectableUIModel
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class GetSplitMovementsTest : BaseUnitTest() {
    private val currencyFormatter: CurrencyFormatter = org.mockito.kotlin.mock {
        on { formatCurrency(amount = any(), any(), any()) }.doAnswer { it.getArgument<BigDecimal>(0).toString() }
    }
    private val sut = GetSplitMovements()

    @Test
    fun `when there is no items selection, then movements is empty`() {
        val result = sut.invoke(
            currentShipment = defaultShipments.keys.first(),
            shipments = defaultShipments,
            selection = defaultSelection
        )
        assert(result.isEmpty())
    }

    @Test
    fun `when there is a single item selected, then movements matches the selection`() {
        val key = 0
        val updatedList = defaultSelection.getValue(key).shippableItems.toMutableList()
        val item = updatedList[0] as SelectableShippableItemUI.SingleSelectableShippableItemUI
        updatedList[0] = item.copy(isSelected = true)
        val selection = defaultSelection.toMutableMap()
        selection[key] = defaultSelection.getValue(key).copy(shippableItems = updatedList)

        val result = sut.invoke(
            currentShipment = defaultShipments.keys.first(),
            shipments = defaultShipments,
            selection = selection
        )
        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().totalItemsToMove).isEqualTo(1)
    }

    @Test
    fun `when there is an inner expandable item selected, then movements matches the selection`() {
        val key = 0
        val updatedList = defaultSelection.getValue(key).shippableItems.toMutableList()
        val item = updatedList[1] as SelectableShippableItemUI.ExpandableSelectableShippableItemUI
        updatedList[1] = item.copy(selectedIndexes = setOf(0))
        val selection = defaultSelection.toMutableMap()
        selection[key] = defaultSelection.getValue(key).copy(shippableItems = updatedList)

        val result = sut.invoke(
            currentShipment = defaultShipments.keys.first(),
            shipments = defaultShipments,
            selection = selection
        )
        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().totalItemsToMove).isEqualTo(1)
    }

    @Test
    fun `when there is a expandable item selected, then movements matches the item quantity`() {
        val key = 0
        val updatedList = defaultSelection.getValue(key).shippableItems.toMutableList()
        val item = updatedList[1] as SelectableShippableItemUI.ExpandableSelectableShippableItemUI
        updatedList[1] = item.copy(selectedIndexes = List(item.shippableItem.quantity.toInt()) { it }.toSet())
        val selection = defaultSelection.toMutableMap()
        selection[key] = defaultSelection.getValue(key).copy(shippableItems = updatedList)

        val result = sut.invoke(
            currentShipment = defaultShipments.keys.first(),
            shipments = defaultShipments,
            selection = selection
        )
        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().totalItemsToMove).isEqualTo(item.shippableItem.quantity.toInt())
    }

    private val defaultShippableItems = List(3) {
        ShippableItemModel(
            itemId = it.toLong(),
            productId = it.toLong(),
            title = "Product $it",
            price = BigDecimal(it),
            quantity = 1 + it.toFloat(),
            weight = it + 0.01f,
            currency = "USD",
            imageUrl = "https://example.com/image.jpg",
            width = it.toFloat(),
            height = it.toFloat(),
            length = it.toFloat()
        )
    }

    private val defaultShipments = mapOf(0 to defaultShippableItems)
    val defaultSelection = defaultShipments.mapValues {
        it.value.toSelectableUIModel(
            currencyFormatter = currencyFormatter,
            dimensionUnit = "cm",
            weightUnit = "kg"
        )
    }
}
