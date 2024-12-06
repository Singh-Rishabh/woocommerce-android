package com.woocommerce.android.ui.woopos.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

const val HOME_PAYMENT_COMPLETED_VIA_CASH_KEY = "home_payment_completed_via_cash_key"
private const val HOME_ROUTE = "home"

fun NavController.navigateToHomeScreen() {
    navigate(HOME_ROUTE)
}

fun NavGraphBuilder.homeScreen(
    onNavigationEvent: (WooPosNavigationEvent) -> Unit
) {
    composable(
        route = HOME_ROUTE,
        enterTransition = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = spring(
                    dampingRatio = 0.8f,
                    stiffness = 200f
                )
            )
        },
    ) { entry ->
        val isPaymentCompletedViaCash = entry.savedStateHandle.get<Boolean>(HOME_PAYMENT_COMPLETED_VIA_CASH_KEY) == true
        WooPosHomeScreen(
            isPaymentCompletedViaCash = isPaymentCompletedViaCash,
            onNavigationEvent = onNavigationEvent,
        )
    }
}
