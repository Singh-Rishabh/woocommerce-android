package com.woocommerce.android.ui.woopos.home.navigation

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class WooPosLeftPaneScreensViewModelTest {
    private val navigator: LeftPaneNavigator = mock()
    private val viewModel = WooPosLeftPaneScreensViewModel(navigator)

    @Test
    fun `when OnNavigateToItemsListScreen event fired, then proper method from navigator is called`() {
        viewModel.onUiEvent(WooPosLeftPaneScreensNavigationEvent.OnNavigateToItemsListScreen)

        verify(navigator).navigateBackToItemListScreen()
    }
}
