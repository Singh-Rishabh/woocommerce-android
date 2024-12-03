package com.woocommerce.android.ui.woopos.home.totals.payment.processing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.home.totals.WooPosTotalsViewState

@Composable
fun WooPosPaymentProcessingScreen(
    state: WooPosTotalsViewState.PaymentProcessing,
) {
    Box(
        modifier = Modifier
            .background(color = WooPosTheme.colors.paymentProcessingBackground)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = state.title)
            Text(text = state.subtitle)
        }
    }
}

@WooPosPreview
@Composable
fun WooPosPaymentProcessingScreenPreview() {
    WooPosPaymentProcessingScreen(
        state = WooPosTotalsViewState.PaymentProcessing(
            title = "Processing payment",
            subtitle = "Please wait...",
        )
    )
}