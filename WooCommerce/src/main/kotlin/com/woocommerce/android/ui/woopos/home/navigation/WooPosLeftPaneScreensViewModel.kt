package com.woocommerce.android.ui.woopos.home.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WooPosLeftPaneScreensViewModel @Inject constructor(
    private val navigator: LeftPaneNavigator,
) : ViewModel() {
    val leftPaneScreen = navigator.leftPaneScreen

    fun onUiEvent(wooPosLeftPaneScreensNavigationEvent: WooPosLeftPaneScreensNavigationEvent) {
        when (wooPosLeftPaneScreensNavigationEvent) {
            WooPosLeftPaneScreensNavigationEvent.OnNavigateToItemsListScreen -> {
                navigator.navigateBackToItemListScreen()
            }
        }
    }
}
