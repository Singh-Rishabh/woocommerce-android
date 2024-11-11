package com.woocommerce.android.ui.woopos.home.variations

import com.woocommerce.android.ui.woopos.common.data.WooPosGetProductById
import com.woocommerce.android.ui.woopos.home.items.WooPosVariationsViewState
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsDataSource
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsViewModel
import com.woocommerce.android.ui.woopos.util.WooPosCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

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
}
