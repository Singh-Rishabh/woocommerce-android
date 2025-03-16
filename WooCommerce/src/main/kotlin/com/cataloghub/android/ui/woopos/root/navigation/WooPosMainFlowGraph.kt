package com.cataloghub.android.ui.woopos.root.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import com.cataloghub.android.ui.woopos.cashpayment.cashPaymentScreen
import com.cataloghub.android.ui.woopos.emailreceipt.emailReceiptScreen
import com.cataloghub.android.ui.woopos.home.WooPosHomeViewModel
import com.cataloghub.android.ui.woopos.home.homeScreen
import com.cataloghub.android.ui.woopos.splash.SPLASH_ROUTE
import com.cataloghub.android.ui.woopos.splash.splashScreen

const val MAIN_GRAPH_ROUTE = "main-graph"

fun NavGraphBuilder.mainGraph(
    onNavigationEvent: (WooPosNavigationEvent) -> Unit,
    homeViewModel: WooPosHomeViewModel,
) {
    navigation(
        startDestination = SPLASH_ROUTE,
        route = MAIN_GRAPH_ROUTE,
    ) {
        splashScreen(onNavigationEvent = onNavigationEvent)
        homeScreen(homeViewModel = homeViewModel)
        cashPaymentScreen(onNavigationEvent = onNavigationEvent)
        emailReceiptScreen(onNavigationEvent = onNavigationEvent)
    }
}
