package com.cataloghub.android.ui.payments.changeduecalculator

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.model.Order
import com.cataloghub.android.ui.orders.details.OrderDetailRepository
import com.cataloghub.android.ui.products.ParameterRepository
import com.cataloghub.android.ui.products.models.SiteParameters
import com.cataloghub.android.util.CurrencyFormatter
import com.cataloghub.android.viewmodel.BaseUnitTest
import com.cataloghub.android.viewmodel.ResourceProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal

private const val ORDER_TOTAL = "100.00"

@ExperimentalCoroutinesApi
class ChangeDueCalculatorViewModelTest : BaseUnitTest() {

    private val order: Order = mock {
        on { total }.thenReturn(BigDecimal(ORDER_TOTAL))
    }

    private val orderDetailRepository: OrderDetailRepository = mock()
    private val parameters: SiteParameters = mock {
        on { currencySymbol }.thenReturn("$")
    }
    private val parameterRepository: ParameterRepository = mock {
        on { getParameters() }.thenReturn(parameters)
    }
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("orderId" to 1L))
    private val resourceProvider: ResourceProvider = mock()
    private val currencyFormatter: CurrencyFormatter = mock()

    private lateinit var viewModel: ChangeDueCalculatorViewModel

    @Test
    fun `given valid order details, when order details are requested, then success state is emitted`() = runTest {
        // GIVEN
        whenever(orderDetailRepository.getOrderById(eq(1L))).thenReturn(order)

        // WHEN
        viewModel = ChangeDueCalculatorViewModel(
            savedStateHandle = savedStateHandle,
            orderDetailRepository = orderDetailRepository,
            parameterRepository = parameterRepository,
            resourceProvider = resourceProvider,
            currencyFormatter = currencyFormatter
        )

        // THEN
        val uiState = viewModel.uiState.value
        assertThat(uiState.change).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `when amount received is less than the amount due, then order cannot be completed`() = runTest {
        // GIVEN
        whenever(orderDetailRepository.getOrderById(eq(1L))).thenReturn(order)

        viewModel = ChangeDueCalculatorViewModel(
            savedStateHandle = savedStateHandle,
            orderDetailRepository = orderDetailRepository,
            parameterRepository = parameterRepository,
            resourceProvider = resourceProvider,
            currencyFormatter = currencyFormatter
        )

        // WHEN
        val canCompleteOrder = viewModel.uiState.value.canCompleteOrder

        // THEN
        assertThat(canCompleteOrder).isFalse
    }

    @Test
    fun `when updateRecordTransactionDetailsChecked is called, then recordTransactionDetailsChecked state is updated`() = runTest {
        // GIVEN
        whenever(orderDetailRepository.getOrderById(eq(1L))).thenReturn(order)
        viewModel = ChangeDueCalculatorViewModel(
            savedStateHandle = savedStateHandle,
            orderDetailRepository = orderDetailRepository,
            parameterRepository = parameterRepository,
            resourceProvider = resourceProvider,
            currencyFormatter = currencyFormatter
        )

        // WHEN
        viewModel.updateRecordTransactionDetailsChecked(true)
        advanceUntilIdle()

        // THEN
        val isChecked = viewModel.uiState.value.recordTransactionDetailsChecked
        assertThat(isChecked).isTrue
    }

    @Test
    fun `when getCurrencySymbol is called, then currency symbol from parameter repository is returned`() = runTest {
        // GIVEN
        val currencySymbol = "$"
        whenever(orderDetailRepository.getOrderById(eq(1L))).thenReturn(order)
        val siteParameters: SiteParameters = mock()
        whenever(siteParameters.currencySymbol).thenReturn(currencySymbol)
        whenever(parameterRepository.getParameters()).thenReturn(siteParameters)
        viewModel = ChangeDueCalculatorViewModel(
            savedStateHandle = savedStateHandle,
            orderDetailRepository = orderDetailRepository,
            parameterRepository = parameterRepository,
            resourceProvider = resourceProvider,
            currencyFormatter = currencyFormatter
        )

        // WHEN
        val result = viewModel.uiState.value.currencySymbol

        // THEN
        assertThat(result).isEqualTo(currencySymbol)
    }
}
