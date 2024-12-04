package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent
import java.io.Serializable

private const val ROUTE = "cash_payment"

data class WooPosCashPaymentNavigationState(
    val orderId: Long,
    val total: String,
): Serializable

fun NavController.navigateToCashPaymentScreen(navigationState: WooPosCashPaymentNavigationState) {
    navigate("$ROUTE/$navigationState")
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
