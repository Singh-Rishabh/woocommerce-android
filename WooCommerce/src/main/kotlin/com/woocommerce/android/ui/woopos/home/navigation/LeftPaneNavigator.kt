package com.woocommerce.android.ui.woopos.home.navigation

import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class LeftPaneNavigator @Inject constructor() {
    private val _events = MutableSharedFlow<LeftPaneNavigationEvent>()
    val events = _events.asSharedFlow()

    suspend fun sendNavigationEvent(event: LeftPaneNavigationEvent) {
        _events.emit(event)
    }

    sealed class LeftPaneNavigationEvent {
        data class NavigateToVariationsScreen(val product: VariableProductData) : LeftPaneNavigationEvent()
        data object NavigateBackToItemListScreen : LeftPaneNavigationEvent()
    }
}
