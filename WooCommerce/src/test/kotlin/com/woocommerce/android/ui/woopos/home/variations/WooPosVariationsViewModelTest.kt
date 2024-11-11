package com.woocommerce.android.ui.woopos.home.variations

import app.cash.turbine.test
import com.woocommerce.android.ui.products.ProductTestUtils
import com.woocommerce.android.ui.woopos.common.data.WooPosGetProductById
import com.woocommerce.android.ui.woopos.home.items.WooPosVariationsViewState
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsDataSource
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsViewModel
import com.woocommerce.android.ui.woopos.util.WooPosCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class WooPosVariationsViewModelTest {

    @Rule
    @JvmField
    val coroutinesTestRule = WooPosCoroutineTestRule()

    private val getProductById: WooPosGetProductById = mock()
    private val variationsDataSource: WooPosVariationsDataSource = mock()
    private lateinit var wooPosVariationsViewModel: WooPosVariationsViewModel

    @Test
    fun `given view model init, then loading state is displayed`() {
        wooPosVariationsViewModel = WooPosVariationsViewModel(getProductById, variationsDataSource)
        wooPosVariationsViewModel.init(1L)

        assertThat(
            wooPosVariationsViewModel.viewState.value
        ).isEqualTo(
            WooPosVariationsViewState.Loading(withCart = true)
        )
    }

    @Test
    fun `given view model init, then API call is made to fetch product`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(emptyFlow())

        wooPosVariationsViewModel = WooPosVariationsViewModel(getProductById, variationsDataSource)
        wooPosVariationsViewModel.init(1L)

        verify(getProductById).invoke(1L)
    }

    @Test
    fun `given view model init, then API call is made to fetch variation`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(emptyFlow())

        wooPosVariationsViewModel = WooPosVariationsViewModel(getProductById, variationsDataSource)
        wooPosVariationsViewModel.init(1L)

        verify(variationsDataSource).fetchVariations(eq(1L), any())
    }

    @Test
    fun `given view model init, then API call is made to fetch variation with forceRefresh set to true`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(emptyFlow())

        wooPosVariationsViewModel = WooPosVariationsViewModel(getProductById, variationsDataSource)
        wooPosVariationsViewModel.init(1L)

        verify(variationsDataSource).fetchVariations(1L, forceRefresh = true)
    }

    @Test
    fun `given view model init, when variation fetched successfully, then view state is updated with variation content`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(
            flowOf(
                listOf(
                    ProductTestUtils.generateProductVariation(1L, 2L),
                    ProductTestUtils.generateProductVariation(1L, 3L),
                    ProductTestUtils.generateProductVariation(1L, 4L),
                )
            )
        )
        whenever(getProductById.invoke(any())).thenReturn(
            ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
        )

        wooPosVariationsViewModel = WooPosVariationsViewModel(getProductById, variationsDataSource)
        wooPosVariationsViewModel.init(1L)
        advanceUntilIdle()

        wooPosVariationsViewModel.viewState.test {
            // THEN
            val value = awaitItem() as WooPosVariationsViewState.Content
            assertThat(value).isInstanceOf(WooPosVariationsViewState.Content::class.java)
        }
    }
}
