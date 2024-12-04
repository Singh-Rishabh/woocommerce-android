package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosButton
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding

@Composable
fun WooPosCashPaymentScreen(
    state: WooPosCashPaymentState,
    onAmountChanged: (String) -> Unit,
    onCompleteOrderClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .width(540.dp)
        ) {
            PaymentCashToolbar(onBackClicked)

            Spacer(modifier = Modifier.weight(1f))

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
                label = { Text("Given amount") },
            )

            Spacer(modifier = Modifier.height(16.dp))

            WooPosButton(
                text = "Mark completed",
                onClick = onCompleteOrderClicked,
                enabled = state.canBeOrderBeCompleted,
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PaymentCashToolbar(onBackClicked: () -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val (backButton, title) = createRefs()
        IconButton(
            onClick = { onBackClicked() },
            modifier = Modifier
                .constrainAs(backButton) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    centerVerticallyTo(parent)
                }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_back_24dp),
                contentDescription = stringResource(R.string.woopos_cart_back_content_description),
                tint = MaterialTheme.colors.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }

        val iconTitlePadding = 16.dp.toAdaptivePadding()
        Text(
            text = stringResource(R.string.woopos_cash_payment_title),
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.onBackground,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .constrainAs(title) {
                    top.linkTo(backButton.top)
                    start.linkTo(backButton.end, margin = iconTitlePadding)
                    centerVerticallyTo(parent)
                }
        )
    }
}

@WooPosPreview
@Composable
fun WooPosTotalsPaymentCashScreenScreen() {
    WooPosTheme {
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
