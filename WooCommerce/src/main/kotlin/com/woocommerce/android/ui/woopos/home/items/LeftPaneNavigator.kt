package com.woocommerce.android.ui.woopos.home.items

import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LeftPaneNavigator {
    private val _leftPaneScreen = MutableStateFlow<LeftPaneScreen>(LeftPaneScreen.ItemListScreen)
    val leftPaneScreen: StateFlow<LeftPaneScreen> = _leftPaneScreen

    fun navigateToVariationsScreen(product: VariableProductData) {
        _leftPaneScreen.value = LeftPaneScreen.VariationsScreen(product)
    }

    fun navigateBackToItemListScreen() {
        _leftPaneScreen.value = LeftPaneScreen.ItemListScreen
    }

    sealed class LeftPaneScreen {
        data object ItemListScreen : LeftPaneScreen()
        data class VariationsScreen(val product: VariableProductData) : LeftPaneScreen()
    }
}
