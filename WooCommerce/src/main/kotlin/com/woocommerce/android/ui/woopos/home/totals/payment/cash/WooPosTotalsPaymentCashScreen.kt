package com.woocommerce.android.ui.woopos.home.totals.payment.cash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosButton
import com.woocommerce.android.ui.woopos.home.totals.WooPosTotalsViewState

@Composable
fun WooPosTotalsPaymentCashScreen(
    state: WooPosTotalsViewState.CashPayment,
    onAmountChanged: (String) -> Unit,
    onCompleteOrderClicked: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column (
            modifier = Modifier
                .width(540.dp),
        ) {
            Text(
                text = "Cash payment",
                style = MaterialTheme.typography.h2,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total",
                style = MaterialTheme.typography.h5,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.total,
                style = MaterialTheme.typography.h6,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Change due",
                style = MaterialTheme.typography.h5,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.changeDue,
                style = MaterialTheme.typography.h5,
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = state.enteredAmount,
                onValueChange = onAmountChanged,
                label = { "Given amount" },
            )

            Spacer(modifier = Modifier.height(16.dp))

            WooPosButton(
                text = "Mark completed",
                onClick = onCompleteOrderClicked,
                enabled = state.canBeOrderBeCompleted,
            )
        }
    }
}

@WooPosPreview
@Composable
fun WooPosTotalsPaymentCashScreenScreen() {
    WooPosTotalsPaymentCashScreen(
        state = WooPosTotalsViewState.CashPayment(
            enteredAmount = "5$",
            changeDue = "5$",
            total = "10$",
            canBeOrderBeCompleted = true,
        ),
        onAmountChanged = {},
        onCompleteOrderClicked = {},
    )
}
