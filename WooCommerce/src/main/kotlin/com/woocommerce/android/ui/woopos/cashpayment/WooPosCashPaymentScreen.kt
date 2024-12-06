package com.woocommerce.android.ui.woopos.cashpayment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosButton
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

@Composable
fun WooPosCashPaymentScreen(onNavigationEvent: (WooPosNavigationEvent) -> Unit) {
    val viewModel = hiltViewModel<WooPosCashPaymentViewModel>()
    val state = viewModel.state.collectAsState().value

    WooPosCashPaymentScreen(
        state = state,
        onAmountChanged = { viewModel.onUIEvent(WooPosCashPaymentUIEvent.AmountChanged(it)) },
        onCompleteOrderClicked = { viewModel.onUIEvent(WooPosCashPaymentUIEvent.CompleteOrderClicked) },
        onBackClicked = { onNavigationEvent(WooPosNavigationEvent.BackFromCashPayment) },
        onOrderComplete = { onNavigationEvent(WooPosNavigationEvent.OpenHomeFromCashPaymentAfterSuccessfulPayment) },
    )
}

@Composable
fun WooPosCashPaymentScreen(
    state: WooPosCashPaymentState,
    onAmountChanged: (String) -> Unit,
    onCompleteOrderClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onOrderComplete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Toolbar(onBackClicked)

        when (state) {
            is WooPosCashPaymentState.Collecting -> {
                Collecting(
                    state = state,
                    onAmountChanged = onAmountChanged,
                    onCompleteOrderClicked = onCompleteOrderClicked,
                )
            }

            WooPosCashPaymentState.Complete -> onOrderComplete()
            WooPosCashPaymentState.Initiating -> {
                // show nothing
            }
        }
    }
}

@Composable
private fun Collecting(
    state: WooPosCashPaymentState.Collecting,
    onAmountChanged: (String) -> Unit,
    onCompleteOrderClicked: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .width(540.dp)
                .padding(32.dp)
        ) {
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
                text = state.button.text,
                onClick = onCompleteOrderClicked,
                enabled = state.button.status == WooPosCashPaymentState.Collecting.Button.Status.ENABLED,
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun Toolbar(onBackClicked: () -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp.toAdaptivePadding())
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
                .padding(start = 8.dp.toAdaptivePadding())
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_back_24dp),
                contentDescription = stringResource(R.string.woopos_cart_back_content_description),
                tint = MaterialTheme.colors.onBackground,
                modifier = Modifier.size(28.dp)
            )
        }

        val iconTitlePadding = 8.dp.toAdaptivePadding()
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
            state = WooPosCashPaymentState.Collecting(
                enteredAmount = "5$",
                changeDue = "5$",
                total = "10$",
                button = WooPosCashPaymentState.Collecting.Button(
                    text = "Complete order",
                    status = WooPosCashPaymentState.Collecting.Button.Status.ENABLED
                )
            ),
            onAmountChanged = {},
            onCompleteOrderClicked = {},
            onBackClicked = {},
            onOrderComplete = {},
        )
    }
}
