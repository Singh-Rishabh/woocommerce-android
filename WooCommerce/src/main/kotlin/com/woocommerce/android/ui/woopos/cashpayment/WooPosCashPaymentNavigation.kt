package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

private const val ROUTE = "cash_payment/{orderId}"

fun NavController.navigateToCashPaymentScreen(orderId: Long) {
    navigate("cash_payment/$orderId")
}

fun NavGraphBuilder.cashPaymentScreen(
    onNavigationEvent: (WooPosNavigationEvent) -> Unit
) {
    composable(
        route = ROUTE,
        arguments = listOf(
            navArgument("orderId") { type = NavType.LongType }
        ),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 200f
                )
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 200f
                )
            )
        },
    ) { backStackEntry ->
        WooPosCashPaymentScreen(
            onNavigationEvent = onNavigationEvent,
        )
    }
}
