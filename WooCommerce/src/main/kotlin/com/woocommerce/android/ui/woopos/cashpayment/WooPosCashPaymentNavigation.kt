package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
            fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        }
    ) { backStackEntry ->
        WooPosCashPaymentScreen(
            onNavigationEvent = onNavigationEvent,
        )
    }
}
