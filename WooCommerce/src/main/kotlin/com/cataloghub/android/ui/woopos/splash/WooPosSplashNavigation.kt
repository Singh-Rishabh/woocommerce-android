package com.cataloghub.android.ui.woopos.splash

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cataloghub.android.ui.woopos.root.navigation.WooPosNavigationEvent

const val SPLASH_ROUTE = "splash"

fun NavGraphBuilder.splashScreen(
    onNavigationEvent: (WooPosNavigationEvent) -> Unit
) {
    composable(SPLASH_ROUTE) {
        WooPosSplashScreen(onNavigationEvent)
    }
}
