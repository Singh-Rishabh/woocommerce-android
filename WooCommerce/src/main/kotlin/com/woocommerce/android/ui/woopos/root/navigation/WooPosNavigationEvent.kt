package com.woocommerce.android.ui.woopos.root.navigation

import java.math.BigDecimal

sealed class WooPosNavigationEvent {
    data object ExitPosClicked : WooPosNavigationEvent()
    data object BackFromSplashClicked : WooPosNavigationEvent()
    data object OpenHomeFromSplash : WooPosNavigationEvent()
    data class OpenCashPayment(
        val orderId: Long,
        val total: BigDecimal,
    ) : WooPosNavigationEvent()
}
