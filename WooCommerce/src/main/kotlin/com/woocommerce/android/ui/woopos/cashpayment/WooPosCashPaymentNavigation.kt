package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.woocommerce.android.ui.woopos.home.WooPosHomeScreen
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

private const val ROUTE = "cash_payment"

fun NavController.navigateToCashPaymentScreen() {
    navigate(ROUTE)
}

fun NavGraphBuilder.cashPaymentScreen(
    onNavigationEvent: (WooPosNavigationEvent) -> Unit
) {
    composable(
        route = ROUTE,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        }
    ) {
        WooPosHomeScreen(onNavigationEvent)
    }
}
