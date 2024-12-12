package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.woocommerce.android.model.Order
import com.woocommerce.android.ui.orders.OrderTestUtils
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.PackageSelectionState.DataAvailable
import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.WooShippingViewState
import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.WooShippingViewState.DataState
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippableItemModel
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.assertj.core.api.Assertions.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingLabelCreationViewModelTest : BaseUnitTest() {
    private val orderId = 1L
    private val defaultShippableItems = List(3) {
        ShippableItemModel(
            itemId = it.toLong(),
            productId = it.toLong(),
            title = "Product $it",
            price = BigDecimal(it),
            quantity = it.toFloat(),
            weight = it.toFloat(),
            currency = "USD",
            imageUrl = "https://example.com/image.jpg",
            width = it.toFloat(),
            height = it.toFloat(),
            length = it.toFloat()
        )
    }
    private val defaultShippingLines = List(3) {
        Order.ShippingLine(
            methodTitle = "Shipping Line $it",
            total = BigDecimal(it),
            methodId = it.toString(),
            itemId = it.toLong(),
            totalTax = BigDecimal.ZERO,
        )
    }
    private val defaultOriginAddresses = listOf(
        OriginShippingAddress(
            firstName = "first name",
            lastName = "last name",
            company = "Company",
            phone = "",
            address1 = "A huge address that should be truncated",
            address2 = "",
            city = "San Francisco",
            postcode = "",
            email = "email",
            country = "USA",
            state = "California",
            id = "id_1",
            isDefault = false,
            isVerified = true
        )
    )
    private val defaultShippingRates = emptyMap<Carrier, List<ShippingRateUI>>()

    private val orderDetailRepository: OrderDetailRepository = mock()
    private val getShippableItems: GetShippableItems = mock()
    private val currencyFormatter: CurrencyFormatter = mock {
        on { formatCurrency(any<BigDecimal>(), any(), any()) } doAnswer {
            val amount = it.getArgument(0) as BigDecimal
            "$ ${amount.toPlainString()}"
        }
    }
    private val savedState: SavedStateHandle =
        WooShippingLabelCreationFragmentArgs(orderId = orderId).toSavedStateHandle()

    private val observeOriginAddresses: ObserveOriginAddresses = mock()
    private val getShippingRates: GetShippingRates = mock()

    private lateinit var sut: WooShippingLabelCreationViewModel

    fun createViewModel() {
        sut = WooShippingLabelCreationViewModel(
            orderDetailRepository = orderDetailRepository,
            getShippableItems = getShippableItems,
            currencyFormatter = currencyFormatter,
            observeOriginAddresses = observeOriginAddresses,
            getShippingRates = getShippingRates,
            savedState = savedState
        )
    }

    @Test
    fun `when the order NO contains shipping lines, then NO shipping lines summary is displayed`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = emptyList()
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        val currentViewState = sut.viewState.value
        assert(currentViewState is DataState)
        val dataState = currentViewState as DataState
        assert(dataState.shippingLines.isEmpty())
    }

    @Test
    fun `when the order contains shipping lines, then shipping lines summary is displayed`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        val currentViewState = sut.viewState.value
        assert(currentViewState is DataState)
        val dataState = currentViewState as DataState
        assert(dataState.shippingLines.isNotEmpty())
        assertEquals(dataState.shippingLines.size, defaultShippingLines.size)
    }

    @Test
    fun `when the order is not found, then show an error`() = testBlocking {
        val order: Order? = null
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        val currentViewState = sut.viewState.value
        assert(currentViewState is WooShippingViewState.Error)
    }

    @Test
    fun `when there are no origin addresses, then show an error`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(observeOriginAddresses()) doReturn flowOf(emptyList())
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        val currentViewState = sut.viewState.value
        assert(currentViewState is WooShippingViewState.Error)
    }

    @Test
    fun `when there are origin addresses, then display the origin addresses`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        val currentViewState = sut.viewState.value
        assert(currentViewState is DataState)
        val dataState = currentViewState as DataState
        assertEquals(dataState.shippingAddresses.originAddresses.size, defaultOriginAddresses.size)
        val ids = dataState.shippingAddresses.originAddresses.map { it.id }
        assert(ids.containsAll(defaultOriginAddresses.map { it.id }))
    }

    @Test
    fun `when shipping rates succeed then display the shipping rates`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        val currentViewState = sut.viewState.value
        assert(currentViewState is DataState)
        val dataState = currentViewState as DataState
        assertIs<WooShippingLabelCreationViewModel.ShippingRatesState.DataState>(dataState.shippingRates)
    }

    @Test
    fun `when shipping rates fail then display an error`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.failure(Exception("Random error"))

        createViewModel()

        val currentViewState = sut.viewState.value
        assert(currentViewState is DataState)
        val dataState = currentViewState as DataState
        assertIs<WooShippingLabelCreationViewModel.ShippingRatesState.Error>(dataState.shippingRates)
    }

    @Test
    fun `when refresh rates is triggered then refresh shipping rates`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()
        sut.onRefreshShippingRates()

        verify(getShippingRates, times(2)).invoke(any(), any())
    }

    @Test
    fun `when rates sort order is changed then refresh shipping rates`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        sut.onSelectedRateSortOrderChanged(ShippingSortOption.CHEAPEST)

        verify(getShippingRates, times(2)).invoke(any(), any())
    }

    @Test
    fun `when rates sort order is NOT changed then DON'T refresh shipping rates`() = testBlocking {
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()

        sut.onSelectedRateSortOrderChanged(ShippingSortOption.FASTEST)

        verify(getShippingRates, times(1)).invoke(any(), any())
    }

    @Test
    fun `onPackageSelected updates state to DataAvailable when current state is DataAvailable`() = testBlocking {
        var currentViewState: WooShippingViewState? = null
        val order = OrderTestUtils.generateTestOrder(orderId = orderId).copy(
            shippingLines = defaultShippingLines
        )
        whenever(orderDetailRepository.getOrderById(any())) doReturn order
        whenever(getShippableItems(any())) doReturn defaultShippableItems
        whenever(observeOriginAddresses()) doReturn flowOf(defaultOriginAddresses)
        whenever(getShippingRates(any(), any())) doReturn Result.success(defaultShippingRates)

        createViewModel()
        sut.viewState.asLiveData().observeForever {
            currentViewState = it
        }

        val initialPackageData = PackageData(
            name = "Initial Package",
            dimensions = "5 x 5 x 5",
            weight = "0.5",
            isSelected = true,
            isLetter = false
        )

        sut.onPackageSelected(initialPackageData)

        val newPackageData = PackageData(
            name = "New Package",
            dimensions = "10 x 10 x 10",
            weight = "1.5",
            isSelected = true,
            isLetter = false
        )

        sut.onPackageSelected(newPackageData)

        assertThat(currentViewState).isInstanceOf(DataState::class.java)
        val dataState = currentViewState as DataState

        assertThat(dataState.packageSelection).isInstanceOf(DataAvailable::class.java)
        val dataAvailable = dataState.packageSelection as DataAvailable
        assertThat(dataAvailable.selectedPackage).isEqualTo(newPackageData)
    }
}
