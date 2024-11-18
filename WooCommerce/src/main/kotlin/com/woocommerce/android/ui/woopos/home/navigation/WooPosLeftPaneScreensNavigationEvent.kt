package com.woocommerce.android.ui.woopos.home.navigation

sealed class WooPosLeftPaneScreensNavigationEvent {
    data object OnNavigateToItemsListScreen : WooPosLeftPaneScreensNavigationEvent()
}
