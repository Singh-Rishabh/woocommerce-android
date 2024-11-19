package com.woocommerce.android.ui.woopos.home.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooPosLeftPaneScreensViewModel @Inject constructor(
    private val navigator: LeftPaneNavigator,
) : ViewModel() {
    private val _screenState = MutableStateFlow<LeftPaneScreen>(LeftPaneScreen.ItemListScreen)
    val screenState: StateFlow<LeftPaneScreen> = _screenState

    init {
        viewModelScope.launch {
            listenToNavigationEvents()
        }
    }

    private fun navigateToVariationsScreen(product: VariableProductData) {
        _screenState.value = LeftPaneScreen.VariationsScreen(product)
    }

    private fun navigateBackToItemListScreen() {
        _screenState.value = LeftPaneScreen.ItemListScreen
    }

    private suspend fun listenToNavigationEvents() {
        navigator.events.collect {
            when (it) {
                is LeftPaneNavigator.LeftPaneNavigationEvent.NavigateToVariationsScreen -> {
                    navigateToVariationsScreen(it.product)
                }
                is LeftPaneNavigator.LeftPaneNavigationEvent.NavigateBackToItemListScreen -> {
                    navigateBackToItemListScreen()
                }
            }
        }
    }

    fun onUiEvent(wooPosLeftPaneScreensNavigationEvent: LeftPaneNavigator.LeftPaneNavigationEvent) {
        viewModelScope.launch {
            navigator.sendNavigationEvent(wooPosLeftPaneScreensNavigationEvent)
        }
    }

    sealed class LeftPaneScreen {
        data object ItemListScreen : LeftPaneScreen()
        data class VariationsScreen(val product: VariableProductData) : LeftPaneScreen()
    }
}
