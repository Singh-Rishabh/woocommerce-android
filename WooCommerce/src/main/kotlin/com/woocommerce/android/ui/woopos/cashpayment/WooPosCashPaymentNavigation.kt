package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.woocommerce.android.ui.woopos.home.HOME_ROUTE
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

private const val CASH_ROUTE = "$HOME_ROUTE/cash_payment/{orderId}"

fun NavController.navigateToCashPaymentScreen(orderId: Long) {
    navigate(CASH_ROUTE.replace("{orderId}", orderId.toString()))
}

fun NavGraphBuilder.cashPaymentScreen(
    onNavigationEvent: (WooPosNavigationEvent) -> Unit
) {
    composable(
        route = CASH_ROUTE,
        arguments = listOf(
            navArgument("orderId") { type = NavType.LongType }
        ),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
            )
        },
    ) { backStackEntry ->
        WooPosCashPaymentScreen(
            onNavigationEvent = onNavigationEvent,
        )
    }
}
