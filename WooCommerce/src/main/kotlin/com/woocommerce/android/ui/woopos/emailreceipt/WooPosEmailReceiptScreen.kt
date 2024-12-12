package com.woocommerce.android.ui.woopos.emailreceipt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosButton
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosToolbar
import com.woocommerce.android.ui.woopos.root.navigation.WooPosNavigationEvent

@Composable
fun WooPosEmailReceiptScreen(onNavigationEvent: (WooPosNavigationEvent) -> Unit) {
    val viewModel = hiltViewModel<WooPosEmailReceiptViewModel>()
    val state = viewModel.state.collectAsState().value

    WooPosEmailReceiptScreen(
        state = state,
        onEmailAddressChanged = { viewModel.onUIEvent(WooPosEmailReceiptUIEvent.EmailChanged(it)) },
        onSendReceiptClicked = { viewModel.onUIEvent(WooPosEmailReceiptUIEvent.SendEmailClicked) },
        onBackClicked = { onNavigationEvent(WooPosNavigationEvent.GoBack) },
    )
}

@Composable
private fun WooPosEmailReceiptScreen(
    state: WooPosEmailReceiptState,
    onEmailAddressChanged: (String) -> Unit,
    onSendReceiptClicked: () -> Unit,
    onBackClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        WooPosToolbar(
            titleText = stringResource(R.string.woopos_email_receipt_title),
            onBackClicked = onBackClicked,
        )
        ConstraintLayout(
            modifier = Modifier
                .width(540.dp),
        ) {
            val (title, email, button) = createRefs()

            Text(
                text = "Receipt",
                style = MaterialTheme.typography.h2,
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
            )

            TextField(
                value = state.email,
                onValueChange = onEmailAddressChanged,
                label = { "email" },
                modifier = Modifier.constrainAs(email) {
                    top.linkTo(title.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            WooPosButton(
                text = "Send",
                onClick = onSendReceiptClicked,
                modifier = Modifier.constrainAs(button) {
                    top.linkTo(email.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            )
        }
    }
}

@WooPosPreview
@Composable
fun PreviewWooPosTotalsPaymentReceiptScreen() {
    WooPosEmailReceiptScreen(
        state = WooPosEmailReceiptState(
            email = "email@google.com",
            button = WooPosEmailReceiptState.Button(
                text = "Send",
                status = WooPosEmailReceiptState.Button.Status.ENABLED
            )
        ),
        onEmailAddressChanged = {},
        onSendReceiptClicked = {},
        onBackClicked = {},
    )
}
