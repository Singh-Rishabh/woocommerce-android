package com.woocommerce.android.ui.woopos.home.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsScreen
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WooPosLeftPaneScreens(modifier: Modifier) {
    val viewModel: WooPosLeftPaneScreensViewModel = hiltViewModel()
    WooPosLeftPaneScreens(modifier = modifier, leftPaneScreen = viewModel.screenState) {
        viewModel.onUiEvent(LeftPaneNavigator.LeftPaneNavigationEvent.NavigateBackToItemListScreen)
    }
}

@Composable
fun WooPosLeftPaneScreens(
    modifier: Modifier,
    leftPaneScreen: StateFlow<WooPosLeftPaneScreensViewModel.LeftPaneScreen>,
    onNavigateToItemsListScreen: () -> Unit
) {
    val currentNavigationState = leftPaneScreen.collectAsState()
    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(targetState = currentNavigationState.value, label = "LeftPaneScreen") { navigationState ->
            when (navigationState) {
                is WooPosLeftPaneScreensViewModel.LeftPaneScreen.ItemListScreen -> {
                    WooPosItemsScreen(modifier = modifier)
                }

                is WooPosLeftPaneScreensViewModel.LeftPaneScreen.VariationsScreen -> {
                    NavigateToVariationsScreen(
                        variableProductData = navigationState.product,
                        modifier = modifier,
                        onBackClicked = { onNavigateToItemsListScreen() }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigateToVariationsScreen(
    variableProductData: VariableProductData,
    modifier: Modifier,
    onBackClicked: () -> Unit,
) {
    WooPosVariationsScreen(
        modifier,
        variableProductData,
        onBackClicked = onBackClicked
    )
}
