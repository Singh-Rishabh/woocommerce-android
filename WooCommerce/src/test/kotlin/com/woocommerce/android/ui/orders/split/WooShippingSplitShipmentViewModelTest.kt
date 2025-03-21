package com.woocommerce.android.ui.orders.split

import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.SplitShipmentArgs
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import com.woocommerce.android.ui.orders.wooshippinglabels.split.SelectableShippableItemUI
import com.woocommerce.android.ui.orders.wooshippinglabels.split.WooShippingSplitShipmentFragmentArgs
import com.woocommerce.android.ui.orders.wooshippinglabels.split.WooShippingSplitShipmentViewModel
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.util.observeForTesting
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingSplitShipmentViewModelTest : BaseUnitTest() {

    private val currencyFormatter: CurrencyFormatter = mock()
    lateinit var sut: WooShippingSplitShipmentViewModel

    private fun createViewModel(
        shipmentArgs: SplitShipmentArgs
    ) {
        val savedState = WooShippingSplitShipmentFragmentArgs(shipmentArgs).toSavedStateHandle()
        sut = WooShippingSplitShipmentViewModel(savedState, currencyFormatter)
    }

    @Test
    fun `when split shipments is opened, then display the correct number of expandable products`() = testBlocking {
        val shipmentArgs = SplitShipmentArgs(
            orderId = 1L,
            storeOptions = StoreOptionsModel.EMPTY,
            shipments = defaultShipment
        )

        whenever(currencyFormatter.formatCurrency(
            amount = any(),
            currencyCode = any(),
            applyDecimalFormatting = any())
        ).thenReturn("$5.00")

        createViewModel(shipmentArgs)

        sut.viewState.observeForTesting {  }

        val state = sut.viewState.value!!

        val selectableItems = state.selectableItems.getValue(1)
        val expandableProducts = selectableItems.shippableItems
            .filterIsInstance<SelectableShippableItemUI.ExpandableSelectableShippableItemUI>()
        assertThat(expandableProducts.size).isEqualTo(2)
    }

    @Test
    fun `when split shipments is opened, then display the correct number of single selectable products`() = testBlocking {
        val shipmentArgs = SplitShipmentArgs(
            orderId = 1L,
            storeOptions = StoreOptionsModel.EMPTY,
            shipments = defaultShipment
        )

        whenever(currencyFormatter.formatCurrency(
            amount = any(),
            currencyCode = any(),
            applyDecimalFormatting = any())
        ).thenReturn("$5.00")

        createViewModel(shipmentArgs)

        sut.viewState.observeForTesting {  }

        val state = sut.viewState.value!!

        val selectableItems = state.selectableItems.getValue(1)
        val expandableProducts = selectableItems.shippableItems
            .filterIsInstance<SelectableShippableItemUI.SingleSelectableShippableItemUI>()
        assertThat(expandableProducts.size).isEqualTo(1)
    }

    private val defaultShipment = mapOf(
        1 to listOf(
            ShippableItemModel(
                itemId = 1L,
                productId = 1L,
                title = "A product with quantity 1",
                price = BigDecimal(30),
                quantity = 1f,
                imageUrl = null,
                currency = "USD",
                length = 3f,
                width = 3f,
                height = 3f,
                weight = 8f
            ),
            ShippableItemModel(
                itemId = 2L,
                productId = 2L,
                title = "A product with quantity 5",
                price = BigDecimal(10),
                quantity = 5f,
                imageUrl = null,
                currency = "USD",
                length = 3f,
                width = 3f,
                height = 3f,
                weight = 8f
            ),
            ShippableItemModel(
                itemId = 3L,
                productId = 3L,
                title = "Another product with quantity 3",
                price = BigDecimal(10),
                quantity = 3f,
                imageUrl = null,
                currency = "USD",
                length = 3f,
                width = 3f,
                height = 3f,
                weight = 8f
            )
        )
    )

}
