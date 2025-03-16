package com.cataloghub.android.ui.woopos.root.navigation

import androidx.activity.ComponentActivity
import androidx.navigation.NavHostController
import com.cataloghub.android.ui.woopos.cashpayment.CASH_ROUTE
import com.cataloghub.android.ui.woopos.cashpayment.navigateToCashPaymentScreen
import com.cataloghub.android.ui.woopos.emailreceipt.navigateToEmailReceipt
import com.cataloghub.android.ui.woopos.home.navigateToHomeScreen
import com.cataloghub.android.ui.woopos.home.navigateToHomeScreenAfterSuccessfulCashPayment
import com.cataloghub.android.ui.woopos.home.navigateToHomeScreenIfHomeScreenNotOpen
import com.cataloghub.android.ui.woopos.util.analytics.WooPosAnalyticsEvent.Event.BackToCheckoutFromCash
import com.cataloghub.android.ui.woopos.util.analytics.WooPosAnalyticsTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun NavHostController.handleNavigationEvent(
    event: WooPosNavigationEvent,
    activity: ComponentActivity,
    tracker: WooPosAnalyticsTracker,
) {
    when (event) {
        is WooPosNavigationEvent.ExitPosClicked,
        is WooPosNavigationEvent.BackFromSplashClicked -> activity.finish()

        is WooPosNavigationEvent.OpenHomeFromSplash -> navigateToHomeScreen()
        is WooPosNavigationEvent.OpenCashPayment -> navigateToCashPaymentScreen(event.orderId)
        is WooPosNavigationEvent.GoBack -> {
            if (currentDestination?.route == CASH_ROUTE) {
                CoroutineScope(Dispatchers.Main).launch {
                    tracker.track(BackToCheckoutFromCash)
                }
            }
            popBackStack()
        }
        is WooPosNavigationEvent.OpenHomeFromCashPaymentAfterSuccessfulPayment ->
            navigateToHomeScreenAfterSuccessfulCashPayment()

        is WooPosNavigationEvent.OpenEmailReceipt -> navigateToEmailReceipt(event.orderId)
        WooPosNavigationEvent.ReturnHomeFromCashPayment -> navigateToHomeScreenIfHomeScreenNotOpen()
    }
}
