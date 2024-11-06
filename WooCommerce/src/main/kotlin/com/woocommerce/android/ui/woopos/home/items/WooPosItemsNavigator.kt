package com.woocommerce.android.ui.woopos.home.items

import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WooPosItemsNavigator {
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.ItemListScreen)
    val navigationState: StateFlow<NavigationState> = _navigationState

    fun navigateToVariationsScreen(product: VariableProductData) {
        _navigationState.value = NavigationState.VariationsScreen(product)
    }

    fun navigateBackToItemListScreen() {
        _navigationState.value = NavigationState.ItemListScreen
    }

    sealed class NavigationState {
        data object ItemListScreen : NavigationState()
        data class VariationsScreen(val product: VariableProductData) : NavigationState()
    }
}

