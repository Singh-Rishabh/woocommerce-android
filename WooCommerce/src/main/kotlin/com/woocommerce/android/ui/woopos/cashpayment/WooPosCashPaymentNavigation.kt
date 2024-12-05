package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

private const val ROUTE = "cash_payment"

fun NavController.navigateToCashPaymentScreen(orderId: Long) {
    navigate("$ROUTE/$orderId")
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
        WooPosCashPaymentScreen(
            state = WooPosCashPaymentState(
                enteredAmount = "5$",
                changeDue = "5$",
                total = "10$",
                canBeOrderBeCompleted = true,
            ),
            onAmountChanged = {},
            onCompleteOrderClicked = {},
            onBackClicked = {},
        )
    }
}
