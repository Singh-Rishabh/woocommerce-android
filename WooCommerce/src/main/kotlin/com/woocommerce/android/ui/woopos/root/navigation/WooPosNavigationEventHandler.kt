package com.woocommerce.android.ui.woopos.root.navigation

import androidx.activity.ComponentActivity
import androidx.navigation.NavHostController
import com.woocommerce.android.ui.woopos.cashpayment.navigateToCashPaymentScreen
import com.woocommerce.android.ui.woopos.home.HOME_PAYMENT_COMPLETED_VIA_CASH_KEY
import com.woocommerce.android.ui.woopos.home.navigateToHomeScreen

fun NavHostController.handleNavigationEvent(
    event: WooPosNavigationEvent,
    activity: ComponentActivity,
) {
    when (event) {
        is WooPosNavigationEvent.ExitPosClicked,
        is WooPosNavigationEvent.BackFromSplashClicked -> activity.finish()

        is WooPosNavigationEvent.OpenHomeFromSplash -> navigateToHomeScreen()
        is WooPosNavigationEvent.OpenCashPayment -> navigateToCashPaymentScreen(event.orderId)
        is WooPosNavigationEvent.BackFromCashPayment -> {
            previousBackStackEntry
                ?.savedStateHandle
                ?.set(HOME_PAYMENT_COMPLETED_VIA_CASH_KEY, event.successfullyPaid)
            popBackStack()
        }
    }
}
