package com.woocommerce.android.ui.woopos.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent
import com.woocommerce.android.ui.woopos.root.navigation.navigateOnce

const val HOME_ROUTE = "home"
const val HOME_PAYMENT_COMPLETED_VIA_CASH_KEY = "home_payment_completed_via_cash_key"

fun NavController.navigateToHomeScreen() {
    navigateOnce(HOME_ROUTE)
}

fun NavController.navigateToHomeScreenAfterSuccessfulCashPayment() {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set(HOME_PAYMENT_COMPLETED_VIA_CASH_KEY, true)

    popBackStack()
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
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
            )
        },
    ) { entry ->
        val savedStateHandle = entry.savedStateHandle
        val isPaymentCompletedViaCash = savedStateHandle.get<Boolean>(HOME_PAYMENT_COMPLETED_VIA_CASH_KEY) == true
        if (isPaymentCompletedViaCash) {
            savedStateHandle[HOME_PAYMENT_COMPLETED_VIA_CASH_KEY] = false
        }

        WooPosHomeScreen(
            isPaymentCompletedViaCash = isPaymentCompletedViaCash,
            onNavigationEvent = onNavigationEvent,
        )
    }
}
