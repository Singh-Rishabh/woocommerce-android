package com.woocommerce.android.ui.woopos.home.navigation

import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData
import com.woocommerce.android.ui.woopos.util.WooPosCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class WooPosLeftPaneScreensViewModelTest {

    @Rule
    @JvmField
    val coroutinesTestRule = WooPosCoroutineTestRule()

    private lateinit var navigator: LeftPaneNavigator
    private lateinit var viewModel: WooPosLeftPaneScreensViewModel
    private val navigationEvents = MutableSharedFlow<LeftPaneNavigator.LeftPaneNavigationEvent>()

    @Before
    fun setUp() {
        navigator = mock {
            on { events } doReturn navigationEvents
        }
        viewModel = WooPosLeftPaneScreensViewModel(navigator)
    }

    @Test
    fun `given view model init, then initial state is ItemListScreen`() = runTest {
        assert(viewModel.screenState.value is WooPosLeftPaneScreensViewModel.LeftPaneScreen.ItemListScreen)
    }

    @Test
    fun `given navigation event is NavigateToVariationsScreen, then updates screen state`() = runTest {
        val product = VariableProductData(
            1L,
            "Product Name",
            numOfVariations = 10,
        )
        navigationEvents.emit(LeftPaneNavigator.LeftPaneNavigationEvent.NavigateToVariationsScreen(product))

        assert(viewModel.screenState.value is WooPosLeftPaneScreensViewModel.LeftPaneScreen.VariationsScreen)
        assert(
            (
                viewModel.screenState.value as WooPosLeftPaneScreensViewModel.LeftPaneScreen.VariationsScreen
                ).product == product
        )
    }

    @Test
    fun `given navigate back to ItemListScreen then updates screen state`() = runTest {
        val product = VariableProductData(
            1L,
            "Product Name",
            numOfVariations = 10,
        )
        navigationEvents.emit(LeftPaneNavigator.LeftPaneNavigationEvent.NavigateToVariationsScreen(product))
        navigationEvents.emit(LeftPaneNavigator.LeftPaneNavigationEvent.NavigateBackToItemListScreen)

        assert(viewModel.screenState.value is WooPosLeftPaneScreensViewModel.LeftPaneScreen.ItemListScreen)
    }

    @Test
    fun `given view model init, then listen to navigation events and updates screen state`() = runTest {
        val product = VariableProductData(
            1L,
            "Product Name",
            numOfVariations = 10,
        )
        navigationEvents.emit(LeftPaneNavigator.LeftPaneNavigationEvent.NavigateToVariationsScreen(product))

        assert(viewModel.screenState.value is WooPosLeftPaneScreensViewModel.LeftPaneScreen.VariationsScreen)
        assert(
            (
                viewModel.screenState.value as WooPosLeftPaneScreensViewModel.LeftPaneScreen.VariationsScreen
                ).product == product
        )

        navigationEvents.emit(LeftPaneNavigator.LeftPaneNavigationEvent.NavigateBackToItemListScreen)

        assert(viewModel.screenState.value is WooPosLeftPaneScreensViewModel.LeftPaneScreen.ItemListScreen)
    }
}
